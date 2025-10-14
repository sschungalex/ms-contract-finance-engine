-- 合同表 DDL
-- PostgreSQL 数据库

-- 创建合同状态枚举类型
CREATE TYPE contract_status AS ENUM (
    'DRAFT',
    'ACTIVE', 
    'COMPLETED',
    'CANCELLED',
    'EXPIRED'
);

-- 创建AI处理状态枚举类型
CREATE TYPE ai_processing_status AS ENUM (
    'PENDING',
    'PROCESSING',
    'COMPLETED',
    'FAILED'
);

-- 创建支付频率枚举类型
CREATE TYPE payment_frequency AS ENUM (
    'ONCE',
    'MONTHLY',
    'QUARTERLY',
    'YEARLY'
);

-- 创建服务周期类型枚举类型
CREATE TYPE service_period_type AS ENUM (
    'ONCE',
    'WEEKLY',
    'MONTHLY', 
    'QUARTERLY',
    'YEARLY'
);

-- 创建合同表
CREATE TABLE contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(100) UNIQUE NOT NULL,
    contract_name VARCHAR(200) NOT NULL,
    contract_type VARCHAR(50) NOT NULL,
    counterparty VARCHAR(200) NOT NULL,
    contract_date DATE,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status contract_status NOT NULL DEFAULT 'DRAFT',
    ai_processing_status ai_processing_status DEFAULT 'PROCESSING',
    upload_time TIMESTAMP,
    ai_confidence DECIMAL(5,4),
    total_amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    payment_frequency payment_frequency NOT NULL,
    payment_method VARCHAR(50),
    tax_rate DECIMAL(5,2),
    payment_dates TEXT,
    parties TEXT,
    amount_elements TEXT,
    time_elements TEXT,
    unit_price DECIMAL(15,2),
    quantity INTEGER,
    service_period_type service_period_type,
    service_duration INTEGER,
    service_description VARCHAR(500),
    remarks VARCHAR(500),
    original_filename VARCHAR(255),
    file_path VARCHAR(500),
    file_size BIGINT,
    extracted_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- 创建索引
CREATE INDEX idx_contracts_contract_number ON contracts(contract_number);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_ai_processing_status ON contracts(ai_processing_status);
CREATE INDEX idx_contracts_contract_date ON contracts(contract_date);
CREATE INDEX idx_contracts_created_at ON contracts(created_at);
CREATE INDEX idx_contracts_counterparty ON contracts(counterparty);

-- 添加注释
COMMENT ON TABLE contracts IS '合同表';
COMMENT ON COLUMN contracts.id IS '主键ID';
COMMENT ON COLUMN contracts.contract_number IS '合同编号';
COMMENT ON COLUMN contracts.contract_name IS '合同名称';
COMMENT ON COLUMN contracts.contract_type IS '合同类型';
COMMENT ON COLUMN contracts.counterparty IS '合同对方';
COMMENT ON COLUMN contracts.contract_date IS '合同签订日期';
COMMENT ON COLUMN contracts.start_date IS '合同开始日期';
COMMENT ON COLUMN contracts.end_date IS '合同结束日期';
COMMENT ON COLUMN contracts.status IS '合同状态';
COMMENT ON COLUMN contracts.ai_processing_status IS 'AI处理状态';
COMMENT ON COLUMN contracts.upload_time IS '文件上传时间';
COMMENT ON COLUMN contracts.ai_confidence IS 'AI置信度';
COMMENT ON COLUMN contracts.total_amount IS '合同总金额';
COMMENT ON COLUMN contracts.currency IS '币种';
COMMENT ON COLUMN contracts.payment_frequency IS '支付频率';
COMMENT ON COLUMN contracts.payment_method IS '支付方式';
COMMENT ON COLUMN contracts.tax_rate IS '税率';
COMMENT ON COLUMN contracts.payment_dates IS '付款日期列表(JSON格式)';
COMMENT ON COLUMN contracts.parties IS '合同当事方(JSON格式)';
COMMENT ON COLUMN contracts.amount_elements IS '金额要素(JSON格式)';
COMMENT ON COLUMN contracts.time_elements IS '时间要素(JSON格式)';
COMMENT ON COLUMN contracts.unit_price IS '单价';
COMMENT ON COLUMN contracts.quantity IS '数量';
COMMENT ON COLUMN contracts.service_period_type IS '服务周期类型';
COMMENT ON COLUMN contracts.service_duration IS '服务持续时长';
COMMENT ON COLUMN contracts.service_description IS '服务描述';
COMMENT ON COLUMN contracts.remarks IS '备注';
COMMENT ON COLUMN contracts.original_filename IS '原始文件名';
COMMENT ON COLUMN contracts.file_path IS '文件存储路径';
COMMENT ON COLUMN contracts.file_size IS '文件大小(字节)';
COMMENT ON COLUMN contracts.extracted_text IS '提取的合同文本';
COMMENT ON COLUMN contracts.created_at IS '创建时间';
COMMENT ON COLUMN contracts.updated_at IS '更新时间';
COMMENT ON COLUMN contracts.created_by IS '创建人';

-- 创建更新时间触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_contracts_updated_at 
    BEFORE UPDATE ON contracts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
