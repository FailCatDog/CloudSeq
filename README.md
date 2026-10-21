# 云序 CloudSeq

云序（CloudSeq）是面向软件项目课程的团队协作工作台，围绕课程教学周期连接学生、教师与管理员，帮助团队有序完成组队选题、项目协作、进度跟踪和周报管理。

## 项目功能

- **学生工作台**：加入课程、团队协作、提交选题、管理项目任务与甘特进度、协作文档和表格、提交周报。
- **教师教学端**：管理课程与课号、审核团队选题、查看学生团队、跟踪项目进度并评阅周报。
- **平台管理端**：管理用户、角色、菜单和字典配置。
- **实时协作**：基于独立协作服务支持文档协同编辑；项目资料支持对象存储。
- **成果导出**：支持项目内容导出为 DOCX、PDF 与 XLSX。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、Vue Router、Naive UI、Ant Design Vue、Tiptap、Yjs |
| 后端 | Java 17、Spring Boot 3.4、MyBatis-Plus、MySQL、Redis、Redisson |
| 协作服务 | Node.js、Hocuspocus、Yjs、JSON Web Token |
| 文件存储 | MinIO |

## 目录结构

```text
CloudSeq/
├── backend/        # Spring Boot 后端及测试
├── frontend/       # Vue 3 前端
├── collab-server/  # 实时协作 WebSocket 服务
├── docs/           # 设计说明与开发计划
├── DESIGN.md       # 设计规范
└── PRODUCT.md      # 产品说明
```

## 环境要求

- Java 17 或更高版本
- Maven 3.8+
- Node.js 20.19+ 或 22.12+（满足 Vite 8 的运行要求）
- npm
- MySQL、Redis，以及 MinIO（文件上传和对象存储功能需要）

## 配置说明

首次启动前，请分别检查 `backend/src/main/resources/application.yml`、`application-dev.yml`、`application-prod.yml`，以及 `collab-server` 的配置，并按本地环境配置数据库、Redis、MinIO、服务地址和协作服务密钥。请使用环境变量或本地未提交配置提供真实凭证，不要将密码、JWT 密钥或内部服务密钥提交到代码仓库。

后端默认使用 `dev` Spring Profile，HTTP 端口为 `19001`。前端开发服务器使用 Vite，协作服务端口和前端代理配置请以 `frontend/vite.config.js`、`collab-server/src/config.js` 及后端配置为准。

## 本地开发

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

运行测试：

```bash
cd backend
mvn test
```

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

构建前端：

```bash
cd frontend
npm run build
```

### 启动实时协作服务

```bash
cd collab-server
npm install
npm run dev
```

启动前确保后端、Redis、数据库及所需对象存储服务已完成配置。前后端 API 与 WebSocket 地址应与各自配置保持一致。

## 测试

- 后端测试使用 Maven：`cd backend && mvn test`
- 前端当前包含基于 Node.js 内置测试框架编写的测试文件；运行构建验证：`cd frontend && npm run build`

## 相关文档

- [产品说明](PRODUCT.md)
- [界面设计规范](DESIGN.md)
- [导出功能说明](backend/src/main/resources/export/README.md)
- [GitHub 仓库](https://github.com/FailCatDog/CloudSeq)

## 许可证

本项目采用仓库中的 [LICENSE](LICENSE) 许可协议。
