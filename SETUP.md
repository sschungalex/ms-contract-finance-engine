# 合同分析引擎 - 项目启动指南

## 🚀 快速启动

### 环境要求
- **Java**: 17 或更高版本
- **Maven**: 3.8+ 
- **PostgreSQL**: 12+ 
- **Redis**: 6+ (可选，用于缓存)

### 1. 数据库配置

#### 创建数据库
```sql
-- 连接到PostgreSQL
psql -U postgres

-- 创建数据库和用户
CREATE DATABASE contract_engine;
CREATE USER contract_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE contract_engine TO contract_user;

-- 切换到新数据库
\c contract_engine

-- 授予schema权限
GRANT ALL ON SCHEMA public TO contract_user;
```

#### 配置环境变量
```bash
# 创建 .env 文件或设置环境变量
export DB_USERNAME=contract_user
export DB_PASSWORD=your_password
```

### 2. 项目配置

#### 复制配置文件
```bash
# 复制配置文件模板
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

#### 修改配置文件
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/contract_engine
    username: ${DB_USERNAME:contract_user}
    password: ${DB_PASSWORD:your_password}
```

### 3. 启动应用

#### 使用Maven启动
```bash
# 清理并编译
mvn clean compile

# 启动应用
mvn spring-boot:run
```

#### 使用IDE启动
1. 导入项目到IDE (IntelliJ IDEA 或 Eclipse)
2. 运行 `ContractEngineApplication.java`

### 4. 验证启动

#### 检查应用状态
```bash
# 健康检查
curl http://localhost:8080/api/actuator/health

# 预期响应
{
  "status": "UP"
}
```

#### 访问API文档
打开浏览器访问: http://localhost:8080/api/swagger-ui.html

## 📁 项目结构说明

```
src/main/java/com/windsurf/contractengine/
├── ContractEngineApplication.java          # 主应用程序类
├── controller/                             # REST控制器
│   └── ContractController.java            # 合同管理API
├── service/                               # 业务服务层
│   ├── ContractService.java              # 合同服务接口
│   ├── FieldExtractionService.java       # 字段提取服务
│   ├── ScheduleGenerationService.java    # 计划生成服务
│   └── AccountingService.java            # 会计分录服务
├── repository/                            # 数据访问层
│   ├── ContractRepository.java           # 合同数据访问
│   └── ContractFieldRepository.java      # 字段数据访问
├── entity/                               # JPA实体类
│   ├── Contract.java                     # 合同实体
│   ├── ContractField.java               # 合同字段实体
│   ├── PaymentSchedule.java             # 支付计划实体
│   ├── AmortizationSchedule.java        # 摊销计划实体
│   ├── JournalEntry.java                # 会计分录实体
│   └── JournalEntryLine.java            # 分录明细实体
├── dto/                                  # 数据传输对象
│   ├── ContractCreateRequest.java       # 合同创建请求
│   ├── ContractUpdateRequest.java       # 合同更新请求
│   ├── ContractResponse.java            # 合同响应
│   └── PageResponse.java                # 分页响应
├── config/                               # 配置类
│   └── FileUploadConfig.java            # 文件上传配置
├── exception/                            # 异常处理
│   ├── GlobalExceptionHandler.java      # 全局异常处理器
│   ├── BusinessException.java           # 业务异常
│   ├── ResourceNotFoundException.java   # 资源未找到异常
│   └── ErrorResponse.java               # 错误响应
└── util/                                 # 工具类
```

## 🔧 开发指南

### 添加新的合同模板支持

1. **定义模板规则**
```java
// 在FieldExtractionService实现中添加新模板
public Map<String, Object> getExtractionTemplate(String contractType) {
    switch (contractType) {
        case "SERVICE_CONTRACT":
            return createServiceContractTemplate();
        case "PURCHASE_CONTRACT":
            return createPurchaseContractTemplate();
        // 添加新的合同类型
        default:
            return createDefaultTemplate();
    }
}
```

2. **配置字段映射**
```java
private Map<String, Object> createServiceContractTemplate() {
    Map<String, Object> template = new HashMap<>();
    template.put("amount_patterns", Arrays.asList(
        "总金额[：:]?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)",
        "合同金额[：:]?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)"
    ));
    template.put("date_patterns", Arrays.asList(
        "签订日期[：:]?\\s*(\\d{4}[年-]\\d{1,2}[月-]\\d{1,2})",
        "合同期限[：:]?\\s*(\\d{4}[年-]\\d{1,2}[月-]\\d{1,2})"
    ));
    return template;
}
```

### 自定义会计分录规则

1. **实现分录规则接口**
```java
@Component
public class CustomAccountingRules implements AccountingRuleEngine {
    
    @Override
    public JournalEntry generatePaymentEntry(PaymentSchedule schedule) {
        // 自定义支付分录逻辑
        return journalEntry;
    }
    
    @Override
    public JournalEntry generateAmortizationEntry(AmortizationSchedule schedule) {
        // 自定义摊销分录逻辑
        return journalEntry;
    }
}
```

2. **配置科目映射**
```yaml
app:
  accounting:
    accounts:
      receivable: "1122"  # 应收账款
      revenue: "6001"     # 主营业务收入
      cash: "1001"        # 库存现金
      prepaid: "1123"     # 预付账款
```

## 🧪 测试

### 运行单元测试
```bash
mvn test
```

### 运行集成测试
```bash
mvn test -P integration-test
```

### API测试示例
```bash
# 上传合同
curl -X POST http://localhost:8080/api/v1/contracts/upload \
  -F "file=@contract.pdf" \
  -F "contractName=测试合同" \
  -F "contractType=服务合同" \
  -F "counterparty=测试公司" \
  -F "totalAmount=100000" \
  -F "paymentFrequency=MONTHLY"

# 查询合同列表
curl http://localhost:8080/api/v1/contracts?page=0&size=10
```

## 📊 监控与运维

### 健康检查端点
- `/api/actuator/health` - 应用健康状态
- `/api/actuator/metrics` - 应用指标
- `/api/actuator/info` - 应用信息

### 日志配置
日志文件位置: `logs/contract-engine.log`

调整日志级别:
```yaml
logging:
  level:
    com.windsurf.contractengine: DEBUG
```

## 🚨 常见问题

### 1. 数据库连接失败
- 检查PostgreSQL服务是否启动
- 验证数据库连接参数
- 确认用户权限设置

### 2. 文件上传失败
- 检查上传目录权限
- 验证文件大小限制
- 确认支持的文件类型

### 3. 内存不足
- 调整JVM堆内存: `-Xmx2g`
- 优化数据库查询
- 启用Redis缓存

## 📞 技术支持

如遇到问题，请：
1. 查看应用日志: `logs/contract-engine.log`
2. 检查数据库连接状态
3. 验证配置文件设置
4. 联系开发团队

---

**版本**: 1.0.0  
**更新时间**: 2024-01-01
