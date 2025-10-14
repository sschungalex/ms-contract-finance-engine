-- 合同字段提取结果表 DDL
-- PostgreSQL 数据库

-- 创建合同字段表
CREATE TABLE contract_fields (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    field_value TEXT,
    field_type VARCHAR(50),
    confidence_score DECIMAL(5,4),
    extraction_method VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_contract_fields_contract_id 
        FOREIGN KEY (contract_id) 
        REFERENCES contracts(id) 
        ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_contract_fields_contract_id ON contract_fields(contract_id);
CREATE INDEX idx_contract_fields_field_name ON contract_fields(field_name);
CREATE INDEX idx_contract_fields_field_type ON contract_fields(field_type);
CREATE INDEX idx_contract_fields_confidence_score ON contract_fields(confidence_score);
CREATE INDEX idx_contract_fields_created_at ON contract_fields(created_at);

-- 添加注释
COMMENT ON TABLE contract_fields IS '合同字段提取结果表';
COMMENT ON COLUMN contract_fields.id IS '主键ID';
COMMENT ON COLUMN contract_fields.contract_id IS '关联的合同ID';
COMMENT ON COLUMN contract_fields.field_name IS '字段名称';
COMMENT ON COLUMN contract_fields.field_value IS '字段值';
COMMENT ON COLUMN contract_fields.field_type IS '字段类型';
COMMENT ON COLUMN contract_fields.confidence_score IS '置信度分数';
COMMENT ON COLUMN contract_fields.extraction_method IS '提取方法';
COMMENT ON COLUMN contract_fields.created_at IS '创建时间';
COMMENT ON COLUMN contract_fields.updated_at IS '更新时间';

-- 创建更新时间触发器
CREATE TRIGGER update_contract_fields_updated_at 
    BEFORE UPDATE ON contract_fields 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
