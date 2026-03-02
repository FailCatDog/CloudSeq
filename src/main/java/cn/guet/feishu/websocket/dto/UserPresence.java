package cn.guet.feishu.websocket.dto;

import lombok.Data;

@Data
public class UserPresence {
    private String documentId;
    private String userId;
    private String username;
    private String action;
    private Integer cursorPosition;
}

