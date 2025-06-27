# 资源数据清理功能

## 功能概述

我们已经实现了完整的资源数据清理功能，用于清理资源入库时和数据库中已有数据的反斜杠字符（`\`）。该功能包括：

1. **入库时自动清理**：在所有资源入库点自动清理反斜杠字符
2. **一次性数据库清理**：清理数据库中已有的包含反斜杠字符的数据
3. **管理接口**：提供API接口进行手动清理和统计

## 实现的组件

### 1. 字符串清理工具类

#### StringCleanupUtil.java
- ✅ `removeBackslashes()` - 移除反斜杠字符
- ✅ `cleanSpecialCharacters()` - 清理多种特殊字符
- ✅ `cleanResourceName()` - 专门清理资源名称
- ✅ `cleanResourceContent()` - 专门清理资源内容
- ✅ `cleanResourceUrl()` - 专门清理资源URL
- ✅ `cleanResourceFields()` - 批量清理资源字段
- ✅ `containsBackslashes()` - 检查是否包含反斜杠
- ✅ `countBackslashes()` - 统计反斜杠数量

### 2. 入库点修改

#### QQChannelCrawlerService.java
- ✅ 在 `saveResourceToDatabase()` 方法中添加字段清理
- ✅ 清理name、content、url字段
- ✅ 记录清理日志

#### ExcelGameImportService.java
- ✅ 在 `convertToResource()` 方法中添加字段清理
- ✅ 清理游戏名称、备注、URL字段
- ✅ 记录清理日志

#### ResourceService.java
- ✅ 在 `createResource()` 方法中添加字段清理
- ✅ 清理所有资源字段
- ✅ 记录清理日志

### 3. 数据库清理功能

#### ResourceMapper.java & ResourceMapper.xml
- ✅ `selectResourcesWithBackslashes()` - 查询包含反斜杠的资源
- ✅ `updateResourceFields()` - 批量更新资源字段

#### DataCleanupService.java
- ✅ `cleanupBackslashesInDatabase()` - 清理数据库中的反斜杠字符
- ✅ `countResourcesWithBackslashes()` - 统计需要清理的资源数量
- ✅ `previewCleanup()` - 预览需要清理的数据
- ✅ `getCleanupStatistics()` - 获取清理统计信息

### 4. 一次性任务

#### DataCleanupTask.java
- ✅ 应用启动时自动执行清理任务
- ✅ 支持预览模式和实际清理模式
- ✅ 可配置开关控制是否执行
- ✅ 提供手动触发方法

### 5. 管理接口

#### DataCleanupController.java
- ✅ `/api/admin/cleanup/statistics` - 获取清理统计信息
- ✅ `/api/admin/cleanup/preview` - 预览需要清理的数据
- ✅ `/api/admin/cleanup/execute` - 执行数据清理
- ✅ `/api/admin/cleanup/check` - 检查是否需要清理
- ✅ `/api/admin/cleanup/health` - 健康检查

## 使用方法

### 1. 自动清理（无需操作）

所有新入库的资源会自动清理反斜杠字符：

```java
// QQ频道爬虫入库时自动清理
// Excel游戏导入时自动清理  
// API创建资源时自动清理
```

### 2. 一次性数据库清理

#### 应用启动时自动执行
```java
// 在DataCleanupTask.java中配置
private static final boolean ENABLE_CLEANUP = true;  // 启用清理
private static final boolean PREVIEW_ONLY = false;   // 实际清理模式
```

#### 手动API调用

**获取统计信息：**
```bash
curl -X GET http://localhost:8080/api/admin/cleanup/statistics
```

**预览需要清理的数据：**
```bash
curl -X GET http://localhost:8080/api/admin/cleanup/preview
```

**执行清理：**
```bash
curl -X POST http://localhost:8080/api/admin/cleanup/execute
```

**检查是否需要清理：**
```bash
curl -X GET http://localhost:8080/api/admin/cleanup/check
```

### 3. API响应示例

#### 统计信息响应
```json
{
  "success": true,
  "statistics": "清理统计: 总计 15 条资源需要清理，其中名称字段 8 条，内容字段 12 条，URL字段 3 条",
  "count": 15,
  "message": "统计信息获取成功"
}
```

#### 预览响应
```json
{
  "success": true,
  "preview": [
    "ID: 123, 原名称: '测试\\资源', 清理后: '测试资源'",
    "ID: 124, 原名称: '游戏\\下载', 清理后: '游戏下载'"
  ],
  "count": 2,
  "message": "预览数据获取成功"
}
```

#### 执行清理响应
```json
{
  "success": true,
  "beforeCount": 15,
  "afterCount": 0,
  "cleanedCount": 15,
  "message": "清理完成，共清理了 15 条记录"
}
```

## 清理规则

### 1. 字符清理规则
- **反斜杠（`\`）**：完全移除
- **转义字符**：移除 `\r`、`\n`、`\t`
- **多余空白**：将多个空白字符替换为单个空格
- **首尾空白**：去除首尾空白字符

### 2. 字段长度限制
- **资源名称**：最大255字符，超出部分截断
- **资源内容**：最大1000字符，超出部分截断
- **资源URL**：不限制长度，只清理反斜杠

### 3. 清理示例
```
原始: "测试\资源\下载"
清理后: "测试资源下载"

原始: "游戏\\r\\n描述"
清理后: "游戏 描述"

原始: "  多个   空格  "
清理后: "多个 空格"
```

## 日志记录

### 1. 入库清理日志
```
INFO - 清理资源字段中的反斜杠字符: 测试\资源
INFO - 清理Excel游戏资源字段中的反斜杠字符: 游戏\名称
```

### 2. 数据库清理日志
```
INFO - 开始清理数据库中包含反斜杠字符的资源...
INFO - 找到 15 条包含反斜杠字符的资源
INFO - 批量更新第 1 批，更新了 15 条记录
INFO - 数据清理完成，共清理了 15 条资源
```

### 3. 一次性任务日志
```
INFO - === 开始执行数据清理一次性任务 ===
INFO - 清理统计: 总计 15 条资源需要清理，其中名称字段 8 条，内容字段 12 条，URL字段 3 条
INFO - === 开始执行数据清理 ===
INFO - 数据清理完成，所有反斜杠字符已清理
INFO - === 数据清理任务执行完成，耗时: 1250 ms ===
```

## 安全特性

### 1. 事务保护
- 所有数据库清理操作都在事务中执行
- 清理失败时自动回滚，不影响数据完整性

### 2. 批量处理
- 大量数据分批处理，避免内存溢出
- 每批100条记录，减少数据库压力

### 3. 错误处理
- 完善的异常处理，清理失败不影响应用启动
- 详细的错误日志，便于问题排查

### 4. 预览功能
- 支持预览模式，可以先查看需要清理的数据
- 避免误操作，提高安全性

## 性能考虑

### 1. 清理性能
- 只对包含反斜杠的字段进行清理
- 使用批量更新减少数据库交互
- 分批处理避免长时间锁表

### 2. 查询优化
- 使用LIKE查询快速定位需要清理的记录
- 按ID排序保证处理顺序

### 3. 内存优化
- 分批加载数据，避免一次性加载大量记录
- 及时释放不需要的对象

## 配置选项

### 1. 一次性任务配置
```java
// 在DataCleanupTask.java中
private static final boolean ENABLE_CLEANUP = true;   // 是否启用清理
private static final boolean PREVIEW_ONLY = false;    // 是否只预览
```

### 2. 批量处理配置
```java
// 在DataCleanupService.java中
int batchSize = 100;  // 批量处理大小
```

## 注意事项

1. **首次运行**：建议先使用预览模式查看需要清理的数据
2. **备份数据**：重要数据建议先备份再执行清理
3. **性能影响**：大量数据清理可能需要较长时间
4. **日志监控**：关注清理过程的日志，及时发现问题
5. **定期检查**：建议定期检查是否有新的需要清理的数据

## 扩展功能

未来可以考虑添加的功能：
1. 支持清理其他特殊字符
2. 支持自定义清理规则
3. 支持定时自动清理
4. 支持清理历史记录
5. 支持清理结果导出
