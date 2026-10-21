# 导出资源说明

## 中文字体（PDF）

PDF 导出（`PdfNodeExporter`）在 classpath 上发现以下字体时会自动注册，用于中文渲染：

```
classpath:fonts/NotoSansSC-Regular.otf
```

### 放置方式

将 `NotoSansSC-Regular.otf` 放到：

```
backend/src/main/resources/fonts/NotoSansSC-Regular.otf
```

字体文件体积较大，**请勿默认提交到仓库**，除非团队明确提供并同意纳入版本控制。未放置字体时，PDF 仍可导出，但中文可能显示为缺字/方框。

### 获取字体

可从 [Google Fonts — Noto Sans SC](https://fonts.google.com/noto/specimen/Noto+Sans+SC) 下载 OTF/TTF，并按上述文件名放入 `resources/fonts/`。
