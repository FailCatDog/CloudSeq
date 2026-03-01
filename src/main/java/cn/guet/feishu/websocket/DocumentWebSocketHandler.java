package cn.guet.feishu.websocket;

import cn.guet.feishu.websocket.dto.DocumentEditMessage;
import cn.guet.feishu.websocket.dto.UserPresence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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

    @MessageMapping("/document/{documentId}/join")
    public void joinDocument(@DestinationVariable String documentId, @Payload UserPresence user) {
        documentUsers.computeIfAbsent(documentId, k -> new ConcurrentHashMap<>())
                .put(user.getUserId(), user);
        
        user.setAction("join");
        messagingTemplate.convertAndSend("/topic/document/" + documentId + "/presence", user);
        
        messagingTemplate.convertAndSend("/topic/document/" + documentId + "/users", 
                documentUsers.get(documentId).values());
    }

    @MessageMapping("/document/{documentId}/leave")
    public void leaveDocument(@DestinationVariable String documentId, @Payload UserPresence user) {
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

    @MessageMapping("/document/{documentId}/edit")
    public void editDocument(@DestinationVariable String documentId, @Payload DocumentEditMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/document/" + documentId + "/changes", message);
    }

    @MessageMapping("/document/{documentId}/cursor")
    public void updateCursor(@DestinationVariable String documentId, @Payload UserPresence user) {
        Map<String, UserPresence> users = documentUsers.get(documentId);
        if (users != null && users.containsKey(user.getUserId())) {
            users.put(user.getUserId(), user);
            messagingTemplate.convertAndSend("/topic/document/" + documentId + "/cursors", user);
        }
    }
}

