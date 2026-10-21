# Workspace Node Export (DOCX / PDF / XLSX) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users download a workspace DOCUMENT as DOCX/PDF (with embedded MinIO images) and a SHEET as XLSX via a sync streaming API and editor toolbar actions.

**Architecture:** New `biz/export` module: `ExportService` loads node + content, checks `requireCurrentAccess`, resolves a `NodeExporter` from `FormatRegistry`, renders an `ExportArtifact`, and returns it through `SyncStreamDelivery`. Image URLs of the form `/api/assets/{id}` are resolved only within the current workspace (SSRF-safe). Async MinIO delivery is an interface extension only—not implemented this phase.

**Tech Stack:** Spring Boot 3.4 / Java 17, Apache POI 5.x (DOCX + XLSX), flexmark-java (Markdown AST), openhtmltopdf (HTML→PDF), Vue 3 frontend (`fetch` + Blob download).

**Spec:** `docs/superpowers/specs/2026-07-20-workspace-export-design.md`

## Global Constraints

- DOCUMENT → `docx` | `pdf` only; SHEET → `xlsx` only; mismatch → `BizResponseCode.EXPORT_FORMAT_INVALID`.
- Read access only: `WorkspaceAccessService.requireCurrentAccess` — no write required.
- Images: only `/api/assets/{assetId}` (optional origin prefix); missing asset → skip/placeholder, do not fail whole export.
- Soft limits (configurable): markdown chars ≤ 100_000; sheet non-empty cells ≤ 50_000; embedded image bytes ≤ 20MB total.
- Do not write `workspace-exports` this phase; keep `ExportDelivery` interface for future `AsyncMinioDelivery`.
- Do not introduce OnlyOffice.
- Follow existing package style under `cn.guet.soft_manage.biz.*`; error codes in `BizResponseCode` 5xxxx range.
- Repo currently has **no** backend unit tests — create `src/test/java` tests with JUnit 5 + Mockito (via `spring-boot-starter-test`). Prefer pure unit tests over `@SpringBootTest` for exporters.

---

## File Structure

| Path | Responsibility |
|------|----------------|
| `backend/pom.xml` | Add POI, flexmark, openhtmltopdf deps |
| `backend/.../frame/config/ExportProperties.java` | Soft-limit config `export.*` |
| `backend/.../frame/enums/BizResponseCode.java` | New export error codes |
| `backend/.../biz/export/dto/ExportFormat.java` | Enum `DOCX/PDF/XLSX` |
| `backend/.../biz/export/dto/ExportArtifact.java` | bytes + contentType + fileName |
| `backend/.../biz/export/dto/ExportContext.java` | node, contentMd, workspaceId, title, assetResolver |
| `backend/.../biz/export/dto/ResolvedExportAsset.java` | bytes + contentType + fileName |
| `backend/.../biz/export/exporter/NodeExporter.java` | `supports` + `export` |
| `backend/.../biz/export/exporter/FormatRegistry.java` | Map nodeType×format → exporter |
| `backend/.../biz/export/asset/ExportAssetResolver.java` | Interface |
| `backend/.../biz/export/asset/ExportAssetResolverImpl.java` | Parse URL + load MinIO via DAO/storage |
| `backend/.../biz/export/markdown/MarkdownBlock.java` | Block model |
| `backend/.../biz/export/markdown/MarkdownDocumentParser.java` | flexmark → blocks |
| `backend/.../biz/export/exporter/DocxNodeExporter.java` | DOCUMENT + docx |
| `backend/.../biz/export/exporter/PdfNodeExporter.java` | DOCUMENT + pdf |
| `backend/.../biz/export/exporter/XlsxNodeExporter.java` | SHEET + xlsx |
| `backend/.../biz/export/delivery/ExportDelivery.java` | Interface |
| `backend/.../biz/export/delivery/SyncStreamDelivery.java` | Pass-through artifact |
| `backend/.../biz/export/service/ExportService.java` | Interface |
| `backend/.../biz/export/service/impl/ExportServiceImpl.java` | Orchestration |
| `backend/.../biz/export/controller/ExportController.java` | `GET /api/nodes/{nodeId}/export` |
| `backend/.../biz/export/util/ExportFileNames.java` | Sanitize download file name |
| `backend/src/test/java/.../biz/export/*Test.java` | Unit tests per task |
| `frontend/src/api/export.js` | Binary download API |
| `frontend/src/utils/downloadBlob.js` | Trigger browser save |
| `frontend/.../ProjectDocEditor.vue` | Export dropdown |
| `frontend/.../ProjectSheetEditor.vue` | Export Excel button |

---

### Task 1: Dependencies, error codes, config, core types

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/java/cn/guet/soft_manage/frame/enums/BizResponseCode.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/frame/config/ExportProperties.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/dto/ExportFormat.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/dto/ExportArtifact.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/dto/ExportContext.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/dto/ResolvedExportAsset.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/NodeExporter.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/asset/ExportAssetResolver.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/delivery/ExportDelivery.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/util/ExportFileNames.java`
- Modify: `backend/src/main/resources/application.yml`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/util/ExportFileNamesTest.java`

**Interfaces:**
- Consumes: none
- Produces:
  - `ExportFormat.fromParam(String)` → enum or null
  - `ExportArtifact(byte[] bytes, String contentType, String fileName)`
  - `ExportContext`: `Long nodeId`, `Long workspaceId`, `String nodeType`, `String title`, `String contentMd`, `ExportAssetResolver assetResolver`
  - `NodeExporter`: `boolean supports(String nodeType, ExportFormat format); ExportArtifact export(ExportContext ctx);`
  - `ExportDelivery`: `ExportArtifact deliver(ExportArtifact artifact);`
  - `ExportAssetResolver`: `ResolvedExportAsset resolve(Long workspaceId, String imageUrl);`
  - `ExportFileNames.build(String title, String extension)` → sanitized `title.ext`

- [ ] **Step 1: Write failing test for file name sanitization**

Create `ExportFileNamesTest.java`:

```java
package cn.guet.soft_manage.biz.export.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExportFileNamesTest {
    @Test
    void stripsPathSeparatorsAndKeepsExtension() {
        assertEquals("报告.docx", ExportFileNames.build("a/../报告", "docx"));
    }

    @Test
    void blankTitleFallsBackToNode() {
        assertEquals("node.pdf", ExportFileNames.build("  ", "pdf"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd backend && mvn -q -Dtest=ExportFileNamesTest test`  
Expected: FAIL (class not found)

- [ ] **Step 3: Add Maven dependencies**

In `backend/pom.xml` `<properties>` add:

```xml
<poi.version>5.3.0</poi.version>
<flexmark.version>0.64.8</flexmark.version>
<openhtmltopdf.version>1.0.10</openhtmltopdf.version>
```

In `<dependencies>` add:

```xml
<dependency>
  <groupId>org.apache.poi</groupId>
  <artifactId>poi-ooxml</artifactId>
  <version>${poi.version}</version>
</dependency>
<dependency>
  <groupId>com.vladsch.flexmark</groupId>
  <artifactId>flexmark-all</artifactId>
  <version>${flexmark.version}</version>
</dependency>
<dependency>
  <groupId>com.openhtmltopdf</groupId>
  <artifactId>openhtmltopdf-pdfbox</artifactId>
  <version>${openhtmltopdf.version}</version>
</dependency>
```

- [ ] **Step 4: Add BizResponseCode entries**

Before `APPROVAL_NOT_FOUND`, add:

```java
EXPORT_FORMAT_INVALID(50045, "导出格式无效或不支持该节点类型"),
EXPORT_CONTENT_TOO_LARGE(50046, "内容超过导出上限，请拆分后重试"),
EXPORT_IMAGES_TOO_LARGE(50047, "内嵌图片总大小超过导出上限"),
EXPORT_RENDER_FAILED(50048, "导出文件生成失败"),
```

- [ ] **Step 5: Implement config + DTOs + interfaces + ExportFileNames**

`ExportProperties` (`@ConfigurationProperties(prefix = "export")`):

```java
private int maxDocumentChars = 100_000;
private int maxSheetCells = 50_000;
private long maxEmbeddedImageBytes = 20L * 1024 * 1024;
```

`application.yml`:

```yaml
export:
  max-document-chars: 100000
  max-sheet-cells: 50000
  max-embedded-image-bytes: 20971520
```

`ExportFormat` enum with `param` + `contentType`; `fromParam(String raw)` case-insensitive, null if unknown.

`ExportFileNames.build`: trim, replace `[\\\\/:*?\"<>|]` and path segments with `_`, if blank use `node`, append `.` + extension.

Implement DTO builders (Lombok `@Data` / `@Builder` consistent with project) and empty interfaces listed above.

- [ ] **Step 6: Run test to verify it passes**

Run: `cd backend && mvn -q -Dtest=ExportFileNamesTest test`  
Expected: PASS

- [ ] **Step 7: Commit**

```bash
git add backend/pom.xml backend/src/main/resources/application.yml \
  backend/src/main/java/cn/guet/soft_manage/frame/enums/BizResponseCode.java \
  backend/src/main/java/cn/guet/soft_manage/frame/config/ExportProperties.java \
  backend/src/main/java/cn/guet/soft_manage/biz/export \
  backend/src/test/java/cn/guet/soft_manage/biz/export/util/ExportFileNamesTest.java
git commit -m "feat(export): add deps, error codes, and core export types"
```

---

### Task 2: FormatRegistry

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/FormatRegistry.java`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/FormatRegistryTest.java`

**Interfaces:**
- Consumes: `NodeExporter`, `ExportFormat`
- Produces: `FormatRegistry.resolve(String nodeType, ExportFormat format)` → `NodeExporter` or throws `BusinessException(EXPORT_FORMAT_INVALID)`

- [ ] **Step 1: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class FormatRegistryTest {
    @Mock NodeExporter docx;
    @Mock NodeExporter pdf;
    @Mock NodeExporter xlsx;

    @BeforeEach
    void stub() {
        when(docx.supports("DOCUMENT", ExportFormat.DOCX)).thenReturn(true);
        when(pdf.supports("DOCUMENT", ExportFormat.PDF)).thenReturn(true);
        when(xlsx.supports("SHEET", ExportFormat.XLSX)).thenReturn(true);
    }

    @Test
    void resolvesDocumentDocx() {
        FormatRegistry registry = new FormatRegistry(List.of(docx, pdf, xlsx));
        assertSame(docx, registry.resolve("DOCUMENT", ExportFormat.DOCX));
    }

    @Test
    void rejectsSheetDocx() {
        FormatRegistry registry = new FormatRegistry(List.of(docx, pdf, xlsx));
        BusinessException ex = assertThrows(BusinessException.class,
            () -> registry.resolve("SHEET", ExportFormat.DOCX));
        assertEquals(BizResponseCode.EXPORT_FORMAT_INVALID.getCode(), ex.getCode());
    }
}
```

Adapt `ex.getCode()` to whatever `BusinessException` exposes (check existing class).

- [ ] **Step 2: Run test — expect FAIL**

`mvn -q -Dtest=FormatRegistryTest test`

- [ ] **Step 3: Implement FormatRegistry**

```java
@Component
public class FormatRegistry {
    private final List<NodeExporter> exporters;

    public FormatRegistry(List<NodeExporter> exporters) {
        this.exporters = exporters == null ? List.of() : List.copyOf(exporters);
    }

    public NodeExporter resolve(String nodeType, ExportFormat format) {
        if (nodeType == null || format == null) {
            throw new BusinessException(BizResponseCode.EXPORT_FORMAT_INVALID);
        }
        return exporters.stream()
            .filter(e -> e.supports(nodeType, format))
            .findFirst()
            .orElseThrow(() -> new BusinessException(BizResponseCode.EXPORT_FORMAT_INVALID));
    }
}
```

- [ ] **Step 4: Run tests — expect PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/FormatRegistry.java \
  backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/FormatRegistryTest.java
git commit -m "feat(export): add FormatRegistry for node type x format"
```

---

### Task 3: ExportAssetResolver (SSRF-safe image load)

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/asset/ExportAssetResolverImpl.java`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/asset/ExportAssetResolverImplTest.java`

**Interfaces:**
- Consumes: `DocumentAssetUrlHelper.ASSET_API_PREFIX`, `WorkspaceAssetDao`, `ObjectStorageService`, `MinioConstants.BUCKET_WORKSPACE_ASSETS`
- Produces: `resolve(workspaceId, url)` → `ResolvedExportAsset` or `null` if missing/invalid/foreign workspace

- [ ] **Step 1: Write failing tests**

```java
@ExtendWith(MockitoExtension.class)
class ExportAssetResolverImplTest {
    @Mock WorkspaceAssetDao assetDao;
    @Mock ObjectStorageService storage;
    ExportAssetResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExportAssetResolverImpl(assetDao, storage);
    }

    @Test
    void parsesRelativeAssetUrl() throws Exception {
        WorkspaceAsset asset = WorkspaceAsset.builder()
            .id(9L).workspaceId(1L).storageKey("k").contentType("image/png")
            .originalName("a.png").sizeBytes(3L).build();
        when(assetDao.selectById(9L)).thenReturn(asset);
        when(storage.getObject(eq(MinioConstants.BUCKET_WORKSPACE_ASSETS), eq("k")))
            .thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));

        ResolvedExportAsset got = resolver.resolve(1L, "/api/assets/9");
        assertNotNull(got);
        assertEquals(3, got.getBytes().length);
    }

    @Test
    void rejectsExternalUrl() {
        assertNull(resolver.resolve(1L, "https://evil.example/x.png"));
    }

    @Test
    void rejectsOtherWorkspace() {
        WorkspaceAsset asset = WorkspaceAsset.builder()
            .id(9L).workspaceId(2L).storageKey("k").build();
        when(assetDao.selectById(9L)).thenReturn(asset);
        assertNull(resolver.resolve(1L, "/api/assets/9"));
    }
}
```

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement resolver**

Accept:
- `/api/assets/{id}`
- `http(s)://anything/api/assets/{id}` — extract id only; **never** HTTP-fetch the URL

On DAO miss / storage IO / wrong workspace → return `null`.

- [ ] **Step 4: Run — expect PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/asset/ExportAssetResolverImpl.java \
  backend/src/test/java/cn/guet/soft_manage/biz/export/asset/ExportAssetResolverImplTest.java
git commit -m "feat(export): resolve workspace-scoped asset images for embed"
```

---

### Task 4: Markdown parser shared by DOCX/PDF

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/markdown/MarkdownBlock.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/markdown/MarkdownDocumentParser.java`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/markdown/MarkdownDocumentParserTest.java`

**Interfaces:**
- Consumes: flexmark (+ Tables extension)
- Produces: `List<MarkdownBlock>` with types: `HEADING`, `PARAGRAPH`, `BULLET_ITEM`, `ORDERED_ITEM`, `TABLE`, `IMAGE` (paragraph that contains only an image → IMAGE block)

- [ ] **Step 1: Write failing parser tests**

```java
@Test
void parsesHeadingParagraphAndImage() {
    var blocks = MarkdownDocumentParser.parse(
        "# Title\n\nHello\n\n![alt](/api/assets/12)\n");
    assertEquals(MarkdownBlock.Type.HEADING, blocks.get(0).getType());
    assertEquals(1, blocks.get(0).getLevel());
    assertEquals(MarkdownBlock.Type.PARAGRAPH, blocks.get(1).getType());
    assertEquals(MarkdownBlock.Type.IMAGE, blocks.get(2).getType());
    assertEquals("/api/assets/12", blocks.get(2).getUrl());
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement parser with flexmark**

Use `Parser.builder(TablesExtension.create()).build()` (correct flexmark API for enabling tables) and walk AST nodes into `MarkdownBlock` list.

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/markdown \
  backend/src/test/java/cn/guet/soft_manage/biz/export/markdown
git commit -m "feat(export): parse markdown into export blocks"
```

---

### Task 5: DocxNodeExporter

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/DocxNodeExporter.java`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/DocxNodeExporterTest.java`

**Interfaces:**
- Consumes: `MarkdownDocumentParser`, `ExportAssetResolver`, `ExportProperties`, `ExportFileNames`, `ExportFormat.DOCX`
- Produces: `@Component` `supports("DOCUMENT", DOCX)`

- [ ] **Step 1: Write failing test**

```java
@Test
void exportsMinimalDocxWithMagicBytes() {
    ExportAssetResolver resolver = (ws, url) -> null;
    DocxNodeExporter exporter = new DocxNodeExporter(new ExportProperties(), resolver);
    ExportContext ctx = ExportContext.builder()
        .nodeId(1L).workspaceId(1L).nodeType("DOCUMENT")
        .title("演示")
        .contentMd("# Hello\n\nWorld")
        .assetResolver(resolver)
        .build();
    ExportArtifact art = exporter.export(ctx);
    assertTrue(art.getBytes().length > 100);
    assertEquals('P', art.getBytes()[0]);
    assertEquals('K', art.getBytes()[1]);
    assertTrue(art.getFileName().endsWith(".docx"));
}
```

Also add a test that embeds a tiny PNG when resolver returns bytes for `/api/assets/1`.

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement DocxNodeExporter**

1. If `contentMd` code-point/char count > `maxDocumentChars` → `EXPORT_CONTENT_TOO_LARGE`
2. Parse blocks → `XWPFDocument`
3. Images via `XWPFRun.addPicture`; track cumulative bytes; exceeding `maxEmbeddedImageBytes` → `EXPORT_IMAGES_TOO_LARGE`
4. Missing resolve → italic placeholder `[图片不可用]`
5. Return artifact with DOCX content type + `ExportFileNames.build(title, "docx")`

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/DocxNodeExporter.java \
  backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/DocxNodeExporterTest.java
git commit -m "feat(export): export DOCUMENT nodes to DOCX"
```

---

### Task 6: PdfNodeExporter

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/PdfNodeExporter.java`
- Create: `backend/src/main/resources/export/README.md` (Chinese font drop-in instructions)
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/PdfNodeExporterTest.java`

**Interfaces:**
- Consumes: same parser + resolver; HTML bridge; openhtmltopdf
- Produces: `supports("DOCUMENT", PDF)`

- [ ] **Step 1: Write failing test**

```java
@Test
void exportsPdfHeader() {
    PdfNodeExporter exporter = new PdfNodeExporter(new ExportProperties(), (w, u) -> null);
    ExportArtifact art = exporter.export(ExportContext.builder()
        .workspaceId(1L).nodeType("DOCUMENT").title("t")
        .contentMd("# Hi\n\nBody")
        .assetResolver((w, u) -> null)
        .build());
    assertTrue(new String(art.getBytes(), 0, 5, StandardCharsets.US_ASCII).startsWith("%PDF"));
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement HTML bridge + PDF**

- Escape text; images as `<img src="data:{contentType};base64,..."/>` when resolved
- Wrap HTML document with basic CSS `img{max-width:100%;}`
- `PdfRendererBuilder` → bytes; on failure → `EXPORT_RENDER_FAILED`
- Same size limits as DOCX
- If `classpath:fonts/NotoSansSC-Regular.otf` exists, register with builder; do **not** commit a large font unless provided — document path in `export/README.md`

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/PdfNodeExporter.java \
  backend/src/main/resources/export/README.md \
  backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/PdfNodeExporterTest.java
git commit -m "feat(export): export DOCUMENT nodes to PDF"
```

---

### Task 7: XlsxNodeExporter

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/XlsxNodeExporter.java`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/XlsxNodeExporterTest.java`
- Test fixture: `backend/src/test/resources/export/sheet-snapshot-sample.json` (capture from real SpeedSheet `toSnapshot()`)

**Interfaces:**
- Consumes: Jackson `ObjectMapper`; snapshot JSON with root `sheets[]` (as used by `SheetContentMetadataUtil`)
- Produces: `supports("SHEET", XLSX)`

**Cell handling (v1):**
1. Capture one real snapshot (DB `content_md` or network) and save as fixture
2. Support the real shape; also tolerate Luckysheet-like `celldata: [{r,c,v:{v,m}}]` if present in fixture variants
3. Write display values only; multi-sheet preserved; blank name → `Sheet{i}`
4. Non-empty cell count > `maxSheetCells` → `EXPORT_CONTENT_TOO_LARGE`

- [ ] **Step 1: Write failing test with fixture JSON**

Assert OOXML magic `PK`, `.xlsx` file name, and overflow throws.

- [ ] **Step 2: Capture real snapshot and lock parser to that shape**

- [ ] **Step 3: Implement exporter with Apache POI `XSSFWorkbook`**

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/exporter/XlsxNodeExporter.java \
  backend/src/test/java/cn/guet/soft_manage/biz/export/exporter/XlsxNodeExporterTest.java \
  backend/src/test/resources/export/sheet-snapshot-sample.json
git commit -m "feat(export): export SHEET nodes to XLSX"
```

---

### Task 8: ExportService + SyncStreamDelivery + Controller

**Files:**
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/delivery/SyncStreamDelivery.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/service/ExportService.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/service/impl/ExportServiceImpl.java`
- Create: `backend/src/main/java/cn/guet/soft_manage/biz/export/controller/ExportController.java`
- Test: `backend/src/test/java/cn/guet/soft_manage/biz/export/service/ExportServiceImplTest.java`

**Interfaces:**
- Consumes: `WorkspaceNodeDao`, `WorkspaceContentDao`, `WorkspaceAccessService`, `FormatRegistry`, `ExportDelivery`, `ExportAssetResolver`
- Produces: `ExportService.export(Long nodeId, String formatParam): ExportArtifact`

- [ ] **Step 1: Write ExportServiceImplTest with mocks**

Cases: happy DOCUMENT+docx; unknown format; node missing; content missing; verify `requireCurrentAccess` called.

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement service + controller**

Flow:
1. `ExportFormat.fromParam` — null → `EXPORT_FORMAT_INVALID`
2. Load node — null → `NODE_NOT_FOUND`
3. `requireCurrentAccess(node.workspaceId)`
4. Load content by nodeId — null → `DOCUMENT_CONTENT_NOT_FOUND`
5. Build context → `registry.resolve` → `exporter.export` → `delivery.deliver`

Controller:

```java
@RestController
@RequestMapping("/api/nodes")
public class ExportController {
    @GetMapping("/{nodeId}/export")
    public ResponseEntity<byte[]> export(
            @PathVariable Long nodeId,
            @RequestParam String format) {
        ExportArtifact artifact = exportService.export(nodeId, format);
        ContentDisposition disposition = ContentDisposition.attachment()
            .filename(artifact.getFileName(), StandardCharsets.UTF_8)
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .contentType(MediaType.parseMediaType(artifact.getContentType()))
            .body(artifact.getBytes());
    }
}
```

`SyncStreamDelivery.deliver` returns the same artifact unchanged.

**RBAC:** no menu seed required; unmatched API paths pass `PermissionInterceptor`. Workspace ACL is authoritative.

- [ ] **Step 4: Run unit tests — PASS**

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/guet/soft_manage/biz/export/delivery \
  backend/src/main/java/cn/guet/soft_manage/biz/export/service \
  backend/src/main/java/cn/guet/soft_manage/biz/export/controller \
  backend/src/test/java/cn/guet/soft_manage/biz/export/service
git commit -m "feat(export): wire export service API with sync delivery"
```

---

### Task 9: Frontend download API + editor actions

**Files:**
- Create: `frontend/src/api/export.js`
- Create: `frontend/src/utils/downloadBlob.js`
- Modify: `frontend/src/views/student/project/components/ProjectDocEditor.vue`
- Modify: `frontend/src/views/student/project/components/ProjectSheetEditor.vue`
- Modify: parent pages that render editors if they must pass `nodeId` / already pass `sheetId`

**Interfaces:**
- Consumes: auth token storage key `authorization` (same as `request.js`)
- Produces: `downloadNodeExportApi(nodeId, format)` — triggers browser download; throws `Error` on failure

- [ ] **Step 1: Implement `downloadBlob.js`**

```js
export function triggerBlobDownload(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName || 'download'
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export function fileNameFromContentDisposition(header, fallback) {
  if (!header) return fallback
  const utf8 = /filename\*=UTF-8''([^;]+)/i.exec(header)
  if (utf8?.[1]) return decodeURIComponent(utf8[1])
  const plain = /filename="?([^";]+)"?/i.exec(header)
  return plain?.[1] || fallback
}
```

- [ ] **Step 2: Implement `export.js`**

Do **not** use `request()` (it always parses JSON). Attach Bearer token manually; if response is JSON error, throw `data.message`; else blob + `triggerBlobDownload`.

- [ ] **Step 3: Document editor UI**

Add 「导出」 dropdown (Word / PDF) in article head meta row. Ensure editor receives `nodeId` prop (add + wire from `doc/index.vue` if missing). Show in readOnly too. On error, set a visible notice string (reuse notice pattern near `uploadMessage`).

- [ ] **Step 4: Sheet editor UI**

Button 「导出 Excel」 → `downloadNodeExportApi(props.sheetId, 'xlsx')`.

- [ ] **Step 5: Manual verify**

1. Document with image → DOCX + PDF open; image present  
2. Sheet → xlsx opens in Excel/WPS  
3. Wrong format via curl returns business error  

- [ ] **Step 6: Commit**

```bash
git add frontend/src/api/export.js frontend/src/utils/downloadBlob.js \
  frontend/src/views/student/project/components/ProjectDocEditor.vue \
  frontend/src/views/student/project/components/ProjectSheetEditor.vue \
  frontend/src/views/student/project/doc/index.vue
git commit -m "feat(export): add editor export actions for docx/pdf/xlsx"
```

---

### Task 10: Limit / mismatch regression tests

**Files:**
- Modify tests under `backend/src/test/java/cn/guet/soft_manage/biz/export/`

- [ ] **Step 1: Ensure coverage for**

- Document char overflow → `EXPORT_CONTENT_TOO_LARGE`  
- Image byte overflow → `EXPORT_IMAGES_TOO_LARGE`  
- SHEET + `docx` via service/registry → `EXPORT_FORMAT_INVALID`

- [ ] **Step 2: Run full export suite**

```bash
cd backend && mvn -q -Dtest=cn.guet.soft_manage.biz.export.**.*Test test
```

Expected: all PASS

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/cn/guet/soft_manage/biz/export
git commit -m "test(export): cover size limits and format mismatch cases"
```

---

## Spec coverage checklist

| Spec requirement | Task |
|------------------|------|
| DOCUMENT → docx/pdf | 5, 6, 8, 9 |
| SHEET → xlsx | 7, 8, 9 |
| Sync stream download | 8 |
| Image embed from MinIO | 3, 5, 6 |
| SSRF-safe asset URLs only | 3 |
| Missing image non-fatal | 5, 6 |
| Format mismatch error | 2, 8 |
| Read-only access | 8 |
| Soft limits | 1, 5, 6, 7, 10 |
| `ExportDelivery` extension point | 1, 8 |
| No `workspace-exports` write | 8 |
| Frontend toolbar | 9 |
| No OnlyOffice | Global |

## Self-review notes

- Types consistent across tasks: `ExportFormat`, `ExportArtifact`, `ExportContext`, `NodeExporter`, `ExportDelivery`, `ExportAssetResolver`, `ResolvedExportAsset`
- API fixed: `GET /api/nodes/{nodeId}/export?format=`
- Task 7 requires a real SpeedSheet snapshot fixture before locking cell parsing — no TBD left in final code

---

## Execution handoff

Plan complete and saved to `docs/superpowers/plans/2026-07-20-workspace-export.md`.

**Two execution options:**

1. **Subagent-Driven (recommended)** — fresh subagent per task, review between tasks  
2. **Inline Execution** — execute in this session with executing-plans checkpoints  

Which approach?
