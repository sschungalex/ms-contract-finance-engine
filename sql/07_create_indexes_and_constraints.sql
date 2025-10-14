-- 额外的索引和约束 DDL
-- PostgreSQL 数据库

-- 为性能优化创建额外的复合索引

-- 合同表的复合索引
CREATE INDEX IF NOT EXISTS idx_contracts_status_date 
    ON contracts(status, contract_date);
CREATE INDEX IF NOT EXISTS idx_contracts_counterparty_status 
    ON contracts(counterparty, status);
CREATE INDEX IF NOT EXISTS idx_contracts_amount_range 
    ON contracts(total_amount, currency);

-- 支付计划表的复合索引
CREATE INDEX IF NOT EXISTS idx_payment_schedules_date_status 
    ON payment_schedules(scheduled_date, status);
CREATE INDEX IF NOT EXISTS idx_payment_schedules_actual_date_amount 
    ON payment_schedules(actual_date, actual_amount) 
    WHERE actual_date IS NOT NULL;

-- 摊销计划表的复合索引
CREATE INDEX IF NOT EXISTS idx_amortization_schedules_date_status 
    ON amortization_schedules(amortization_date, status);
CREATE INDEX IF NOT EXISTS idx_amortization_schedules_period_date 
    ON amortization_schedules(period, amortization_date);

-- 会计分录表的复合索引
CREATE INDEX IF NOT EXISTS idx_journal_entries_date_type_status 
    ON journal_entries(entry_date, entry_type, status);
CREATE INDEX IF NOT EXISTS idx_journal_entries_reference_date 
    ON journal_entries(reference, entry_date) 
    WHERE reference IS NOT NULL;

-- 会计分录明细表的复合索引
CREATE INDEX IF NOT EXISTS idx_journal_entry_lines_gl_date_amount 
    ON journal_entry_lines(gl_account, booking_date, debit_amount, credit_amount);

-- 添加业务逻辑约束

-- 合同表约束
ALTER TABLE contracts 
ADD CONSTRAINT chk_contracts_date_range 
CHECK (start_date <= end_date);

ALTER TABLE contracts 
ADD CONSTRAINT chk_contracts_total_amount_positive 
CHECK (total_amount > 0);

ALTER TABLE contracts 
ADD CONSTRAINT chk_contracts_ai_confidence_range 
CHECK (ai_confidence IS NULL OR (ai_confidence >= 0 AND ai_confidence <= 1));

-- 支付计划表约束
ALTER TABLE payment_schedules 
ADD CONSTRAINT chk_payment_schedules_scheduled_amount_positive 
CHECK (scheduled_amount > 0);

ALTER TABLE payment_schedules 
ADD CONSTRAINT chk_payment_schedules_actual_amount_positive 
CHECK (actual_amount IS NULL OR actual_amount > 0);

ALTER TABLE payment_schedules 
ADD CONSTRAINT chk_payment_schedules_period_positive 
CHECK (period_number > 0);

-- 摊销计划表约束
ALTER TABLE amortization_schedules 
ADD CONSTRAINT chk_amortization_schedules_amounts_positive 
CHECK (
    amortization_amount >= 0 AND 
    accumulated_amount >= 0 AND 
    remaining_amount >= 0
);

ALTER TABLE amortization_schedules 
ADD CONSTRAINT chk_amortization_schedules_period_positive 
CHECK (period_number > 0);

-- 会计分录表约束
ALTER TABLE journal_entries 
ADD CONSTRAINT chk_journal_entries_total_amount_positive 
CHECK (total_amount >= 0);

ALTER TABLE journal_entries 
ADD CONSTRAINT chk_journal_entries_dr_cr_positive 
CHECK (total_dr >= 0 AND total_cr >= 0);

-- 会计分录明细表约束（已在创建表时添加）

-- 创建视图用于常用查询

-- 合同摘要视图
CREATE OR REPLACE VIEW v_contract_summary AS
SELECT 
    c.id,
    c.contract_number,
    c.contract_name,
    c.counterparty,
    c.total_amount,
    c.currency,
    c.status,
    c.contract_date,
    COUNT(ps.id) as payment_schedule_count,
    COUNT(ams.id) as amortization_schedule_count,
    COUNT(je.id) as journal_entry_count,
    COALESCE(SUM(ps.actual_amount), 0) as total_paid_amount,
    COALESCE(SUM(ams.amortization_amount), 0) as total_amortized_amount
FROM contracts c
LEFT JOIN payment_schedules ps ON c.id = ps.contract_id AND ps.status = 'PAID'
LEFT JOIN amortization_schedules ams ON c.id = ams.contract_id AND ams.status = 'COMPLETED'
LEFT JOIN journal_entries je ON c.id = je.contract_id AND je.status = 'POSTED'
GROUP BY c.id, c.contract_number, c.contract_name, c.counterparty, 
         c.total_amount, c.currency, c.status, c.contract_date;

-- 会计分录平衡检查视图
CREATE OR REPLACE VIEW v_journal_entry_balance_check AS
SELECT 
    je.id,
    je.entry_number,
    je.entry_id,
    je.total_dr,
    je.total_cr,
    je.balanced,
    COALESCE(SUM(jel.debit_amount), 0) as calculated_dr,
    COALESCE(SUM(jel.credit_amount), 0) as calculated_cr,
    (COALESCE(SUM(jel.debit_amount), 0) = COALESCE(SUM(jel.credit_amount), 0)) as is_balanced,
    (je.balanced = (COALESCE(SUM(jel.debit_amount), 0) = COALESCE(SUM(jel.credit_amount), 0))) as balance_status_correct
FROM journal_entries je
LEFT JOIN journal_entry_lines jel ON je.id = jel.journal_entry_id
GROUP BY je.id, je.entry_number, je.entry_id, je.total_dr, je.total_cr, je.balanced;

-- 添加视图注释
COMMENT ON VIEW v_contract_summary IS '合同摘要视图，包含支付、摊销和分录统计信息';
COMMENT ON VIEW v_journal_entry_balance_check IS '会计分录平衡检查视图，用于验证分录借贷平衡';
