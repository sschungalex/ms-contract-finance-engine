-- 摊销计划表 DDL
-- PostgreSQL 数据库

-- 创建摊销状态枚举类型
CREATE TYPE amortization_status AS ENUM (
    'PENDING',
    'IN_PROGRESS',
    'COMPLETED',
    'CANCELLED'
);

-- 创建摊销计划表
CREATE TABLE amortization_schedules (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    period_number INTEGER NOT NULL,
    period VARCHAR(20),
    accrual_period VARCHAR(100),
    accounting_period VARCHAR(20),
    amortization_date DATE NOT NULL,
    amortization_amount DECIMAL(15,2) NOT NULL,
    accumulated_amount DECIMAL(15,2) NOT NULL,
    remaining_amount DECIMAL(15,2) NOT NULL,
    status amortization_status NOT NULL DEFAULT 'PENDING',
    generated_time TIMESTAMP,
    calculation_basis TEXT,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_amortization_schedules_contract_id 
        FOREIGN KEY (contract_id) 
        REFERENCES contracts(id) 
        ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_amortization_schedules_contract_id ON amortization_schedules(contract_id);
CREATE INDEX idx_amortization_schedules_period_number ON amortization_schedules(period_number);
CREATE INDEX idx_amortization_schedules_amortization_date ON amortization_schedules(amortization_date);
CREATE INDEX idx_amortization_schedules_status ON amortization_schedules(status);
CREATE INDEX idx_amortization_schedules_period ON amortization_schedules(period);
CREATE INDEX idx_amortization_schedules_created_at ON amortization_schedules(created_at);

-- 创建复合索引
CREATE INDEX idx_amortization_schedules_contract_period ON amortization_schedules(contract_id, period_number);
CREATE INDEX idx_amortization_schedules_contract_status ON amortization_schedules(contract_id, status);

-- 添加注释
COMMENT ON TABLE amortization_schedules IS '摊销计划表';
COMMENT ON COLUMN amortization_schedules.id IS '主键ID';
COMMENT ON COLUMN amortization_schedules.contract_id IS '关联的合同ID';
COMMENT ON COLUMN amortization_schedules.period_number IS '期数';
COMMENT ON COLUMN amortization_schedules.period IS '预提/摊销期间';
COMMENT ON COLUMN amortization_schedules.accrual_period IS '预提期间详细描述';
COMMENT ON COLUMN amortization_schedules.accounting_period IS '入账期间';
COMMENT ON COLUMN amortization_schedules.amortization_date IS '摊销日期';
COMMENT ON COLUMN amortization_schedules.amortization_amount IS '摊销金额';
COMMENT ON COLUMN amortization_schedules.accumulated_amount IS '累计摊销金额';
COMMENT ON COLUMN amortization_schedules.remaining_amount IS '剩余金额';
COMMENT ON COLUMN amortization_schedules.status IS '摊销状态';
COMMENT ON COLUMN amortization_schedules.generated_time IS '生成时间';
COMMENT ON COLUMN amortization_schedules.calculation_basis IS '计算依据(JSON格式)';
COMMENT ON COLUMN amortization_schedules.remarks IS '备注';
COMMENT ON COLUMN amortization_schedules.created_at IS '创建时间';
COMMENT ON COLUMN amortization_schedules.updated_at IS '更新时间';

-- 创建更新时间触发器
CREATE TRIGGER update_amortization_schedules_updated_at 
    BEFORE UPDATE ON amortization_schedules 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
