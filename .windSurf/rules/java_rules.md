# Java/Spring Boot开发规范
- 统一使用JDK 21。
- Controller所有请求参数必须加@Valid，Controller 统一响应结构 'ApiResponse<T>：{ code, message, data }'。
- 所有参数校验失败,业务异常统一用`BizException`，全局用`@RestControllerAdvice`处理。
- 注释风格采用Javadoc，所有对外接口、公共方法须写明用途、参数、返回值。
- 单元测试采用JUnit5，Mock依赖使用Mockito/MockBean。
- Redis/DB操作须加超时和异常处理，避免“吃掉”异常。