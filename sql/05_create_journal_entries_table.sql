-- 会计分录表 DDL
-- PostgreSQL 数据库

-- 创建分录状态枚举类型
CREATE TYPE entry_status AS ENUM (
    'DRAFT',
    'POSTED',
    'CANCELLED'
);

-- 创建分录类型枚举类型
CREATE TYPE entry_type AS ENUM (
    'PAYMENT',
    'AMORTIZATION',
    'ADJUSTMENT',
    'REVERSAL'
);

-- 创建会计分录表
CREATE TABLE journal_entries (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    entry_number VARCHAR(100) UNIQUE NOT NULL,
    entry_id VARCHAR(100) UNIQUE,
    entry_date DATE NOT NULL,
    booking_date DATE,
    entry_type entry_type NOT NULL,
    description VARCHAR(500),
    reference VARCHAR(200),
    total_amount DECIMAL(15,2) NOT NULL,
    total_dr DECIMAL(15,2) DEFAULT 0,
    total_cr DECIMAL(15,2) DEFAULT 0,
    balanced BOOLEAN DEFAULT FALSE,
    status entry_status NOT NULL DEFAULT 'DRAFT',
    payment_schedule_id BIGINT,
    amortization_schedule_id BIGINT,
    remarks VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- 外键约束
    CONSTRAINT fk_journal_entries_contract_id 
        FOREIGN KEY (contract_id) 
        REFERENCES contracts(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_journal_entries_payment_schedule_id 
        FOREIGN KEY (payment_schedule_id) 
        REFERENCES payment_schedules(id) 
        ON DELETE SET NULL,
    CONSTRAINT fk_journal_entries_amortization_schedule_id 
        FOREIGN KEY (amortization_schedule_id) 
        REFERENCES amortization_schedules(id) 
        ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_journal_entries_contract_id ON journal_entries(contract_id);
CREATE INDEX idx_journal_entries_entry_number ON journal_entries(entry_number);
CREATE INDEX idx_journal_entries_entry_id ON journal_entries(entry_id);
CREATE INDEX idx_journal_entries_entry_date ON journal_entries(entry_date);
CREATE INDEX idx_journal_entries_booking_date ON journal_entries(booking_date);
CREATE INDEX idx_journal_entries_entry_type ON journal_entries(entry_type);
CREATE INDEX idx_journal_entries_status ON journal_entries(status);
CREATE INDEX idx_journal_entries_payment_schedule_id ON journal_entries(payment_schedule_id);
CREATE INDEX idx_journal_entries_amortization_schedule_id ON journal_entries(amortization_schedule_id);
CREATE INDEX idx_journal_entries_created_at ON journal_entries(created_at);

-- 创建复合索引
CREATE INDEX idx_journal_entries_contract_type ON journal_entries(contract_id, entry_type);
CREATE INDEX idx_journal_entries_contract_status ON journal_entries(contract_id, status);
CREATE INDEX idx_journal_entries_date_range ON journal_entries(entry_date, booking_date);

-- 添加注释
COMMENT ON TABLE journal_entries IS '会计分录表';
COMMENT ON COLUMN journal_entries.id IS '主键ID';
COMMENT ON COLUMN journal_entries.contract_id IS '关联的合同ID';
COMMENT ON COLUMN journal_entries.entry_number IS '分录编号';
COMMENT ON COLUMN journal_entries.entry_id IS '分录ID(API中的entryId)';
COMMENT ON COLUMN journal_entries.entry_date IS '分录日期';
COMMENT ON COLUMN journal_entries.booking_date IS '记账日期';
COMMENT ON COLUMN journal_entries.entry_type IS '分录类型';
COMMENT ON COLUMN journal_entries.description IS '分录描述';
COMMENT ON COLUMN journal_entries.reference IS '参考号';
COMMENT ON COLUMN journal_entries.total_amount IS '总金额';
COMMENT ON COLUMN journal_entries.total_dr IS '借方总金额';
COMMENT ON COLUMN journal_entries.total_cr IS '贷方总金额';
COMMENT ON COLUMN journal_entries.balanced IS '是否平衡';
COMMENT ON COLUMN journal_entries.status IS '分录状态';
COMMENT ON COLUMN journal_entries.payment_schedule_id IS '关联的支付计划ID';
COMMENT ON COLUMN journal_entries.amortization_schedule_id IS '关联的摊销计划ID';
COMMENT ON COLUMN journal_entries.remarks IS '备注';
COMMENT ON COLUMN journal_entries.created_at IS '创建时间';
COMMENT ON COLUMN journal_entries.updated_at IS '更新时间';
COMMENT ON COLUMN journal_entries.created_by IS '创建人';

-- 创建更新时间触发器
CREATE TRIGGER update_journal_entries_updated_at 
    BEFORE UPDATE ON journal_entries 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
