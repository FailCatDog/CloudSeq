package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.pojo.dto.OfficeCallbackRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeCallbackResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeDocumentDetailDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeEditorConfigDTO;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceFile;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.service.OfficeDocumentService;
import cn.guet.soft_manage.biz.service.ObjectStorageService;
import cn.guet.soft_manage.biz.service.WorkspaceAccessService;
import cn.guet.soft_manage.biz.service.WorkspaceFileService;
import cn.guet.soft_manage.biz.utils.OfficeDownloadTokenUtil;
import cn.guet.soft_manage.biz.utils.OfficeJwtUtil;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.config.OfficeProperties;
import cn.guet.soft_manage.frame.constant.OfficeConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.WorkspaceNodeType;
import cn.guet.soft_manage.frame.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OnlyOffice 文档集成服务实现
 */
@Slf4j
@Service
public class OfficeDocumentServiceImpl implements OfficeDocumentService {

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private WorkspaceAccessService workspaceAccessService;

    @Resource
    private WorkspaceFileService workspaceFileService;

    @Resource
    private ObjectStorageService objectStorageService;

    @Resource
    private OfficeProperties officeProperties;

    @Resource
    private OfficeJwtUtil officeJwtUtil;

    @Resource
    private OfficeDownloadTokenUtil officeDownloadTokenUtil;

    @Override
    public OfficeDocumentDetailDTO getOfficeDocument(Long nodeId) {
        WorkspaceNode node = requireOfficeNode(nodeId);
        WorkspaceAccessContext access = workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());
        WorkspaceFile file = workspaceFileService.requireByNodeId(nodeId);

        return OfficeDocumentDetailDTO.builder()
                .nodeId(node.getId())
                .workspaceId(node.getWorkspaceId())
                .title(node.getTitle())
                .fileName(file.getFileName())
                .version(file.getVersion())
                .canWrite(access.isCanWrite())
                .updateDate(file.getUpdateDate())
                .build();
    }

    @Override
    public OfficeEditorConfigDTO getEditorConfig(Long nodeId) {
        WorkspaceNode node = requireOfficeNode(nodeId);
        WorkspaceAccessContext access = workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());
        WorkspaceFile file = workspaceFileService.requireByNodeId(nodeId);

        User user = UserContext.get();
        if (user == null || user.getId() == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        String baseUrl = normalizeBaseUrl(officeProperties.getCallbackBaseUrl());
        String documentKey = OfficeConstants.buildDocumentKey(nodeId, file.getVersion());
        String documentUrl = objectStorageService.getPresignedDownloadUrl(
                file.getStorageKey(),
                officeProperties.getDownloadTokenExpireSeconds());

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("fileType", "docx");
        document.put("key", documentKey);
        document.put("title", file.getFileName());
        document.put("url", documentUrl);

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", String.valueOf(user.getId()));
        userMap.put("name", resolveDisplayName(user));

        Map<String, Object> editorConfig = new LinkedHashMap<>();
        editorConfig.put("callbackUrl", baseUrl + "/api/documents/" + nodeId + "/office/callback");
        log.info("Office editor config: nodeId={}, callbackUrl={}, documentUrl={}",
                nodeId, editorConfig.get("callbackUrl"), documentUrl);
        editorConfig.put("lang", "zh-CN");
        editorConfig.put("mode", access.isCanWrite() ? "edit" : "view");
        editorConfig.put("user", userMap);

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("documentType", "word");
        config.put("document", document);
        config.put("editorConfig", editorConfig);

        if (officeProperties.isJwtEnabled()) {
            config.put("token", officeJwtUtil.signPayload(config));
        }

        return OfficeEditorConfigDTO.builder()
                .documentServerUrl(normalizeBaseUrl(officeProperties.getDocumentServerUrl()))
                .config(config)
                .build();
    }

    @Override
    public org.springframework.core.io.Resource downloadDocument(Long nodeId, String token) {
        Long tokenNodeId = officeDownloadTokenUtil.parseNodeId(token);
        if (!Objects.equals(tokenNodeId, nodeId)) {
            throw new BusinessException(BizResponseCode.OFFICE_TOKEN_INVALID);
        }

        requireOfficeNode(nodeId);
        WorkspaceFile file = workspaceFileService.requireByNodeId(nodeId);
        InputStream inputStream = workspaceFileService.openStream(file);
        return new InputStreamResource(inputStream);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfficeCallbackResponseDTO handleCallback(Long nodeId, OfficeCallbackRequestDTO request) {
        if (request == null) {
            return OfficeCallbackResponseDTO.failure();
        }

        CallbackPayload payload = resolveCallbackPayload(request);
        log.info("Office callback: nodeId={}, key={}, status={}", nodeId, payload.key(), payload.status());

        if (payload.key() == null || payload.status() == null) {
            log.warn("Office callback invalid payload: nodeId={}, body={}", nodeId, request);
            return OfficeCallbackResponseDTO.failure();
        }

        Long keyNodeId = OfficeConstants.parseNodeIdFromKey(payload.key());
        if (!Objects.equals(keyNodeId, nodeId)) {
            log.warn("Office callback key mismatch: nodeId={}, key={}", nodeId, payload.key());
            return OfficeCallbackResponseDTO.failure();
        }

        requireOfficeNode(nodeId);

        if (payload.status() == OfficeConstants.CALLBACK_STATUS_EDITING
                || payload.status() == OfficeConstants.CALLBACK_STATUS_CLOSED) {
            return OfficeCallbackResponseDTO.success();
        }

        if (payload.status() == OfficeConstants.CALLBACK_STATUS_SAVE
                || payload.status() == OfficeConstants.CALLBACK_STATUS_FORCE_SAVE) {
            if (!StringUtils.hasText(payload.url())) {
                log.warn("Office callback missing url: nodeId={}, status={}", nodeId, payload.status());
                return OfficeCallbackResponseDTO.failure();
            }
            try {
                String fetchUrl = normalizeOnlyOfficeFetchUrl(payload.url());
                log.info("Office callback save: nodeId={}, fetchUrl={}", nodeId, fetchUrl);
                byte[] bytes = downloadFromUrl(fetchUrl);
                Long updateUser = resolveUpdateUser(payload.users());
                try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                    workspaceFileService.replaceContent(nodeId, inputStream, bytes.length, updateUser);
                }
            } catch (Exception ex) {
                log.error("Office callback save failed: nodeId={}", nodeId, ex);
                return OfficeCallbackResponseDTO.failure();
            }
        }

        return OfficeCallbackResponseDTO.success();
    }

    private CallbackPayload resolveCallbackPayload(OfficeCallbackRequestDTO request) {
        if (officeProperties.isJwtEnabled() && StringUtils.hasText(request.getToken())) {
            try {
                Claims claims = officeJwtUtil.parseToken(request.getToken());
                CallbackPayload fromToken = new CallbackPayload(
                        readString(claims.get("key")),
                        readStatus(claims.get("status")),
                        readString(claims.get("url")),
                        toStringList(claims.get("users")));
                if (fromToken.key() != null && fromToken.status() != null) {
                    return fromToken;
                }
            } catch (Exception ex) {
                log.warn("Office callback token parse failed: {}", ex.getMessage());
            }
        }
        return new CallbackPayload(request.getKey(), request.getStatus(), request.getUrl(), request.getUsers());
    }

    private String readString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return null;
    }

    private WorkspaceNode requireOfficeNode(Long nodeId) {
        if (nodeId == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null || Objects.equals(node.getDelFlag(), 1)) {
            throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        }
        if (!Objects.equals(node.getNodeType(), WorkspaceNodeType.OFFICE.getCode())) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);
        }
        return node;
    }

    private byte[] downloadFromUrl(String urlString) throws Exception {
        URI uri = URI.create(urlString);
        try (InputStream inputStream = uri.toURL().openStream()) {
            return inputStream.readAllBytes();
        }
    }

    private String normalizeOnlyOfficeFetchUrl(String urlString) {
        if (!StringUtils.hasText(urlString)) {
            return urlString;
        }
        String publicBase = normalizeBaseUrl(officeProperties.getDocumentServerUrl());
        if (urlString.contains("://localhost")
                || urlString.contains("://127.0.0.1")
                || urlString.contains("://onlyoffice")) {
            return urlString.replaceFirst("https?://[^/]+", publicBase);
        }
        return urlString;
    }

    private Integer readStatus(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private Long resolveUpdateUser(List<String> users) {
        if (users == null || users.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(users.get(0));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private String resolveDisplayName(User user) {
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        if (StringUtils.hasText(user.getNickName())) {
            return user.getNickName();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "用户";
    }

    private String normalizeBaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private record CallbackPayload(String key, Integer status, String url, List<String> users) {
    }
}
