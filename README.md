<<<<<<< HEAD
**English** | [**中文**](README_zh-CN.md) | [**日本語**](README_ja.md)

<div align="center">
=======



# FreeDoc - 文档协作平台

FreeDoc 是一款轻量级的文档协作平台，支持 Markdown 和富文本编辑、团队协作、文档分享与导出。
>>>>>>> f3a634ecbf3137d1d1808485b3d6a631a0e48ed7

# FreeDoc

**Lightweight Document Collaboration Platform**

[![License](https://img.shields.io/badge/License-MIT-informational.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://hub.docker.com/r/tomshushu/free-doc)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Vue 3](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vue.js&logoColor=white)](https://vuejs.org/)

<<<<<<< HEAD
[Features](#features) · [Quick Start](#quick-start) · [Configuration](#configuration) · [Development](#development) · [Contributing](#contributing)
=======
| 类别 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.3 + Java 17、MyBatis-Plus 3.5、Spring Security + JWT |
| 前端 | Vue 3.4 + TypeScript、Vite 5、Element Plus、Pinia |
| 存储 | SQLite / MySQL、Caffeine Cache |
| 导出 | OpenHTMLToPDF、Apache POI |

## 项目结构

```
free-doc/
├── free-doc-server/          # 后端服务
│   └── src/
│       ├── main/
│       │   ├── java/      # Java 源代码
│       │   └── resources/
│       │       ├── i18n/  # 国际化资源
│       │       └── static/ # 静态资源
│       └── test/         # 测试代码
├── free-doc-web/           # 前端应用
│   └── src/
│       ├── api/          # API 接口
│       ├── components/   # 公共组件
│       ├── composables/ # 组合式函数
│       ├── stores/      # 状态管理
│       ├── views/      # 页面视图
│       └── i18n/       # 国际化配置
├── db/                    # 数据库脚本
│   ├── free_doc_sqlite.sql
│   └── free_doc_mysql.sql
└── Dockerfile            # Docker 构建文件
```
>>>>>>> f3a634ecbf3137d1d1808485b3d6a631a0e48ed7

</div>

---

## Features

- 📝 **Markdown Editor** — Real-time preview, syntax highlighting, extended toolbar, resizable split panes
- 👥 **Team Collaboration** — Create teams, invite members, assign read/write permissions by role
- 📁 **Project Organization** — Directory tree for document management with drag-and-drop and rename support
- 🔗 **Document Sharing** — Generate share links with optional password protection and expiration
- 📜 **Version History** — Automatic version tracking with one-click rollback
- 📤 **Multi-format Export** — Export to Markdown, HTML, PDF, and DOCX formats
- 🌍 **i18n** — Supports 5 languages: Simplified Chinese, English, Traditional Chinese, Japanese, and German
- 🗄️ **Dual Database** — SQLite out of the box, switchable to MySQL for production

## Tech Stack

| Frontend | Backend |
| --- | --- |
| Vue 3.4 + TypeScript | Spring Boot 3.3 + Java 17 |
| Vite 5 | MyBatis-Plus 3.5 |
| Element Plus | Spring Security + JWT |
| Pinia | SQLite / MySQL |
| highlight.js + marked | Caffeine Cache |
| Tailwind CSS | OpenHTMLToPDF / Apache POI |
| vue-i18n | |

## Quick Start

### Using Docker (Recommended)

```bash
docker run -d \
  --name freedoc \
  -p 9200:9200 \
  -v freedoc-uploads:/app/uploads \
  -v freedoc-data:/app/data \
  tomshushu/free-doc:latest
```

<<<<<<< HEAD
### Build from Source

```bash
git clone https://github.com/Tomshushu/free-doc.git
=======
### 使用源码构建

```bash
git clone https://gitee.com/Tom-shushu/free-doc.git
>>>>>>> f3a634ecbf3137d1d1808485b3d6a631a0e48ed7
cd free-doc
docker build -t tomshushu/free-doc:latest .
```

<<<<<<< HEAD
Run:

=======
启动后访问 `http://localhost:9200`

## 默认管理员账号

| 账号 | 密码 |
| --- | --- |
| `admin` | `adminadmin` |

> **安全提示**：首次登录后请立即修改默认密码，并设置强 `JWT_SECRET`。

## 环境变量配置

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `SERVER_PORT` | 服务端口 | `9200` |
| `MYSQL_HOST` | MySQL 主机地址 | - |
| `MYSQL_PORT` | MySQL 端口 | `3306` |
| `MYSQL_DATABASE` | MySQL 数据库名 | - |
| `MYSQL_USER` | MySQL 用户名 | - |
| `MYSQL_PASSWORD` | MySQL 密码 | - |
| `JWT_SECRET` | JWT 密钥 | - |
| `upload.path` | 上传文件路径 | `./uploads` |
| `upload.max-size` | 最大上传大小 | `52428800` |

## 使用 MySQL 数据库

当配置 MySQL 相关环境变量后，系统会自动切换到 MySQL：

>>>>>>> f3a634ecbf3137d1d1808485b3d6a631a0e48ed7
```bash
docker run -d \
  --name freedoc \
  -p 9200:9200 \
  -e MYSQL_HOST=mysql_host \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=freedoc \
  -e MYSQL_USER=root \
  -e MYSQL_PASSWORD=your_password \
  tomshushu/free-doc:latest
```

<<<<<<< HEAD
After starting, visit `http://localhost:9200`. Default admin credentials:

| Username | Password |
| --- | --- |
| `admin` | `adminadmin` |

> ⚠️ **Security Notice**: Change the default password immediately after your first login, and set a strong `JWT_SECRET`.

## Configuration

### Environment Variables

Configure via environment variables or the config file (`free-doc.conf`). Environment variables take precedence over the config file.

| Variable | Default | Description |
| --- | --- | --- |
| `DB_TYPE` | `sqlite` | Database type: `sqlite` or `mysql` |
| `SERVER_PORT` | `9200` | Server port |
| `JWT_SECRET` | — | JWT secret key (**required**, at least 32 random characters) |
| `DEFAULT_ADMIN_PASSWORD` | `adminadmin` | Initial admin password |
| `UPLOAD_PATH` | `./uploads` | Upload file storage path |
| `UPLOAD_MAX_SIZE` | `52428800` | Upload file size limit (50MB) |

### MySQL Configuration

When `DB_TYPE=mysql`, additional configuration is required:

| Variable | Description |
| --- | --- |
| `MYSQL_HOST` | MySQL host address |
| `MYSQL_PORT` | MySQL port (default 3306) |
| `MYSQL_DATABASE` | Database name |
| `MYSQL_USER` | Database username |
| `MYSQL_PASSWORD` | Database password |
| `MYSQL_USE_SSL` | Enable SSL (default true) |

### Docker Compose Example (MySQL)

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

### Config File Mount

Mount a config file into the container:

```bash
docker run -d \
  --name freedoc \
  -p 9200:9200 \
  -v /path/to/free-doc.conf:/app/config/free-doc.conf \
  -v freedoc-uploads:/app/uploads \
  -v freedoc-data:/app/data \
  tomshushu/free-doc:latest
```

## Project Structure

```
free-doc/
├── free-doc-server/          # Backend (Spring Boot)
│   └── src/main/java/com/freedoc/
│       ├── controller/       # REST API controllers
│       ├── service/          # Business logic
│       ├── mapper/           # Data access layer
│       ├── entity/           # Data entities
│       ├── dto/              # Data transfer objects
│       ├── config/           # Configuration classes
│       └── security/         # Security & authentication
├── free-doc-web/             # Frontend (Vue 3)
│   └── src/
│       ├── views/            # Page components
│       ├── components/       # Shared components
│       ├── composables/      # Composable functions
│       ├── stores/           # Pinia state management
│       ├── i18n/             # Internationalization
│       ├── api/              # API requests
│       └── utils/            # Utility functions
├── db/                       # Database init scripts
├── Dockerfile                # Multi-stage build
├── docker-entrypoint.sh      # Container entrypoint
└── free-doc.conf             # Default config file
```

## Development

### Prerequisites

- Node.js 20+
- Java 17+
- Maven 3.9+

### Frontend Development

```bash
cd free-doc-web
npm install
npm run dev
```

The frontend dev server runs at `http://localhost:5173` by default.

### Backend Development

```bash
cd free-doc-server
mvn spring-boot:run
```

The backend service runs at `http://localhost:9200` by default.

> When developing the frontend, configure a proxy to forward `/api` requests to the backend service.

## Screenshots

![Login & Register](images/image.png)
![Main Interface](images/image-1.png)
![Dashboard](images/image-2.png)
![Project Detail](images/image-3.png)
![Editor](images/image-4.png)
![Sharing](images/image-5.png)
![Settings](images/image-6.png)
![Export](images/image-7.png)
![Team](images/image-8.png)
![Members](images/image-11.png)
![Documents](images/image-10.png)

### Version Rollback

![Version History](images/image-13.png)
![Rollback](images/image-14.png)

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgments

- [Vue.js](https://vuejs.org/) — The Progressive JavaScript Framework
- [Spring Boot](https://spring.io/projects/spring-boot) — Java backend framework
- [Element Plus](https://element-plus.org/) — Vue 3 UI component library
- [marked](https://marked.js.org/) — Markdown parser
- [highlight.js](https://highlightjs.org/) — Syntax highlighting
=======
## 效果展示

![登录注册](image.png)
![主界面](image-1.png)
![编辑界面](image-2.png)
![项目详情](image-3.png)
![文档浏览](image-4.png)
![分享设置](image-5.png)
![导出功能](image-6.png)
![版本历史](image-7.png)
![版本回退](image-13.png)
![回退确认](image-14.png)

## 许可证

MIT License
>>>>>>> f3a634ecbf3137d1d1808485b3d6a631a0e48ed7
