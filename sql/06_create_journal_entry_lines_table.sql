-- 会计分录明细表 DDL
-- PostgreSQL 数据库

-- 创建会计分录明细表
CREATE TABLE journal_entry_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_entry_id BIGINT NOT NULL,
    line_number INTEGER NOT NULL,
    booking_date DATE,
    gl_account VARCHAR(50),
    gl_account_name VARCHAR(200),
    debit_amount DECIMAL(15,2) DEFAULT 0,
    credit_amount DECIMAL(15,2) DEFAULT 0,
    entered_dr DECIMAL(15,2) DEFAULT 0,
    entered_cr DECIMAL(15,2) DEFAULT 0,
    description VARCHAR(500),
    auxiliary_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 外键约束
    CONSTRAINT fk_journal_entry_lines_journal_entry_id 
        FOREIGN KEY (journal_entry_id) 
        REFERENCES journal_entries(id) 
        ON DELETE CASCADE,
    
    -- 检查约束
    CONSTRAINT chk_journal_entry_lines_amounts 
        CHECK (
            (debit_amount >= 0 AND credit_amount >= 0) AND
            (entered_dr >= 0 AND entered_cr >= 0) AND
            NOT (debit_amount > 0 AND credit_amount > 0) AND
            NOT (entered_dr > 0 AND entered_cr > 0)
        )
);

-- 创建索引
CREATE INDEX idx_journal_entry_lines_journal_entry_id ON journal_entry_lines(journal_entry_id);
CREATE INDEX idx_journal_entry_lines_line_number ON journal_entry_lines(line_number);
CREATE INDEX idx_journal_entry_lines_booking_date ON journal_entry_lines(booking_date);
CREATE INDEX idx_journal_entry_lines_gl_account ON journal_entry_lines(gl_account);
CREATE INDEX idx_journal_entry_lines_created_at ON journal_entry_lines(created_at);

-- 创建复合索引
CREATE INDEX idx_journal_entry_lines_entry_line ON journal_entry_lines(journal_entry_id, line_number);
CREATE INDEX idx_journal_entry_lines_gl_date ON journal_entry_lines(gl_account, booking_date);

-- 创建唯一约束
CREATE UNIQUE INDEX idx_journal_entry_lines_unique_line 
    ON journal_entry_lines(journal_entry_id, line_number);

-- 添加注释
COMMENT ON TABLE journal_entry_lines IS '会计分录明细表';
COMMENT ON COLUMN journal_entry_lines.id IS '主键ID';
COMMENT ON COLUMN journal_entry_lines.journal_entry_id IS '关联的会计分录ID';
COMMENT ON COLUMN journal_entry_lines.line_number IS '行号';
COMMENT ON COLUMN journal_entry_lines.booking_date IS '记账日期';
COMMENT ON COLUMN journal_entry_lines.gl_account IS 'GL科目代码';
COMMENT ON COLUMN journal_entry_lines.gl_account_name IS 'GL科目名称';
COMMENT ON COLUMN journal_entry_lines.debit_amount IS '借方金额';
COMMENT ON COLUMN journal_entry_lines.credit_amount IS '贷方金额';
COMMENT ON COLUMN journal_entry_lines.entered_dr IS '录入借方金额';
COMMENT ON COLUMN journal_entry_lines.entered_cr IS '录入贷方金额';
COMMENT ON COLUMN journal_entry_lines.description IS '摘要';
COMMENT ON COLUMN journal_entry_lines.auxiliary_info IS '辅助核算信息(JSON格式)';
COMMENT ON COLUMN journal_entry_lines.created_at IS '创建时间';
COMMENT ON COLUMN journal_entry_lines.updated_at IS '更新时间';

-- 创建更新时间触发器
CREATE TRIGGER update_journal_entry_lines_updated_at 
    BEFORE UPDATE ON journal_entry_lines 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();
