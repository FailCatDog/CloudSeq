package cn.guet.feishu.websocket;

import cn.guet.feishu.websocket.dto.DocumentEditMessage;
import cn.guet.feishu.websocket.dto.UserPresence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DocumentWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Map<String, UserPresence>> documentUsers = new ConcurrentHashMap<>();

    /**
     * 用户加入文档编辑
     * 将用户添加到在线用户列表，并广播加入通知
     * 
     * @param user 用户状态信息，包含文档ID、用户ID、用户名
     */
    @MessageMapping("/document/join")
    public void joinDocument(@Payload UserPresence user) {
        String documentId = user.getDocumentId();
        documentUsers.computeIfAbsent(documentId, k -> new ConcurrentHashMap<>())
                .put(user.getUserId(), user);
        
        user.setAction("join");
        messagingTemplate.convertAndSend("/topic/document/" + documentId + "/presence", user);
        
        messagingTemplate.convertAndSend("/topic/document/" + documentId + "/users", 
                documentUsers.get(documentId).values());
    }

    /**
     * 用户离开文档编辑
     * 从在线用户列表移除用户，并广播离开通知
     * 
     * @param user 用户状态信息，包含文档ID、用户ID、用户名
     */
    @MessageMapping("/document/leave")
    public void leaveDocument(@Payload UserPresence user) {
        String documentId = user.getDocumentId();
        Map<String, UserPresence> users = documentUsers.get(documentId);
        if (users != null) {
            users.remove(user.getUserId());
            if (users.isEmpty()) {
                documentUsers.remove(documentId);
            }
        }
        
        user.setAction("leave");
        messagingTemplate.convertAndSend("/topic/document/" + documentId + "/presence", user);
    }

    /**
     * 编辑文档内容
     * 接收用户的编辑操作并广播给其他在线用户
     * 
     * @param message 编辑消息，包含文档ID、用户ID、编辑类型、内容等
     */
    @MessageMapping("/document/edit")
    public void editDocument(@Payload DocumentEditMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/document/" + message.getDocumentId() + "/changes", message);
    }

    /**
     * 更新光标位置
     * 同步用户的光标位置给其他在线用户
     * 
     * @param user 用户状态信息，包含文档ID、用户ID、光标位置
     */
    @MessageMapping("/document/cursor")
    public void updateCursor(@Payload UserPresence user) {
        String documentId = user.getDocumentId();
        Map<String, UserPresence> users = documentUsers.get(documentId);
        if (users != null && users.containsKey(user.getUserId())) {
            users.put(user.getUserId(), user);
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/cursors", user);
        }
    }
}

