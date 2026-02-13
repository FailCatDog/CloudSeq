package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequestDTO {
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String avatar;
}

