package cn.guet.feishu.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String userId;
    private String username;
    private String realName;
    private String role;
}

