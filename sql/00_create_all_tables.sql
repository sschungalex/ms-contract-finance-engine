-- 合同财务引擎数据库 DDL 主脚本
-- PostgreSQL 数据库
-- 
-- 执行顺序：
-- 1. 创建枚举类型和基础表
-- 2. 创建关联表
-- 3. 创建索引和约束
-- 4. 添加注释和触发器

-- 设置客户端编码
SET client_encoding = 'UTF8';

-- 开始事务
BEGIN;

-- 1. 创建合同表（包含所有枚举类型）
\i 01_create_contracts_table.sql

-- 2. 创建合同字段表
\i 02_create_contract_fields_table.sql

-- 3. 创建支付计划表
\i 03_create_payment_schedules_table.sql

-- 4. 创建摊销计划表
\i 04_create_amortization_schedules_table.sql

-- 5. 创建会计分录表
\i 05_create_journal_entries_table.sql

-- 6. 创建会计分录明细表
\i 06_create_journal_entry_lines_table.sql

-- 提交事务
COMMIT;

-- 显示创建的表
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'public' 
    AND tablename IN (
        'contracts',
        'contract_fields', 
        'payment_schedules',
        'amortization_schedules',
        'journal_entries',
        'journal_entry_lines'
    )
ORDER BY tablename;

-- 显示创建的枚举类型
SELECT 
    n.nspname AS schema_name,
    t.typname AS enum_name,
    array_agg(e.enumlabel ORDER BY e.enumsortorder) AS enum_values
FROM pg_type t
JOIN pg_enum e ON t.oid = e.enumtypid
JOIN pg_namespace n ON n.oid = t.typnamespace
WHERE n.nspname = 'public'
    AND t.typname IN (
        'contract_status',
        'ai_processing_status', 
        'payment_frequency',
        'service_period_type',
        'payment_status',
        'amortization_status',
        'entry_status',
        'entry_type'
    )
GROUP BY n.nspname, t.typname
ORDER BY t.typname;

ECHO '数据库表创建完成！';
