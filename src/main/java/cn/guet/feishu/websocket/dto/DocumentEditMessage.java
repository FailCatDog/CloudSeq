package cn.guet.feishu.websocket.dto;

import lombok.Data;

@Data
public class DocumentEditMessage {
    private String type;
    private String documentId;
    private String userId;
    private String username;
    private String content;
    private Integer cursorPosition;
    private Integer selectionStart;
    private Integer selectionEnd;
    private Long timestamp;
}

