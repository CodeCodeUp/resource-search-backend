# Resource Search Backend

一个基于Spring Boot + MyBatis + MySQL的资源搜索后端应用，支持菜单层级查询和资源高级搜索功能。

## 功能特性

- **菜单管理**: 支持层级结构的菜单查询
- **资源管理**: 完整的CRUD操作
- **高级搜索**: 支持中文分词、模糊匹配、相关性排序
- **智能匹配**: 如搜索"四六级答案"无完全匹配时，自动匹配"四六级"、"答案"等分词

## 技术栈

- Spring Boot 2.7.18
- MyBatis 2.3.1
- PageHelper 1.4.7 (分页插件)
- MySQL 8.0
- Maven 3.6+
- Java 8+

## 环境要求

- Java 8 或更高版本
- Maven 3.6+
- MySQL 8.0+

## 快速开始

### 1. 数据库准备

1. **安装MySQL 8.0+**

2. **创建数据库:**
   ```sql
   CREATE DATABASE resource_search CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **创建数据表:**
   ```sql
   -- 创建菜单表
   CREATE TABLE IF NOT EXISTS menu (
     id INT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
     menu VARCHAR(100) NOT NULL COMMENT '菜单标识',
     name VARCHAR(100) NOT NULL COMMENT '菜单名称',
     level TINYINT NOT NULL DEFAULT 1 COMMENT '菜单层级',
     create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

   -- 创建资源表
   CREATE TABLE IF NOT EXISTS resource (
     id INT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID',
     name VARCHAR(100) NOT NULL COMMENT '资源名称',
     content TEXT COMMENT '资源内容',
     url VARCHAR(255) COMMENT '资源URL',
     pig VARCHAR(50) COMMENT '标签字段',
     level TINYINT NOT NULL DEFAULT 1 COMMENT '资源层级',
     type VARCHAR(50) COMMENT '资源类型',
     create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
     update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';
   ```

### 2. 配置应用

1. **修改数据库配置** (src/main/resources/application.yml):
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/resource_search?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
       username: your_username
       password: your_password

   # MyBatis配置
   mybatis:
     mapper-locations: classpath:mapper/*.xml
     type-aliases-package: org.example.entity
     configuration:
       map-underscore-to-camel-case: true
   ```

### 3. 启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080/api` 启动

## API接口文档

应用将在 `http://localhost:8080/api` 启动

### 菜单管理接口

- **获取层级菜单结构:** `GET /api/menus/hierarchical`
- **根据层级获取菜单:** `GET /api/menus/level/{level}`
- **获取层级范围菜单:** `GET /api/menus/level-range?minLevel={min}&maxLevel={max}`
- **搜索菜单:** `GET /api/menus/search?name={name}`
- **按层级和名称搜索:** `GET /api/menus/search-by-level?level={level}&name={name}`
- **获取最大层级:** `GET /api/menus/max-level`

### 资源管理接口

#### 基础CRUD操作
- **创建资源:** `POST /api/resources`
- **获取资源:** `GET /api/resources/{id}`
- **更新资源:** `PUT /api/resources/{id}`
- **删除资源:** `DELETE /api/resources/{id}`

#### 查询接口（支持分页和不分页两种方式）
- **获取所有资源:** `GET /api/resources` (不分页)
- **获取所有资源（分页）:** `GET /api/resources/page?page={page}&size={size}`
- **根据层级获取资源:** `GET /api/resources/level/{level}` (不分页)
- **根据层级获取资源（分页）:** `GET /api/resources/level/{level}/page?page={page}&size={size}`
- **根据类型获取资源:** `GET /api/resources/type/{type}` (不分页)
- **根据类型获取资源（分页）:** `GET /api/resources/type/{type}/page?page={page}&size={size}`

### 搜索接口

#### 高级搜索（支持分页和不分页两种方式）
- **高级搜索:** `POST /api/resources/search` (不分页)
- **高级搜索（分页）:** `POST /api/resources/search/page`
- **GET方式分页搜索:** `GET /api/resources/search?term={searchTerm}&page={page}&size={size}`

## API调用示例

### 创建资源
```bash
curl -X POST http://localhost:8080/api/resources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "2025四六级答案",
    "content": "2025年大学英语四六级考试答案和解析，包含听力、阅读、写作、翻译等各部分详细答案",
    "url": "https://example.com/cet-answers-2025",
    "pig": "exam",
    "level": 1,
    "type": "document"
  }'
```

### 高级搜索资源（不分页）
```bash
curl -X POST http://localhost:8080/api/resources/search \
  -H "Content-Type: application/json" \
  -d '{
    "searchTerm": "四六级答案",
    "level": "1"
  }'
```

### 高级搜索资源（分页）
```bash
curl -X POST http://localhost:8080/api/resources/search/page \
  -H "Content-Type: application/json" \
  -d '{
    "searchTerm": "四六级答案",
    "page": 0,
    "size": 10,
    "level": "1"
  }'
```

### 获取层级菜单结构
```bash
curl -X GET http://localhost:8080/api/menus/hierarchical
```

### 分页获取所有资源
```bash
curl -X GET "http://localhost:8080/api/resources/page?page=0&size=10"
```

### 分页根据层级获取资源
```bash
curl -X GET "http://localhost:8080/api/resources/level/1/page?page=0&size=5"
```

### GET方式分页搜索资源
```bash
curl -X GET "http://localhost:8080/api/resources/search?term=四六级&page=0&size=5"
```

## 核心功能

### 菜单管理
- **层级结构查询**: 支持多层级菜单的树形结构展示
- **层级范围查询**: 可指定层级范围获取菜单
- **名称搜索**: 支持菜单名称的模糊搜索
- **组合查询**: 支持按层级和名称组合搜索

### 资源管理
- **完整CRUD**: 创建、读取、更新、删除资源
- **多字段搜索**: 同时搜索资源名称和内容
- **智能分词**: 支持中文分词搜索，如"四六级答案"可匹配"四六级"、"答案"
- **相关性排序**: 搜索结果按相关性排序（完全匹配 > 名称包含 > 内容包含）
- **分页支持**: 使用PageHelper实现高效分页查询和搜索
- **层级过滤**: 可按资源层级过滤结果
- **类型分类**: 支持按资源类型分类查询

### 高级搜索特性
- **模糊匹配**: 自动处理部分匹配情况
- **分词搜索**: 当完整搜索词无结果时，自动进行分词搜索
- **中文支持**: 特别优化中文搜索体验
- **多策略搜索**: 组合多种搜索策略提高召回率
- **MyBatis优化**: 使用MyBatis原生SQL实现高效查询

## Troubleshooting

### Elasticsearch Connection Issues

1. **Check if Elasticsearch is running:**
   ```bash
   curl http://localhost:9200
   ```

2. **Check Docker containers:**
   ```bash
   docker-compose ps
   ```

3. **View application logs:**
   ```bash
   mvn spring-boot:run --debug
   ```

### Common Issues

- **Port 9200 already in use:** Stop any existing Elasticsearch instances
- **Docker not installed:** Install Docker Desktop for your OS
- **Java version issues:** Ensure Java 8+ is installed and JAVA_HOME is set

## Configuration

Edit `src/main/resources/application.yml` to modify:
- Elasticsearch connection settings
- Server port
- Logging levels
- Index configuration

## Development

### Running Tests
```bash
mvn test
```

### Building the Application
```bash
mvn clean package
```

### Running the JAR
```bash
java -jar target/resource-search-backend-1.0-SNAPSHOT.jar
```
