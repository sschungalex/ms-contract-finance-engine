-- 示例数据插入脚本
-- PostgreSQL 数据库

-- 插入示例合同数据
INSERT INTO contracts (
    contract_number, contract_name, contract_type, counterparty,
    contract_date, start_date, end_date, status,
    total_amount, currency, payment_frequency, payment_method,
    service_period_type, service_duration, service_description,
    created_by
) VALUES 
(
    'CT202501001', 
    '软件开发服务合同', 
    '服务合同', 
    '北京科技有限公司',
    '2025-01-01', 
    '2025-01-01 00:00:00', 
    '2025-12-31 23:59:59', 
    'ACTIVE',
    120000.00, 
    'CNY', 
    'MONTHLY', 
    '银行转账',
    'MONTHLY', 
    12, 
    '软件系统开发及维护服务',
    'system'
),
(
    'CT202501002', 
    '咨询服务合同', 
    '咨询合同', 
    '上海咨询集团',
    '2025-01-15', 
    '2025-01-15 00:00:00', 
    '2025-06-30 23:59:59', 
    'ACTIVE',
    60000.00, 
    'CNY', 
    'QUARTERLY', 
    '银行转账',
    'QUARTERLY', 
    2, 
    '管理咨询服务',
    'system'
);

-- 插入支付计划数据
INSERT INTO payment_schedules (
    contract_id, period_number, scheduled_date, scheduled_amount,
    actual_date, actual_amount, payment_method, payment_description, status
) VALUES 
-- 第一个合同的支付计划
(1, 1, '2025-01-31', 10000.00, '2025-01-31', 10000.00, '银行转账', '第1期付款', 'PAID'),
(1, 2, '2025-02-28', 10000.00, '2025-02-28', 10000.00, '银行转账', '第2期付款', 'PAID'),
(1, 3, '2025-03-31', 10000.00, NULL, NULL, NULL, '第3期付款', 'PENDING'),
(1, 4, '2025-04-30', 10000.00, NULL, NULL, NULL, '第4期付款', 'PENDING'),
-- 第二个合同的支付计划
(2, 1, '2025-03-31', 30000.00, '2025-03-31', 30000.00, '银行转账', '第1季度付款', 'PAID'),
(2, 2, '2025-06-30', 30000.00, NULL, NULL, NULL, '第2季度付款', 'PENDING');

-- 插入摊销计划数据
INSERT INTO amortization_schedules (
    contract_id, period_number, period, accrual_period, accounting_period,
    amortization_date, amortization_amount, accumulated_amount, remaining_amount,
    status, generated_time
) VALUES 
-- 第一个合同的摊销计划
(1, 1, '2025-01', '2025年1月', '2025-01', '2025-01-31', 10000.00, 10000.00, 110000.00, 'COMPLETED', '2025-01-01 10:00:00'),
(1, 2, '2025-02', '2025年2月', '2025-02', '2025-02-28', 10000.00, 20000.00, 100000.00, 'COMPLETED', '2025-01-01 10:00:00'),
(1, 3, '2025-03', '2025年3月', '2025-03', '2025-03-31', 10000.00, 30000.00, 90000.00, 'PENDING', '2025-01-01 10:00:00'),
(1, 4, '2025-04', '2025年4月', '2025-04', '2025-04-30', 10000.00, 40000.00, 80000.00, 'PENDING', '2025-01-01 10:00:00'),
-- 第二个合同的摊销计划
(2, 1, '2025-Q1', '2025年第1季度', '2025-03', '2025-03-31', 30000.00, 30000.00, 30000.00, 'COMPLETED', '2025-01-15 10:00:00'),
(2, 2, '2025-Q2', '2025年第2季度', '2025-06', '2025-06-30', 30000.00, 60000.00, 0.00, 'PENDING', '2025-01-15 10:00:00');

-- 插入会计分录数据
INSERT INTO journal_entries (
    contract_id, entry_number, entry_id, entry_date, booking_date,
    entry_type, description, reference, total_amount, total_dr, total_cr,
    balanced, status, payment_schedule_id, created_by
) VALUES 
-- 第一个合同的付款分录
(1, 'JE-2025-001', 'JE-2025-001', '2025-01-31', '2025-01-31', 
 'PAYMENT', '合同预付款确认', 'Contract-1-Payment-1', 10000.00, 10000.00, 10000.00, 
 true, 'POSTED', 1, 'system'),
(1, 'JE-2025-002', 'JE-2025-002', '2025-02-28', '2025-02-28', 
 'PAYMENT', '合同预付款确认', 'Contract-1-Payment-2', 10000.00, 10000.00, 10000.00, 
 true, 'POSTED', 2, 'system'),
-- 第一个合同的摊销分录
(1, 'JE-2025-003', 'JE-2025-003', '2025-01-31', '2025-01-31', 
 'AMORTIZATION', '1月份服务费用摊销', 'Contract-1-Amortization-2025-01', 10000.00, 10000.00, 10000.00, 
 true, 'POSTED', NULL, 'system'),
(1, 'JE-2025-004', 'JE-2025-004', '2025-02-28', '2025-02-28', 
 'AMORTIZATION', '2月份服务费用摊销', 'Contract-1-Amortization-2025-02', 10000.00, 10000.00, 10000.00, 
 true, 'POSTED', NULL, 'system');

-- 插入会计分录明细数据
INSERT INTO journal_entry_lines (
    journal_entry_id, line_number, booking_date, gl_account, gl_account_name,
    debit_amount, credit_amount, entered_dr, entered_cr, description
) VALUES 
-- JE-2025-001 的明细行
(1, 1, '2025-01-31', '1221', '预付账款', 10000.00, 0.00, 10000.00, 0.00, '确认预付服务费'),
(1, 2, '2025-01-31', '1001', '活期存款', 0.00, 10000.00, 0.00, 10000.00, '银行转账付款'),
-- JE-2025-002 的明细行
(2, 1, '2025-02-28', '1221', '预付账款', 10000.00, 0.00, 10000.00, 0.00, '确认预付服务费'),
(2, 2, '2025-02-28', '1001', '活期存款', 0.00, 10000.00, 0.00, 10000.00, '银行转账付款'),
-- JE-2025-003 的明细行
(3, 1, '2025-01-31', '6001', '服务费用', 10000.00, 0.00, 10000.00, 0.00, '1月份服务费摊销'),
(3, 2, '2025-01-31', '1221', '预付账款', 0.00, 10000.00, 0.00, 10000.00, '预付账款摊销'),
-- JE-2025-004 的明细行
(4, 1, '2025-02-28', '6001', '服务费用', 10000.00, 0.00, 10000.00, 0.00, '2月份服务费摊销'),
(4, 2, '2025-02-28', '1221', '预付账款', 0.00, 10000.00, 0.00, 10000.00, '预付账款摊销');

-- 插入合同字段提取结果数据
INSERT INTO contract_fields (
    contract_id, field_name, field_value, field_type, confidence_score, extraction_method
) VALUES 
(1, 'total_amount', '120000.00', 'DECIMAL', 0.9500, 'AI_EXTRACTION'),
(1, 'contract_period', '12个月', 'TEXT', 0.9200, 'AI_EXTRACTION'),
(1, 'payment_method', '银行转账', 'TEXT', 0.8800, 'AI_EXTRACTION'),
(1, 'service_description', '软件系统开发及维护服务', 'TEXT', 0.9100, 'AI_EXTRACTION'),
(2, 'total_amount', '60000.00', 'DECIMAL', 0.9300, 'AI_EXTRACTION'),
(2, 'contract_period', '6个月', 'TEXT', 0.9000, 'AI_EXTRACTION'),
(2, 'payment_method', '银行转账', 'TEXT', 0.8500, 'AI_EXTRACTION'),
(2, 'service_description', '管理咨询服务', 'TEXT', 0.8900, 'AI_EXTRACTION');

-- 验证插入的数据
SELECT '=== 合同数据 ===' as info;
SELECT contract_number, contract_name, counterparty, total_amount, status FROM contracts;

SELECT '=== 支付计划数据 ===' as info;
SELECT c.contract_number, ps.period_number, ps.scheduled_date, ps.scheduled_amount, ps.status 
FROM payment_schedules ps 
JOIN contracts c ON ps.contract_id = c.id 
ORDER BY c.id, ps.period_number;

SELECT '=== 摊销计划数据 ===' as info;
SELECT c.contract_number, ams.period_number, ams.period, ams.amortization_amount, ams.status 
FROM amortization_schedules ams 
JOIN contracts c ON ams.contract_id = c.id 
ORDER BY c.id, ams.period_number;

SELECT '=== 会计分录数据 ===' as info;
SELECT c.contract_number, je.entry_number, je.entry_type, je.description, je.total_amount, je.balanced 
FROM journal_entries je 
JOIN contracts c ON je.contract_id = c.id 
ORDER BY c.id, je.entry_date;

ECHO '示例数据插入完成！';
