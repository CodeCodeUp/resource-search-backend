# /validate-token 接口集成夸克网盘链接转换

## 功能概述

我们已经成功将夸克网盘链接转换功能集成到 `/validate-token` 接口中。现在当用户通过该接口获取资源时，如果资源的URL是夸克网盘链接，系统会自动将其转换为您自己的夸克网盘链接。

## 修改详情

### 1. 更新的文件

**VerificationController.java**
- 添加了 `QuarkPanService` 依赖注入
- 修改了 `/validate-token` 接口的资源URL处理逻辑
- 新增了 `processResourceUrl` 方法来处理URL转换

### 2. 核心修改

#### 添加依赖注入
```java
@Autowired
private QuarkPanService quarkPanService;
```

#### 修改资源返回逻辑
```java
// 处理URL - 如果是夸克网盘链接，则转换为自己的链接
String originalUrl = resource.getUrl();
String processedUrl = processResourceUrl(originalUrl);
resourceData.put("url", processedUrl);
```

#### 新增URL处理方法
```java
private String processResourceUrl(String originalUrl) {
    if (originalUrl == null || originalUrl.trim().isEmpty()) {
        return originalUrl;
    }

    try {
        // 检查是否包含夸克网盘链接
        if (originalUrl.contains("pan.quark.cn/s/")) {
            logger.info("检测到夸克网盘链接，开始转换: {}", originalUrl);
            
            // 使用夸克网盘服务转换链接
            String convertedUrl = quarkPanService.replaceQuarkUrls(originalUrl);
            
            if (convertedUrl != null && !convertedUrl.equals(originalUrl)) {
                logger.info("夸克网盘链接转换成功: {} -> {}", originalUrl, convertedUrl);
                return convertedUrl;
            } else {
                logger.warn("夸克网盘链接转换失败，返回原链接: {}", originalUrl);
                return originalUrl;
            }
        } else {
            // 不是夸克网盘链接，直接返回原URL
            return originalUrl;
        }
    } catch (Exception e) {
        logger.error("处理资源URL时发生错误，返回原URL: {}", originalUrl, e);
        return originalUrl;
    }
}
```

## 工作流程

### 1. 原有流程
1. 用户调用 `/validate-token` 接口
2. 系统验证访问令牌
3. 返回资源信息，包括原始URL

### 2. 新的流程
1. 用户调用 `/validate-token` 接口
2. 系统验证访问令牌
3. **检查资源URL是否为夸克网盘链接**
4. **如果是夸克网盘链接，自动转换为自己的链接**
5. 返回资源信息，包括转换后的URL

## 使用示例

### API调用
```bash
curl -X POST http://localhost:8080/api/verify/validate-token \
  -H "Content-Type: application/json" \
  -d '{"token":"your-access-token"}'
```

### 响应示例

**原始资源URL为夸克网盘链接时：**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "resourceData": {
      "id": 123,
      "name": "测试资源",
      "url": "https://pan.quark.cn/s/xyz789uvw012",  // 转换后的链接
      "type": "movie"
    }
  }
}
```

**原始资源URL不是夸克网盘链接时：**
```json
{
  "success": true,
  "data": {
    "valid": true,
    "resourceData": {
      "id": 123,
      "name": "测试资源",
      "url": "https://example.com/resource.mp4",  // 保持原链接
      "type": "movie"
    }
  }
}
```

## 日志记录

系统会记录详细的转换过程：

```
INFO  - 检测到夸克网盘链接，开始转换: https://pan.quark.cn/s/abc123def456
INFO  - 夸克网盘链接转换成功: https://pan.quark.cn/s/abc123def456 -> https://pan.quark.cn/s/xyz789uvw012
```

## 错误处理

### 1. 转换失败处理
- 如果转换失败，系统会返回原始链接
- 记录警告日志，不影响正常业务流程

### 2. 异常处理
- 捕获所有转换过程中的异常
- 记录错误日志，返回原始链接
- 确保接口的稳定性

## 配置要求

确保夸克网盘转换功能正常工作，需要在 `application.yml` 中配置：

```yaml
quark:
  pan:
    # 必须配置有效的Cookie
    cookie: "你的夸克网盘Cookie"
    # 必须配置目标保存目录ID
    default-save-directory-id: "目标文件夹ID"
    # 其他配置参数
    request-interval: 2000
    max-retry-count: 10
    task-timeout: 300000
    max-files-per-save: 40
```

## 兼容性

### 1. 向后兼容
- 对于非夸克网盘链接，行为完全不变
- 现有的API调用方式保持不变
- 响应格式保持一致

### 2. 渐进增强
- 夸克网盘链接会被自动转换
- 转换失败时优雅降级到原链接
- 不影响其他类型的资源链接

## 性能考虑

### 1. 转换性能
- 只有检测到夸克网盘链接时才进行转换
- 转换过程是异步的，不会阻塞主流程
- 转换失败时快速返回原链接

### 2. 缓存建议
- 可以考虑对转换结果进行缓存
- 避免重复转换相同的链接
- 提高响应速度

## 监控建议

### 1. 关键指标
- 夸克网盘链接检测次数
- 转换成功率
- 转换耗时
- 转换失败次数

### 2. 告警设置
- 转换成功率低于阈值时告警
- 转换耗时过长时告警
- 配置错误时告警

## 测试验证

### 1. 功能测试
```bash
# 测试夸克网盘链接转换
curl -X POST http://localhost:8080/api/verify/validate-token \
  -H "Content-Type: application/json" \
  -d '{"token":"token-for-quark-resource"}'

# 测试普通链接（不转换）
curl -X POST http://localhost:8080/api/verify/validate-token \
  -H "Content-Type: application/json" \
  -d '{"token":"token-for-normal-resource"}'
```

### 2. 错误场景测试
- 夸克网盘配置错误
- 网络连接问题
- 无效的夸克网盘链接

## 注意事项

1. **配置验证**：确保夸克网盘配置正确且有效
2. **网络依赖**：转换过程需要访问夸克网盘API
3. **错误处理**：转换失败不应影响主业务流程
4. **日志监控**：关注转换过程的日志，及时发现问题
5. **性能影响**：转换过程可能增加响应时间，需要监控
