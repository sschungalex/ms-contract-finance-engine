# 合同财务引擎数据库 DDL 脚本

## 概述

本目录包含了合同财务引擎系统的 PostgreSQL 数据库 DDL 脚本，用于创建完整的数据库结构。

## 文件说明

### 核心 DDL 脚本

| 文件名 | 说明 | 依赖关系 |
|--------|------|----------|
| `00_create_all_tables.sql` | 主脚本，按顺序执行所有DDL | 执行所有其他脚本 |
| `01_create_contracts_table.sql` | 合同主表及枚举类型 | 无 |
| `02_create_contract_fields_table.sql` | 合同字段提取结果表 | contracts |
| `03_create_payment_schedules_table.sql` | 支付计划表 | contracts |
| `04_create_amortization_schedules_table.sql` | 摊销计划表 | contracts |
| `05_create_journal_entries_table.sql` | 会计分录表 | contracts, payment_schedules, amortization_schedules |
| `06_create_journal_entry_lines_table.sql` | 会计分录明细表 | journal_entries |

### 辅助脚本

| 文件名 | 说明 |
|--------|------|
| `07_create_indexes_and_constraints.sql` | 额外索引、约束和视图 |
| `08_insert_sample_data.sql` | 示例数据插入脚本 |

## 数据库结构

### 表关系图

```
contracts (合同主表)
├── contract_fields (合同字段)
├── payment_schedules (支付计划)
├── amortization_schedules (摊销计划)
└── journal_entries (会计分录)
    └── journal_entry_lines (会计分录明细)
```

### 枚举类型

| 枚举名称 | 值 | 说明 |
|----------|-----|------|
| `contract_status` | DRAFT, ACTIVE, COMPLETED, CANCELLED, EXPIRED | 合同状态 |
| `ai_processing_status` | PENDING, PROCESSING, COMPLETED, FAILED | AI处理状态 |
| `payment_frequency` | ONCE, MONTHLY, QUARTERLY, YEARLY | 支付频率 |
| `service_period_type` | ONCE, WEEKLY, MONTHLY, QUARTERLY, YEARLY | 服务周期类型 |
| `payment_status` | PENDING, PAID, OVERDUE, PARTIAL, CANCELLED | 支付状态 |
| `amortization_status` | PENDING, IN_PROGRESS, COMPLETED, CANCELLED | 摊销状态 |
| `entry_status` | DRAFT, POSTED, CANCELLED | 分录状态 |
| `entry_type` | PAYMENT, AMORTIZATION, ADJUSTMENT, REVERSAL | 分录类型 |

## 执行方式

### 方式一：执行主脚本（推荐）

```bash
psql -d your_database -f 00_create_all_tables.sql
```

### 方式二：逐个执行

```bash
psql -d your_database -f 01_create_contracts_table.sql
psql -d your_database -f 02_create_contract_fields_table.sql
psql -d your_database -f 03_create_payment_schedules_table.sql
psql -d your_database -f 04_create_amortization_schedules_table.sql
psql -d your_database -f 05_create_journal_entries_table.sql
psql -d your_database -f 06_create_journal_entry_lines_table.sql
```

### 创建额外索引和约束

```bash
psql -d your_database -f 07_create_indexes_and_constraints.sql
```

### 插入示例数据

```bash
psql -d your_database -f 08_insert_sample_data.sql
```

## 主要特性

### 1. 数据完整性

- **外键约束**：确保表间引用完整性
- **检查约束**：验证业务规则（如金额必须为正数）
- **唯一约束**：防止重复数据
- **非空约束**：确保关键字段不为空

### 2. 性能优化

- **主键索引**：所有表都有自增主键
- **外键索引**：所有外键字段都有索引
- **业务索引**：基于查询模式创建的复合索引
- **部分索引**：针对特定条件的优化索引

### 3. 审计功能

- **时间戳**：所有表都有 created_at 和 updated_at 字段
- **自动更新**：通过触发器自动更新 updated_at 字段
- **创建人追踪**：记录数据创建者

### 4. JSON 支持

- **灵活存储**：使用 TEXT 字段存储 JSON 数据
- **结构化数据**：支持复杂的嵌套数据结构
- **扩展性**：便于未来添加新字段

## 业务视图

### v_contract_summary

合同摘要视图，包含：
- 合同基本信息
- 支付计划统计
- 摊销计划统计
- 会计分录统计
- 实际支付金额汇总

### v_journal_entry_balance_check

会计分录平衡检查视图，用于：
- 验证分录借贷平衡
- 检查分录状态正确性
- 审计分录数据完整性

## 注意事项

1. **执行顺序**：必须按照依赖关系顺序执行脚本
2. **权限要求**：需要数据库创建表、索引、触发器的权限
3. **字符编码**：建议使用 UTF8 编码
4. **事务处理**：主脚本使用事务确保原子性
5. **备份建议**：执行前建议备份现有数据

## 维护建议

1. **定期分析**：运行 `ANALYZE` 更新统计信息
2. **索引维护**：监控索引使用情况，及时调整
3. **约束检查**：定期检查数据完整性
4. **性能监控**：监控慢查询，优化索引策略

## 版本兼容性

- **PostgreSQL 版本**：12.0 及以上
- **JPA 兼容性**：与 Spring Boot JPA 注解完全兼容
- **字段映射**：严格按照 Entity 类注解生成
