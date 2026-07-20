# 工作区节点导出（DOCX / PDF / XLSX）设计

> 日期：2026-07-20  
> 状态：已确认  
> 方案：后端导出管线 + 策略注册；同步流式下载；预留异步 MinIO 投递

---

## 1. 背景与目标

项目空间已有富文本文档（TipTap + Markdown 落库）与协同表格（SpeedSheet JSON 落库），以及文档内图片资产（MinIO + `/api/assets/{id}` 代理）。用户需要将当前节点导出为可离线打开的办公文件。

### 1.1 目标

- **文档节点**导出为 **DOCX** 与 **PDF**，正文图片从 MinIO 内嵌
- **表格节点**导出为 **Excel（.xlsx）**
- **同步下载**：点击后立即获得文件流
- **可扩展**：新增格式或改为异步落桶时，不推翻主流程与前端调用约定

### 1.2 非目标（本期不做）

- 异步导出任务、导出历史、进度轮询
- 文件夹批量 ZIP
- 评论 / 批注导出
- 与编辑器像素级一致的排版
- 表格公式与复杂样式完整还原
- 文档导出为 xlsx、表格导出为 docx/pdf（类型不匹配直接拒绝）

---

## 2. 决策记录

| 决策 | 选择 | 说明 |
|------|------|------|
| 格式与节点映射 | 按类型自动匹配 | DOCUMENT → docx/pdf；SHEET → xlsx |
| 交付方式 | 同步流式下载 | 满足课程文档体量；通过 `ExportDelivery` 预留异步 |
| 图片 | 第一期内嵌 | 解析本工作区资产 ID，从 MinIO 取字节写入文件 |
| 架构 | 方案 1：Exporter 策略注册 | 单体后端实现，不引入 OnlyOffice / 独立导出服务 |

---

## 3. 架构

```
前端「导出」
    ↓
GET /api/nodes/{nodeId}/export?format=docx|pdf|xlsx
    ↓
ExportController
    ↓
ExportService
  1. 加载节点 + WorkspaceAccessService.requireCurrentAccess（只读即可）
  2. FormatRegistry：节点类型 × format → NodeExporter（不匹配 → 400）
  3. Exporter.render(ExportContext) → ExportArtifact(bytes, contentType, fileName)
  4. ExportDelivery.deliver(artifact)  ← 一期 SyncStream；二期可加 AsyncMinio
    ↓
Content-Disposition: attachment 流式返回
```

### 3.1 模块边界

建议落在 `biz/document` 旁新增 `biz/export`（或 `biz/workspace/export`），避免把 POI / PDF 依赖散落到现有 Document/Sheet Service。

| 组件 | 职责 |
|------|------|
| `ExportController` | HTTP 入参、流式响应头 |
| `ExportService` | 鉴权、选 Exporter、调用 Delivery |
| `FormatRegistry` | DOCUMENT/SHEET × format 合法组合 |
| `NodeExporter` | 各格式渲染实现 |
| `ExportAssetResolver` | Markdown 中的 `/api/assets/{id}` → 字节（防 SSRF） |
| `ExportDelivery` | 一期 `SyncStreamDelivery`；二期 `AsyncMinioDelivery` → `workspace-exports` |

### 3.2 格式注册表（写死）

| 节点类型 | 允许 `format` |
|----------|----------------|
| `DOCUMENT` | `docx`, `pdf` |
| `SHEET` | `xlsx` |

### 3.3 扩展约定

- 新增格式：实现 `NodeExporter` 并注册，不改 Controller 主流程
- 异步投递：新增 `AsyncMinioDelivery`；可选 `mode=async` 或同 URL 返回 `jobId`（本期不实现，仅保证接口可演进）
- `workspace-exports` 桶已在 `MinioConstants` 预留，一期**不写入**

---

## 4. API 契约

```
GET /api/nodes/{nodeId}/export?format={docx|pdf|xlsx}
Authorization: Bearer <token>
```

**成功：** `200`，body 为文件字节流

| Header | 示例 |
|--------|------|
| `Content-Type` | `application/vnd.openxmlformats-officedocument.wordprocessingml.document` / `application/pdf` / `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| `Content-Disposition` | `attachment; filename*=UTF-8''{节点标题}.{ext}` |

**错误：**

| 情况 | 行为 |
|------|------|
| format 与节点类型不符 | 400 |
| 无读权限 | 403 |
| 节点 / 正文不存在 | 404 |
| 超限（字数 / 单元格 / 内嵌图总大小） | 业务错误码 + 明确文案 |
| 单图资产缺失 | 占位或跳过该图，**不**导致整单失败 |

权限：走 `requireCurrentAccess`，组员与教师只读巡查均可导出；**不要求**写权限。

---

## 5. 格式实现

### 5.1 文档 → DOCX / PDF

**输入：** `WorkspaceContent.contentMd`（TipTap `getMarkdown()` 持久化）+ 节点标题 + 工作区资产。

**流水线：**

1. 解析 Markdown 结构（标题、段落、列表、表格、图片）
2. 图片 URL 仅允许本系统资产形态（如 `/api/assets/{id}`）；经 `ExportAssetResolver` 校验属于当前工作区后从 MinIO 读取
3. **DOCX**：Markdown → Apache POI `XWPF`（标题/段落/列表/表格 + 内嵌图）
4. **PDF**：Markdown → HTML（与 DOCX 同源结构映射）→ OpenHTMLToPDF（或等价 HTML→PDF）；图片以嵌入方式写入
5. 保真目标：结构正确、图片可见；不追求与编辑器像素一致

**依赖（后端）：** Apache POI；OpenHTMLToPDF（或等价）；轻量 Markdown 解析库。不引入 OnlyOffice。

### 5.2 表格 → XLSX

**输入：** `contentMd` 中的 SpeedSheet snapshot JSON（含 `sheets[].cells` 等）。

1. Apache POI 生成 workbook
2. 多 sheet 保留名称与顺序
3. 单元格以值为主写出；公式 / 复杂样式一期可降级为纯值
4. 不提供该节点的 docx/pdf

### 5.3 容量软上限（可配置）

| 维度 | 建议默认 |
|------|----------|
| 文档正文字符 | ~10 万 |
| 表格非空单元格 | ~5 万 |
| 单次内嵌图片总大小 | ~20MB |
| 同步接口超时 | 容器/网关建议 ≥ 60s |

超限返回业务错误，提示拆分文档或后续使用异步导出。

---

## 6. 前端

| 表面 | 入口 |
|------|------|
| 文档编辑页顶栏 | 「导出」下拉：Word (.docx) / PDF |
| 表格编辑页顶栏 | 「导出」：Excel (.xlsx) |
| 只读巡查模式 | 同样可用 |

行为：`fetch` 导出 URL（带 Authorization）→ Blob → 触发浏览器下载；失败用现有 toast / prompt。下载文件名优先使用节点标题。

---

## 7. 安全

- 导出鉴权与工作区读权限一致
- 图片解析禁止任意外链 URL（防 SSRF）；只解析本工作区资产 ID
- 一期响应不持久化导出副本；不写 `workspace-exports`

---

## 8. 测试与验收

### 8.1 测试要点

- 文档：纯文字 DOCX/PDF；含图；缺图不整单失败
- 表格：单 sheet / 多 sheet → xlsx 可被 Excel / WPS 打开
- 负例：SHEET + `format=docx`、DOCUMENT + `format=xlsx` → 400；无权限 → 403
- 前端：两种节点菜单项正确，文件名含节点标题

### 8.2 验收标准

1. 文档页可导出可打开的 DOCX、PDF，正文图内嵌  
2. 表格页可导出可打开的 xlsx  
3. 格式与节点不匹配被拒绝  
4. 有读权限的教师 / 学生都能导出  
5. 新增 format 或 `AsyncMinioDelivery` 时，不改 `ExportService` 主流程骨架  

---

## 9. 后续演进（非本期）

1. `AsyncMinioDelivery` + 可选任务表 / 短时下载链  
2. 文件夹批量 ZIP  
3. 表格样式与公式增强  
4. PDF 中文字体包与打印页边距精细化  

---

## 10. 相关现有代码

| 能力 | 位置 |
|------|------|
| 文档 Markdown 落库 | `WorkspaceContent.contentMd`；`ProjectDocEditor` `getMarkdown()` |
| 表格 JSON 落库 | Sheet 将 snapshot 写入同一 `contentMd` 字段 |
| 资产读取 | `AssetProxyController`、`DocumentAssetService` |
| 导出桶预留 | `MinioConstants.BUCKET_WORKSPACE_EXPORTS` |
| 读权限 | `WorkspaceAccessService.requireCurrentAccess` |
| 设计原则 | `docs/文档内容-设计原则.md`（`content_md` 用于导出） |
