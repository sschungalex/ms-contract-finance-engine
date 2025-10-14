-- 支付计划表 DDL
-- PostgreSQL 数据库

-- 创建支付状态枚举类型
CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'PAID',
    'OVERDUE',
    'PARTIAL',
    'CANCELLED'
);

-- 创建支付计划表
CREATE TABLE payment_schedules (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    period_number INTEGER NOT NULL,
    scheduled_date DATE NOT NULL,
    scheduled_amount DECIMAL(15,2) NOT NULL,
    actual_date DATE,
    actual_amount DECIMAL(15,2),
    payment_method VARCHAR(100),
    payment_description VARCHAR(500),
    status payment_status NOT NULL DEFAULT 'PENDING',
    variance_amount DECIMAL(15,2),
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_payment_schedules_contract_id 
        FOREIGN KEY (contract_id) 
        REFERENCES contracts(id) 
        ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_payment_schedules_contract_id ON payment_schedules(contract_id);
CREATE INDEX idx_payment_schedules_period_number ON payment_schedules(period_number);
CREATE INDEX idx_payment_schedules_scheduled_date ON payment_schedules(scheduled_date);
CREATE INDEX idx_payment_schedules_actual_date ON payment_schedules(actual_date);
CREATE INDEX idx_payment_schedules_status ON payment_schedules(status);
CREATE INDEX idx_payment_schedules_created_at ON payment_schedules(created_at);

-- 创建复合索引
CREATE INDEX idx_payment_schedules_contract_period ON payment_schedules(contract_id, period_number);
CREATE INDEX idx_payment_schedules_contract_status ON payment_schedules(contract_id, status);

-- 添加注释
COMMENT ON TABLE payment_schedules IS '支付计划表';
COMMENT ON COLUMN payment_schedules.id IS '主键ID';
COMMENT ON COLUMN payment_schedules.contract_id IS '关联的合同ID';
COMMENT ON COLUMN payment_schedules.period_number IS '期数';
COMMENT ON COLUMN payment_schedules.scheduled_date IS '计划支付日期';
COMMENT ON COLUMN payment_schedules.scheduled_amount IS '计划支付金额';
COMMENT ON COLUMN payment_schedules.actual_date IS '实际支付日期';
COMMENT ON COLUMN payment_schedules.actual_amount IS '实际支付金额';
COMMENT ON COLUMN payment_schedules.payment_method IS '支付方式';
COMMENT ON COLUMN payment_schedules.payment_description IS '支付描述';
COMMENT ON COLUMN payment_schedules.status IS '支付状态';
COMMENT ON COLUMN payment_schedules.variance_amount IS '差额(实际-计划)';
COMMENT ON COLUMN payment_schedules.remarks IS '备注';
COMMENT ON COLUMN payment_schedules.created_at IS '创建时间';
COMMENT ON COLUMN payment_schedules.updated_at IS '更新时间';

-- 创建更新时间触发器
CREATE TRIGGER update_payment_schedules_updated_at 
    BEFORE UPDATE ON payment_schedules 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
