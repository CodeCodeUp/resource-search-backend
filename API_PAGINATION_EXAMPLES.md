# 分页API使用示例

## 分页响应格式

所有分页接口都返回统一的PageInfo格式：

```json
{
  "total": 100,           // 总记录数
  "list": [...],          // 当前页数据列表
  "pageNum": 1,           // 当前页码
  "pageSize": 10,         // 每页大小
  "pages": 10,            // 总页数
  "hasNextPage": true,    // 是否有下一页
  "hasPreviousPage": false, // 是否有上一页
  "isFirstPage": true,    // 是否是第一页
  "isLastPage": false,    // 是否是最后一页
  "navigatePages": 8,     // 导航页码数
  "navigatepageNums": [1,2,3,4,5,6,7,8], // 导航页码列表
  "navigateFirstPage": 1, // 导航第一页
  "navigateLastPage": 8   // 导航最后一页
}
```

## API接口对比

### 1. 获取所有资源

#### 不分页版本
```bash
GET /api/resources
```
返回：`List<ResourceResponse>`

#### 分页版本
```bash
GET /api/resources/page?page=0&size=10
```
返回：`PageInfo<ResourceResponse>`

### 2. 高级搜索

#### 不分页版本
```bash
POST /api/resources/search
Content-Type: application/json

{
  "searchTerm": "四六级答案",
  "level": "1"
}
```
返回：`List<ResourceResponse>`

#### 分页版本
```bash
POST /api/resources/search/page
Content-Type: application/json

{
  "searchTerm": "四六级答案",
  "page": 0,
  "size": 10,
  "level": "1"
}
```
返回：`PageInfo<ResourceResponse>`

### 3. 根据层级获取资源

#### 不分页版本
```bash
GET /api/resources/level/1
```
返回：`List<ResourceResponse>`

#### 分页版本
```bash
GET /api/resources/level/1/page?page=0&size=10
```
返回：`PageInfo<ResourceResponse>`

### 4. 根据类型获取资源

#### 不分页版本
```bash
GET /api/resources/type/document
```
返回：`List<ResourceResponse>`

#### 分页版本
```bash
GET /api/resources/type/document/page?page=0&size=10
```
返回：`PageInfo<ResourceResponse>`

## 分页参数说明

- **page**: 页码，从0开始
- **size**: 每页大小，默认10
- **level**: 层级过滤（可选）

## 使用建议

1. **小数据量场景**：使用不分页接口，简单直接
2. **大数据量场景**：使用分页接口，避免性能问题
3. **前端展示**：推荐使用分页接口，便于实现分页组件
4. **数据导出**：可以使用不分页接口获取全量数据

## 性能优化

- 分页查询使用PageHelper插件，性能优异
- 支持数据库层面的分页，避免内存溢出
- 自动统计总数，无需额外查询
- 支持复杂查询条件的分页
