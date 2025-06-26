# 夸克网盘链接转换器

## 功能概述

这个模块实现了将夸克网盘分享链接转换为自己账号下的新分享链接的功能，基于Python脚本转换为Java实现。

## 主要功能

1. **链接提取**: 从文本中提取夸克网盘链接
2. **文件转存**: 将分享的文件保存到自己的网盘
3. **新链接生成**: 为转存的文件创建新的分享链接
4. **批量处理**: 支持文本中多个链接的批量转换
5. **文件夹支持**: 支持递归处理文件夹结构

## 核心组件

### 1. 配置类 (QuarkPanConfig)
- 管理夸克网盘相关配置
- 包括Cookie、目录ID、重试次数等设置

### 2. 客户端工具 (QuarkPanClient)
- 封装夸克网盘API调用
- 处理HTTP请求和响应
- 实现文件保存、分享等核心功能

### 3. 业务服务 (QuarkPanService)
- 实现链接转换的主要业务逻辑
- 处理文件和文件夹的递归保存
- 管理批量操作和错误处理

### 4. 控制器 (QuarkPanController)
- 提供REST API接口
- 处理HTTP请求和响应
- 实现用户交互功能

## API接口

### 1. 替换文本中的链接
```http
POST /api/quark/replace-urls
Content-Type: application/json

{
    "text": "这里包含夸克网盘链接的文本"
}
```

**响应示例:**
```json
{
    "success": true,
    "message": "链接替换完成",
    "originalText": "原始文本",
    "updatedText": "替换后的文本",
    "originalUrls": ["abc123", "def456"],
    "processedCount": 2
}
```

### 2. 转换单个链接
```http
POST /api/quark/convert-url
Content-Type: application/json

{
    "url": "https://pan.quark.cn/s/abc123def456"
}
```

**响应示例:**
```json
{
    "success": true,
    "message": "链接转换成功",
    "originalUrl": "https://pan.quark.cn/s/abc123def456",
    "newUrl": "https://pan.quark.cn/s/xyz789uvw012",
    "pwdId": "abc123def456"
}
```

### 3. 提取链接标识符
```http
POST /api/quark/extract-urls
Content-Type: application/json

{
    "text": "包含链接的文本"
}
```

**响应示例:**
```json
{
    "success": true,
    "message": "提取完成",
    "urlIds": ["abc123def456", "xyz789uvw012"],
    "count": 2
}
```

### 4. 健康检查
```http
GET /api/quark/health
```

## 配置说明

在 `application.yml` 中添加以下配置：

```yaml
quark:
  pan:
    # 夸克网盘Cookie（必须配置）
    cookie: "你的夸克网盘Cookie"
    # 默认保存目录ID（必须配置）
    default-save-directory-id: "目标文件夹ID"
    # 请求间隔时间（毫秒）
    request-interval: 2000
    # 最大重试次数
    max-retry-count: 10
    # 任务超时时间（毫秒）
    task-timeout: 300000
    # 单次转存文件数量限制
    max-files-per-save: 40
```

### 获取Cookie的方法

1. 登录夸克网盘网页版
2. 打开浏览器开发者工具 (F12)
3. 在Network标签页中找到任意请求
4. 复制请求头中的Cookie值

### 获取目录ID的方法

1. 在夸克网盘中进入要保存文件的目录
2. 查看浏览器地址栏中的URL
3. 提取其中的目录ID参数

## 使用示例

### Java代码示例

```java
@Autowired
private QuarkPanService quarkPanService;

// 转换文本中的链接
String originalText = "分享链接：https://pan.quark.cn/s/abc123def456";
String convertedText = quarkPanService.replaceQuarkUrls(originalText);

// 转换单个链接
String newUrl = quarkPanService.convertQuarkUrl("abc123def456");

// 提取链接标识符
List<String> urlIds = quarkPanService.extractQuarkUrlIds(originalText);
```

### curl示例

```bash
# 替换文本中的链接
curl -X POST http://localhost:8080/api/quark/replace-urls \
  -H "Content-Type: application/json" \
  -d '{"text":"分享链接：https://pan.quark.cn/s/abc123def456"}'

# 转换单个链接
curl -X POST http://localhost:8080/api/quark/convert-url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://pan.quark.cn/s/abc123def456"}'
```

## 注意事项

1. **Cookie有效性**: 确保配置的Cookie是有效的，过期后需要重新获取
2. **目录权限**: 确保有目标目录的写入权限
3. **网络稳定**: 转换过程需要稳定的网络连接
4. **文件限制**: 注意夸克网盘的文件数量和大小限制
5. **频率限制**: 避免过于频繁的请求，可能触发限制

## 错误处理

系统会处理以下常见错误：

- Cookie无效或过期
- 网络连接问题
- 文件保存失败
- 分享链接创建失败
- 超出用户等级限制

## 日志记录

系统会记录详细的操作日志，包括：

- 链接转换过程
- API调用结果
- 错误信息和堆栈跟踪
- 性能统计信息

## 测试

运行测试用例：

```bash
mvn test -Dtest=QuarkPanServiceTest
```

## 扩展功能

可以考虑添加的功能：

1. 批量文件处理队列
2. 转换历史记录
3. 用户配置管理
4. 定时任务支持
5. 监控和统计功能
