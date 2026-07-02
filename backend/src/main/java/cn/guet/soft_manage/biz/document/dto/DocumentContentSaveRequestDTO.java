package cn.guet.soft_manage.biz.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 保存 Markdown 正文请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentContentSaveRequestDTO {

    private String contentMd;

    private Long version;
}
