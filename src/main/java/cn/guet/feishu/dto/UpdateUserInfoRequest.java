package cn.guet.feishu.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String avatar;
}

