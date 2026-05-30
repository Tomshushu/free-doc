[**English**](README.md) | **中文** | [**日本語**](README_ja.md)

<div align="center">

# FreeDoc

**轻量级文档协作平台**

[![License](https://img.shields.io/badge/License-MIT-informational.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://hub.docker.com/r/tomshushu/free-doc)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Vue 3](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org/)

[功能特性](#功能特性) · [快速启动](#快速启动) · [配置说明](#配置说明) · [开发指南](#开发指南) · [贡献指南](#贡献指南)

</div>

---

## 功能特性

- 📝 **Markdown 编辑器** — 实时预览、语法高亮、扩展工具栏、可拖拽分栏
- 👥 **团队协作** — 创建团队，邀请成员，按角色分配读写权限
- 📁 **项目组织** — 目录树管理文档，支持拖拽排序、重命名
- 🔗 **文档分享** — 生成分享链接，可设置密码和有效期
- 📜 **版本历史** — 自动版本追踪，支持一键回滚
- 📤 **多格式导出** — 支持 Markdown、HTML、PDF、DOCX 格式导出
- 🌍 **国际化** — 支持简体中文、英语、繁体中文、日语、德语 5 种语言
- 🗄️ **双数据库** — 开箱即用 SQLite，生产环境可切换 MySQL

## 技术栈

| 前端 | 后端 |
| --- | --- |
| Vue 3.4 + TypeScript | Spring Boot 3.3 + Java 17 |
| Vite 5 | MyBatis-Plus 3.5 |
| Element Plus | Spring Security + JWT |
| Pinia | SQLite / MySQL |
| highlight.js + marked | Caffeine Cache |
| Tailwind CSS | OpenHTMLToPDF / Apache POI |
| vue-i18n | |

## 快速启动

### 使用 Docker（推荐）

```bash
docker run -d \
  --name freedoc \
  -p 9200:9200 \
  -v freedoc-uploads:/app/uploads \
  -v freedoc-data:/app/data \
  tomshushu/free-doc:latest
```

### 使用源码构建

```bash
git clone https://github.com/Tomshushu/free-doc.git
cd free-doc
docker build -t tomshushu/free-doc:latest .
```

启动：

```bash
docker run -d \
  --name freedoc \
  -p 9200:9200 \
  -v freedoc-uploads:/app/uploads \
  -v freedoc-data:/app/data \
  tomshushu/free-doc:latest
```

启动后访问 `http://localhost:9200`，默认管理员账号：

| 账号 | 密码 |
| --- | --- |
| `admin` | `adminadmin` |

> ⚠️ **安全提醒**：首次登录后请立即修改默认密码，并设置强 `JWT_SECRET`。

## 配置说明

### 环境变量

通过环境变量或配置文件 (`free-doc.conf`) 进行配置，环境变量优先级高于配置文件。

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_TYPE` | `sqlite` | 数据库类型：`sqlite` 或 `mysql` |
| `SERVER_PORT` | `9200` | 服务端口 |
| `JWT_SECRET` | — | JWT 密钥（**必填**，至少 32 位随机字符串） |
| `DEFAULT_ADMIN_PASSWORD` | `adminadmin` | 初始管理员密码 |
| `UPLOAD_PATH` | `./uploads` | 上传文件存储路径 |
| `UPLOAD_MAX_SIZE` | `52428800` | 上传文件大小限制（50MB） |

### MySQL 配置

当 `DB_TYPE=mysql` 时，需要额外配置：

| 变量 | 说明 |
| --- | --- |
| `MYSQL_HOST` | MySQL 主机地址 |
| `MYSQL_PORT` | MySQL 端口（默认 3306） |
| `MYSQL_DATABASE` | 数据库名 |
| `MYSQL_USER` | 数据库用户名 |
| `MYSQL_PASSWORD` | 数据库密码 |
| `MYSQL_USE_SSL` | 是否启用 SSL（默认 true） |

### Docker Compose 示例（MySQL）

```yaml
version: '3.8'
services:
  freedoc:
    image: tomshushu/free-doc:latest
    container_name: freedoc
    ports:
      - "9200:9200"
    environment:
      - DB_TYPE=mysql
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_DATABASE=freedoc
      - MYSQL_USER=freedoc
      - MYSQL_PASSWORD=your_secure_password
      - JWT_SECRET=your_jwt_secret_at_least_32_chars
    volumes:
      - freedoc-uploads:/app/uploads
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=freedoc
      - MYSQL_USER=freedoc
      - MYSQL_PASSWORD=your_secure_password
    volumes:
      - mysql-data:/var/lib/mysql

volumes:
  freedoc-uploads:
  mysql-data:
```

### 配置文件挂载

将配置文件挂载到容器内：

```bash
docker run -d \
  --name freedoc \
  -p 9200:9200 \
  -v /path/to/free-doc.conf:/app/config/free-doc.conf \
  -v freedoc-uploads:/app/uploads \
  -v freedoc-data:/app/data \
  tomshushu/free-doc:latest
```

## 项目结构

```
free-doc/
├── free-doc-server/          # 后端 (Spring Boot)
│   └── src/main/java/com/freedoc/
│       ├── controller/       # REST API 控制器
│       ├── service/          # 业务逻辑层
│       ├── mapper/           # 数据访问层
│       ├── entity/           # 数据实体
│       ├── dto/              # 数据传输对象
│       ├── config/           # 配置类
│       └── security/         # 安全认证
├── free-doc-web/             # 前端 (Vue 3)
│   └── src/
│       ├── views/            # 页面组件
│       ├── components/       # 通用组件
│       ├── composables/      # 组合式函数
│       ├── stores/           # Pinia 状态管理
│       ├── i18n/             # 国际化
│       ├── api/              # API 请求
│       └── utils/            # 工具函数
├── db/                       # 数据库初始化脚本
├── Dockerfile                # 多阶段构建
├── docker-entrypoint.sh      # 容器入口脚本
└── free-doc.conf             # 默认配置文件
```

## 开发指南

### 环境要求

- Node.js 20+
- Java 17+
- Maven 3.9+

### 前端开发

```bash
cd free-doc-web
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`。

### 后端开发

```bash
cd free-doc-server
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:9200`。

> 前端开发时需要配置代理将 `/api` 请求转发到后端服务。

## 效果展示

![登录注册](images/image.png)
![主界面](images/image-1.png)
![仪表盘](images/image-2.png)
![项目详情](images/image-3.png)
![编辑器](images/image-4.png)
![分享](images/image-5.png)
![设置](images/image-6.png)
![导出](images/image-7.png)
![团队](images/image-8.png)
![成员](images/image-11.png)
![文档](images/image-10.png)

### 版本回退

![版本历史](images/image-13.png)
![回滚](images/image-14.png)

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 许可证

本项目基于 [MIT License](LICENSE) 开源。

## 致谢

- [Vue.js](https://vuejs.org/) — 渐进式 JavaScript 框架
- [Spring Boot](https://spring.io/projects/spring-boot) — Java 后端框架
- [Element Plus](https://element-plus.org/) — Vue 3 UI 组件库
- [marked](https://marked.js.org/) — Markdown 解析器
- [highlight.js](https://highlightjs.org/) — 代码语法高亮
