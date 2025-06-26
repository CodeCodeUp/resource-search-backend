# 夸克网盘链接转换器使用示例

## 快速开始

### 1. 配置应用

在 `application.yml` 中配置夸克网盘相关信息：

```yaml
quark:
  pan:
    # 必须配置：你的夸克网盘Cookie
    cookie: "你的Cookie值"
    # 必须配置：目标保存目录ID
    default-save-directory-id: "目标文件夹ID"
    # 可选配置
    request-interval: 2000
    max-retry-count: 10
    task-timeout: 300000
    max-files-per-save: 40
```

### 2. 启动应用

```bash
mvn spring-boot:run
```

### 3. 使用API

#### 替换文本中的链接

```bash
curl -X POST http://localhost:8080/api/quark/replace-urls \
  -H "Content-Type: application/json" \
  -d '{
    "text": "这里有一个夸克网盘链接：https://pan.quark.cn/s/abc123def456 请下载"
  }'
```

**响应示例：**
```json
{
  "success": true,
  "message": "链接替换完成",
  "originalText": "这里有一个夸克网盘链接：https://pan.quark.cn/s/abc123def456 请下载",
  "updatedText": "这里有一个夸克网盘链接：https://pan.quark.cn/s/xyz789uvw012 请下载",
  "originalUrls": ["abc123def456"],
  "processedCount": 1
}
```

#### 转换单个链接

```bash
curl -X POST http://localhost:8080/api/quark/convert-url \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://pan.quark.cn/s/abc123def456"
  }'
```

**响应示例：**
```json
{
  "success": true,
  "message": "链接转换成功",
  "originalUrl": "https://pan.quark.cn/s/abc123def456",
  "newUrl": "https://pan.quark.cn/s/xyz789uvw012",
  "pwdId": "abc123def456"
}
```

#### 提取链接标识符

```bash
curl -X POST http://localhost:8080/api/quark/extract-urls \
  -H "Content-Type: application/json" \
  -d '{
    "text": "链接1：https://pan.quark.cn/s/abc123 链接2：https://pan.quark.cn/s/def456"
  }'
```

**响应示例：**
```json
{
  "success": true,
  "message": "提取完成",
  "urlIds": ["abc123", "def456"],
  "count": 2
}
```

## Java代码示例

### 在Spring Boot应用中使用

```java
@RestController
@RequestMapping("/demo")
public class DemoController {
    
    @Autowired
    private QuarkPanService quarkPanService;
    
    @PostMapping("/convert-text")
    public String convertText(@RequestBody String text) {
        return quarkPanService.replaceQuarkUrls(text);
    }
    
    @PostMapping("/convert-single")
    public String convertSingle(@RequestParam String pwdId) {
        return quarkPanService.convertQuarkUrl(pwdId);
    }
}
```

### 在服务类中使用

```java
@Service
public class ContentService {
    
    @Autowired
    private QuarkPanService quarkPanService;
    
    public void processContent(String content) {
        // 提取链接
        List<String> urlIds = quarkPanService.extractQuarkUrlIds(content);
        System.out.println("找到 " + urlIds.size() + " 个夸克网盘链接");
        
        // 转换链接
        String convertedContent = quarkPanService.replaceQuarkUrls(content);
        System.out.println("转换后的内容：" + convertedContent);
    }
}
```

## 前端集成示例

### JavaScript/Ajax

```javascript
// 转换文本中的链接
function convertText() {
    const text = document.getElementById('inputText').value;
    
    fetch('/api/quark/replace-urls', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ text: text })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById('outputText').value = data.updatedText;
            console.log('转换成功，处理了 ' + data.processedCount + ' 个链接');
        } else {
            alert('转换失败：' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('请求失败');
    });
}

// 转换单个链接
function convertSingleUrl() {
    const url = document.getElementById('singleUrl').value;
    
    fetch('/api/quark/convert-url', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ url: url })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            document.getElementById('newUrl').value = data.newUrl;
            console.log('链接转换成功');
        } else {
            alert('转换失败：' + data.message);
        }
    });
}
```

### HTML表单示例

```html
<!DOCTYPE html>
<html>
<head>
    <title>夸克网盘链接转换器</title>
</head>
<body>
    <h1>夸克网盘链接转换器</h1>
    
    <!-- 文本转换 -->
    <div>
        <h2>文本链接转换</h2>
        <textarea id="inputText" rows="5" cols="50" 
                  placeholder="输入包含夸克网盘链接的文本"></textarea><br>
        <button onclick="convertText()">转换链接</button><br>
        <textarea id="outputText" rows="5" cols="50" 
                  placeholder="转换后的文本" readonly></textarea>
    </div>
    
    <!-- 单个链接转换 -->
    <div>
        <h2>单个链接转换</h2>
        <input type="text" id="singleUrl" 
               placeholder="https://pan.quark.cn/s/abc123def456"><br>
        <button onclick="convertSingleUrl()">转换</button><br>
        <input type="text" id="newUrl" placeholder="转换后的链接" readonly>
    </div>
    
    <script>
        // JavaScript代码见上面示例
    </script>
</body>
</html>
```

## 错误处理

### 常见错误及解决方案

1. **Cookie无效**
   ```json
   {
     "success": false,
     "message": "获取stoken失败"
   }
   ```
   解决：重新获取有效的Cookie

2. **目录ID错误**
   ```json
   {
     "success": false,
     "message": "保存文件失败"
   }
   ```
   解决：检查配置的目录ID是否正确

3. **网络连接问题**
   ```json
   {
     "success": false,
     "message": "处理过程中发生错误: Connection timeout"
   }
   ```
   解决：检查网络连接，增加超时时间

### 日志监控

查看应用日志以了解详细的处理过程：

```bash
tail -f logs/application.log | grep QuarkPan
```

## 性能优化建议

1. **批量处理**：对于大量链接，建议分批处理
2. **缓存机制**：可以实现转换结果缓存
3. **异步处理**：对于耗时操作，考虑使用异步处理
4. **限流控制**：避免过于频繁的API调用

## 安全注意事项

1. **Cookie保护**：确保Cookie不被泄露
2. **访问控制**：在生产环境中添加适当的访问控制
3. **日志脱敏**：避免在日志中记录敏感信息
4. **HTTPS**：在生产环境中使用HTTPS
