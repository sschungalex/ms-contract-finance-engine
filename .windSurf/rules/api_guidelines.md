# API设计规范
- RESTful接口命名：资源用复数，如`/users`、`/orders`。
- GET用于查询，POST用于新增，PUT用于修改，DELETE用于删除。
- 所有接口返回结构{
  "code": 0,
  "message": "success",
  "data": { ... }
  }
- 异常码、错误信息详见`exception_handling.md`
- 所有响应均带有traceId用于链路追踪