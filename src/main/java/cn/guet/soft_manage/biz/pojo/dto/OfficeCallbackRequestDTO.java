package cn.guet.soft_manage.biz.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * OnlyOffice 保存回调请求体
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfficeCallbackRequestDTO {

    private String token;

    private String key;

    private Integer status;

    private String url;

    private List<String> users;
}
