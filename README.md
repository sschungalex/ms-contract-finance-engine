# 合同分析引擎 (Contract Engine)

智能合同分析与会计分录生成系统

## 项目概述

本系统是一个基于Spring Boot的智能合同分析系统，能够自动分析上传的合同文档，提取关键字段信息，并生成相应的支付计划、摊销计划和会计分录。

## 核心功能

### 1. 合同分析与字段提取
- 支持PDF、Word、TXT等格式的合同文档上传
- 自动提取合同关键字段：金额、周期、支付方式、合同期限等
- 支持多种合同模板的智能识别
- 提供字段提取置信度评分

### 2. 支付计划生成
- 根据合同条款自动生成支付时间表
- 支持多种支付频率：月付、季付、半年付、年付、一次性
- 支持跨期支付计划
- 实际支付与计划支付的差异分析

### 3. 摊销计划生成
- 自动生成合同费用摊销计划
- 支持跨期摊销处理
- 累计摊销金额和剩余金额跟踪

### 4. 会计分录自动生成
- 根据支付计划和摊销计划自动生成会计分录
- 支持借贷平衡验证
- 差额分录处理（多付/少付）
- 可配置的分录规则引擎

## 技术架构

### 技术栈
- **后端框架**: Spring Boot 3.2.0
- **数据库**: PostgreSQL
- **缓存**: Redis
- **文档处理**: Apache PDFBox, Apache POI
- **API文档**: SpringDoc OpenAPI 3
- **构建工具**: Maven
- **Java版本**: 17

### 项目结构
```
src/main/java/com/windsurf/contractengine/
├── ContractEngineApplication.java          # 主应用程序类
├── controller/                             # 控制器层
├── service/                               # 服务层
├── repository/                            # 数据访问层
├── entity/                               # 实体类
│   ├── Contract.java                     # 合同实体
│   ├── ContractField.java               # 合同字段实体
│   ├── PaymentSchedule.java             # 支付计划实体
│   ├── AmortizationSchedule.java        # 摊销计划实体
│   ├── JournalEntry.java                # 会计分录实体
│   └── JournalEntryLine.java            # 会计分录明细实体
├── dto/                                  # 数据传输对象
├── config/                               # 配置类
├── exception/                            # 异常处理
└── util/                                 # 工具类
```

## 数据库设计

### 核心表结构
1. **contracts** - 合同主表
2. **contract_fields** - 合同字段提取结果
3. **payment_schedules** - 支付计划
4. **amortization_schedules** - 摊销计划
5. **journal_entries** - 会计分录
6. **journal_entry_lines** - 会计分录明细

## API接口设计

### 合同管理
- `POST /api/v1/contracts/upload` - 上传合同文档
- `GET /api/v1/contracts` - 获取合同列表
- `GET /api/v1/contracts/{id}` - 获取合同详情
- `PUT /api/v1/contracts/{id}` - 更新合同信息
- `DELETE /api/v1/contracts/{id}` - 删除合同

### 字段提取
- `POST /api/v1/contracts/{id}/extract` - 执行字段提取
- `GET /api/v1/contracts/{id}/fields` - 获取提取的字段

### 计划生成
- `POST /api/v1/contracts/{id}/payment-schedule` - 生成支付计划
- `POST /api/v1/contracts/{id}/amortization-schedule` - 生成摊销计划
- `GET /api/v1/contracts/{id}/schedules` - 获取所有计划

### 会计分录
- `POST /api/v1/contracts/{id}/journal-entries` - 生成会计分录
- `GET /api/v1/journal-entries` - 获取分录列表
- `POST /api/v1/journal-entries/{id}/post` - 过账分录

## 快速开始

### 环境要求
- Java 17+
- PostgreSQL 12+
- Redis 6+
- Maven 3.8+

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd contract-engine
```

2. **配置数据库**
```sql
CREATE DATABASE contract_engine;
CREATE USER contract_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE contract_engine TO contract_user;
```

3. **配置环境变量**
```bash
export DB_USERNAME=contract_user
export DB_PASSWORD=your_password
```

4. **启动应用**
```bash
mvn spring-boot:run
```

5. **访问API文档**
打开浏览器访问: http://localhost:8080/api/swagger-ui.html

## 配置说明

### 应用配置 (application.yml)
- 数据库连接配置
- Redis缓存配置
- 文件上传配置
- 日志配置
- API文档配置

### 自定义配置
- 文件上传目录和大小限制
- 字段提取置信度阈值
- 会计科目配置
- 分录规则配置

## 开发指南

### 添加新的合同模板
1. 在字段提取服务中添加新的模板识别规则
2. 配置对应的字段映射关系
3. 更新测试用例

### 自定义分录规则
1. 实现 `AccountingRuleEngine` 接口
2. 配置规则优先级和适用条件
3. 注册到Spring容器中

## 监控与运维

### 健康检查
- `/api/actuator/health` - 应用健康状态
- `/api/actuator/metrics` - 应用指标

### 日志
- 应用日志: `logs/contract-engine.log`
- 日志级别可通过配置文件调整

## 许可证

Copyright © 2024 Windsurf Team. All rights reserved.
