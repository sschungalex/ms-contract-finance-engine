# 合同财务引擎 RESTful API 规范

## 1. 合同管理 API

### 1.1 文件上传 + AI 模型交互
```
POST /api/contracts/upload
Content-Type: multipart/form-data
```

#### 请求参数
| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| contract_file | File | 是 | 合同文件 | 支持PDF、DOC、DOCX格式，最大10MB |
| model_id | Integer | 否 | AI模型ID | 大于0的整数，默认使用系统默认模型 |

#### 请求示例 (Form Data)
```
POST /api/contracts/upload
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="contract_file"; filename="contract.pdf"
Content-Type: application/pdf

[二进制文件内容]
--boundary
Content-Disposition: form-data; name="model_id"

1
--boundary--
```

#### 成功响应 (HTTP 201)
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 123,
    "status": "PROCESSING",
    "originalFileName": "contract_abc.pdf",
    "uploadTime": "2025-01-01T10:00:00Z",
    "aiResult": {
      "extractedFields": {
        "amountElements": {
          "totalAmount": 100000.00,
          "unitPrice": 5000.00,
          "quantity": 20
        },
        "timeElements": {
          "servicePeriod": {
            "type": "MONTHLY",
            "duration": 12,
            "description": "按月服务，共12个月"
          },
          "deliveryNodes": [
            {
              "milestone": "项目启动",
              "percentage": 30,
              "dueDate": "2025-01-15"
            },
            {
              "milestone": "中期交付",
              "percentage": 40,
              "dueDate": "2025-06-15"
            },
            {
              "milestone": "项目完成",
              "percentage": 30,
              "dueDate": "2025-12-15"
            }
          ]
        },
        "paymentMethod": "银行转账",
        "contractDate": "2025-01-01",
        "parties": ["甲方公司", "乙方公司"]
      },
      "confidence": 0.95
    }
  },
  "traceId": "trace-123456"
}
```

#### 错误响应
| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 400 | 40001 | 参数校验失败 |
| 413 | 41301 | 文件大小超限 |
| 415 | 41501 | 不支持的文件格式 |
| 500 | 50001 | AI模型处理异常 |

---

### 1.2 查询已上传合同列表
```
GET /api/contracts
```

#### 请求参数 (Query Parameters)
| 参数名 | 类型 | 必填 | 默认值 | 说明 | 约束 |
|--------|------|------|--------|------|------|
| page | Integer | 否 | 1 | 页码 | 大于0 |
| size | Integer | 否 | 10 | 每页大小 | 1-100 |
| status | String | 否 | - | 合同状态 | PROCESSING/COMPLETED/FAILED |
| start_date | String | 否 | - | 开始日期 | yyyy-MM-dd格式 |
| end_date | String | 否 | - | 结束日期 | yyyy-MM-dd格式 |

#### 请求示例
```
GET /api/contracts?page=1&size=10&status=COMPLETED&start_date=2025-01-01&end_date=2025-12-31
```

#### 成功响应 (HTTP 200)
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 50,
    "page": 1,
    "size": 10,
    "items": [
      {
        "contractId": 123,
        "originalFileName": "contract_abc.pdf",
        "status": "COMPLETED",
        "uploadTime": "2025-01-01T10:00:00Z",
        "totalAmount": 100000.00,
        "paymentMethod": "银行转账"
      }
    ]
  },
  "traceId": "trace-123456"
}
```

#### 错误响应
| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 400 | 40002 | 参数格式错误 |

---

### 1.3 查询合同详情
```
GET /api/contracts/{id}
```

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| id | Long | 是 | 合同ID | 大于0的整数 |

#### 请求示例
```
GET /api/contracts/123
```

#### 成功响应 (HTTP 200)
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 123,
    "contractNumber": "CT202401001",
    "contractName": "软件开发服务合同",
    "contractType": "服务合同",
    "counterparty": "ABC科技有限公司",
    "status": "ACTIVE",
    "aiProcessingStatus": "COMPLETED",
    "aiConfidence": 0.95,
    "originalFileName": "contract_abc.pdf",
    "uploadTime": "2025-01-01T10:00:00Z",
    "paymentMethod": "银行转账",
    "totalAmount": 100000.00,
    "currency": "CNY",
    "paymentFrequency": "MONTHLY",
    "paymentDates": ["2025-01-01", "2025-03-01", "2025-06-01"],
    "taxRate": 6.00,
    "remarks": "备注信息",
    "contractDate": "2025-01-01",
    "parties": ["甲方公司", "乙方公司"],
    "amountElements": {
      "totalAmount": 100000.00,
      "unitPrice": 5000.00,
      "quantity": 20
    },
    "timeElements": {
      "servicePeriod": {
        "type": "MONTHLY",
        "duration": 12,
        "description": "按月服务，共12个月"
      },
      "deliveryNodes": [
        {
          "milestone": "项目启动",
          "percentage": 30,
          "dueDate": "2025-01-15"
        },
        {
          "milestone": "中期交付",
          "percentage": 40,
          "dueDate": "2025-06-15"
        },
        {
          "milestone": "项目完成",
          "percentage": 30,
          "dueDate": "2025-12-15"
        }
      ]
    },
    "unitPrice": 5000.00,
    "quantity": 20,
    "servicePeriodType": "MONTHLY",
    "serviceDuration": 12,
    "serviceDescription": "按月服务，共12个月",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "createdAt": "2025-01-01T10:00:00Z",
    "updatedAt": "2025-01-01T10:30:00Z",
    "createdBy": "张三"
  },
  "traceId": "trace-123456"
}
```

#### 响应字段说明
响应数据使用统一的ContractResponse对象，包含合同的所有详细信息（包括AI提取的结构化数据），与1.4数据修正编辑的请求字段基本一致，便于前端直接用于编辑表单的数据回显。

#### 错误响应
| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 404 | 40401 | 合同不存在 |
| 500 | 50003 | 查询异常 |

---

### 1.4 数据修正编辑
```
PUT /api/contracts/{id}
Content-Type: application/json
```

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| id | Long | 是 | 合同ID | 大于0的整数 |

#### 请求体 (JSON)
```json
{
  "paymentMethod": "银行转账",
  "totalAmount": 100000.00,
  "paymentDates": ["2025-01-01", "2025-03-01"],
  "taxRate": 6.00,
  "remarks": "备注信息",
  "amountElements": {
    "totalAmount": 100000.00,
    "unitPrice": 5000.00,
    "quantity": 20
  },
  "timeElements": {
    "servicePeriod": {
      "type": "MONTHLY",
      "duration": 12,
      "description": "按月服务，共12个月"
    },
    "deliveryNodes": [
      {
        "milestone": "项目启动",
        "percentage": 30,
        "dueDate": "2025-01-15"
      },
      {
        "milestone": "中期交付",
        "percentage": 40,
        "dueDate": "2025-06-15"
      },
      {
        "milestone": "项目完成",
        "percentage": 30,
        "dueDate": "2025-12-15"
      }
    ]
  },
  "contractDate": "2025-01-01",
  "parties": ["甲方公司", "乙方公司"]
}
```

#### 请求示例
```
PUT /api/contracts/123
Content-Type: application/json

{
  "paymentMethod": "银行转账",
  "totalAmount": 100000.00,
  "paymentDates": ["2025-01-01", "2025-03-01", "2025-06-01"],
  "taxRate": 6.00,
  "remarks": "合同金额已确认，按季度付款",
  "amountElements": {
    "totalAmount": 100000.00,
    "unitPrice": 5000.00,
    "quantity": 20
  },
  "timeElements": {
    "servicePeriod": {
      "type": "QUARTERLY",
      "duration": 4,
      "description": "按季度服务，共4个季度"
    },
    "deliveryNodes": [
      {
        "milestone": "项目启动",
        "percentage": 30,
        "dueDate": "2025-01-15"
      },
      {
        "milestone": "中期交付",
        "percentage": 40,
        "dueDate": "2025-06-15"
      },
      {
        "milestone": "项目完成",
        "percentage": 30,
        "dueDate": "2025-12-15"
      }
    ]
  },
  "contractDate": "2025-01-01",
  "parties": ["甲方公司修正", "乙方公司修正"]
}
```

#### 请求字段说明
| 字段名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| paymentMethod | String | 是 | 支付方式 | 最大50字符 |
| totalAmount | Number | 是 | 合同总金额 | 大于0，最多12位整数2位小数 |
| paymentDates | Array | 是 | 付款日期列表 | 日期格式yyyy-MM-dd，最多10个 |
| taxRate | Number | 是 | 税率 | 0-100之间的数值 |
| remarks | String | 否 | 备注 | 最大500字符 |
| amountElements | Object | 否 | 金额要素 | 包含总金额、单价、数量 |
| amountElements.totalAmount | Number | 否 | 合同总金额 | 大于0 |
| amountElements.unitPrice | Number | 否 | 单价 | 大于0 |
| amountElements.quantity | Number | 否 | 数量 | 大于0的整数 |
| timeElements | Object | 否 | 时间要素 | 包含服务周期和交付节点 |
| timeElements.servicePeriod | Object | 否 | 服务周期 | 服务周期信息 |
| timeElements.servicePeriod.type | String | 否 | 服务周期类型 | ONCE/WEEKLY/MONTHLY/QUARTERLY/YEARLY |
| timeElements.servicePeriod.duration | Number | 否 | 持续时长 | 大于0的整数 |
| timeElements.servicePeriod.description | String | 否 | 描述信息 | 最大200字符 |
| timeElements.deliveryNodes | Array | 否 | 交付节点列表 | 最多20个节点 |
| timeElements.deliveryNodes[].milestone | String | 否 | 里程碑名称 | 最大100字符 |
| timeElements.deliveryNodes[].percentage | Number | 否 | 交付百分比 | 0-100之间的数值 |
| timeElements.deliveryNodes[].dueDate | String | 否 | 预期交付日期 | yyyy-MM-dd格式 |
| contractDate | String | 否 | 合同签订日期 | yyyy-MM-dd格式 |
| parties | Array | 否 | 合同当事方 | 字符串数组，最多10个 |

#### 成功响应 (HTTP 200)
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 123,
    "message": "合同数据更新成功",
    "updatedTime": "2025-01-01T10:30:00Z",
    "updatedFields": ["paymentMethod", "totalAmount", "paymentDates", "taxRate"]
  },
  "traceId": "trace-123456"
}
```

#### 错误响应
| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 400 | 40003 | 参数校验失败 |
| 404 | 40401 | 合同不存在 |
| 409 | 40901 | 合同状态不允许修改 |
| 500 | 50002 | 数据更新异常 |

---

### 1.5 预付摊销表生成 API
```
GET /api/contracts/{id}/amortization-schedule
```

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| id | Long | 是 | 合同ID | 大于0的整数 |

#### 请求参数 (Query Parameters)
| 参数名 | 类型 | 必填 | 默认值 | 说明 | 约束 |
|--------|------|------|--------|------|------|
| generate_mode | String | 否 | AUTO | 生成模式 | AUTO(根据AI结果自动生成)/MANUAL(手动指定参数) |

#### 请求示例
```
GET /api/contracts/123/amortization-schedule?generate_mode=AUTO
```

#### 成功响应 (HTTP 200)
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 123,
    "contractInfo": {
      "fileName": "contract_abc.pdf",
      "totalAmount": 120000.00,
      "contractStartDate": "2025-01-01",
      "servicePeriod": {
        "type": "MONTHLY",
        "duration": 12,
        "description": "按月服务，共12个月"
      }
    },
    "amortizationSchedule": [
      {
        "period": "2025-01",
        "accrualPeriod": "2025-01-01 至 2025-01-31",
        "accountingPeriod": "2025-01",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-02",
        "accrualPeriod": "2025-02-01 至 2025-02-28",
        "accountingPeriod": "2025-02",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-03",
        "accrualPeriod": "2025-03-01 至 2025-03-31",
        "accountingPeriod": "2025-03",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-04",
        "accrualPeriod": "2025-04-01 至 2025-04-30",
        "accountingPeriod": "2025-04",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-05",
        "accrualPeriod": "2025-05-01 至 2025-05-31",
        "accountingPeriod": "2025-05",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-06",
        "accrualPeriod": "2025-06-01 至 2025-06-30",
        "accountingPeriod": "2025-06",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-07",
        "accrualPeriod": "2025-07-01 至 2025-07-31",
        "accountingPeriod": "2025-07",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-08",
        "accrualPeriod": "2025-08-01 至 2025-08-31",
        "accountingPeriod": "2025-08",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-09",
        "accrualPeriod": "2025-09-01 至 2025-09-30",
        "accountingPeriod": "2025-09",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-10",
        "accrualPeriod": "2025-10-01 至 2025-10-31",
        "accountingPeriod": "2025-10",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-11",
        "accrualPeriod": "2025-11-01 至 2025-11-30",
        "accountingPeriod": "2025-11",
        "amortizationAmount": 10000.00
      },
      {
        "period": "2025-12",
        "accrualPeriod": "2025-12-01 至 2025-12-31",
        "accountingPeriod": "2025-12",
        "amortizationAmount": 10000.00
      }
    ],
    "summary": {
      "totalRecords": 12,
      "totalAmortizationAmount": 120000.00,
      "averageMonthlyAmount": 10000.00,
      "startPeriod": "2025-01",
      "endPeriod": "2025-12"
    },
    "generatedTime": "2025-01-01T10:00:00Z",
    "calculationBasis": {
      "totalAmount": 120000.00,
      "servicePeriodType": "MONTHLY",
      "serviceDuration": 12,
      "amortizationFormula": "合同总金额 ÷ 服务期间月数",
      "monthlyAmount": "120000.00 ÷ 12 = 10000.00"
    }
  },
  "traceId": "trace-123456"
}
```

#### 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| contractInfo | Object | 合同基础信息 |
| amortizationSchedule | Array | 预付摊销表记录列表 |
| amortizationSchedule[].period | String | 预提/摊销期间 |
| amortizationSchedule[].accrualPeriod | String | 预提期间详细描述 |
| amortizationSchedule[].accountingPeriod | String | 入账期间 |
| amortizationSchedule[].amortizationAmount | Number | 预提/摊销金额 |
| summary | Object | 摊销表汇总信息 |
| calculationBasis | Object | 计算依据和公式 |

#### 摊销金额计算规则
1. **按月付费**: 预提金额 = 合同总金额 ÷ 服务期间月数
2. **按季度付费**: 预提金额 = 合同总金额 ÷ 服务期间季度数 ÷ 3
3. **按年付费**: 预提金额 = 合同总金额 ÷ 服务期间年数 ÷ 12
4. **一次性付费**: 预提金额 = 合同总金额 ÷ 服务期间总月数

#### 期间计算规则
- 起始时间：根据AI扫描结果中的 `contractDate` 或 `timeElements.servicePeriod` 确定
- 结束时间：起始时间 + 服务期间长度
- 期间划分：根据 `timeElements.servicePeriod.type` 确定划分粒度

#### 错误响应
| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 400 | 40004 | 参数校验失败 |
| 404 | 40401 | 合同不存在 |
| 422 | 42202 | 合同缺少必要的AI扫描结果数据 |
| 500 | 50004 | 摊销表生成异常 |

---

### 1.6 会计分录生成 API
```
POST /api/contracts/{id}/journal-entries
Content-Type: application/json
```

#### 路径参数
| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| id | Long | 是 | 合同ID | 大于0的整数 |

#### 请求体 (JSON)
```json
{
  "contractInfo": {
    "contractId": 123,
    "totalAmount": 120000.00,
    "paymentPeriod": {
      "type": "MONTHLY",
      "duration": 12,
      "description": "按月服务，共12个月"
    }
  },
  "actualPayments": [
    {
      "paymentDate": "2025-01-15",
      "amount": 30000.00,
      "paymentMethod": "银行转账",
      "description": "首期付款"
    },
    {
      "paymentDate": "2025-04-15", 
      "amount": 30000.00,
      "paymentMethod": "银行转账",
      "description": "第二期付款"
    },
    {
      "paymentDate": "2025-07-15",
      "amount": 30000.00,
      "paymentMethod": "银行转账", 
      "description": "第三期付款"
    },
    {
      "paymentDate": "2025-10-15",
      "amount": 30000.00,
      "paymentMethod": "银行转账",
      "description": "第四期付款"
    }
  ],
  "generateOptions": {
    "includeAccruals": true,
    "autoBalanceWithExpense": true,
    "baseCurrency": "CNY"
  }
}
```

#### 请求字段说明
| 字段名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| contractInfo | Object | 是 | 合同基础信息 | 来自AI解析结果 |
| contractInfo.contractId | Long | 是 | 合同ID | 大于0 |
| contractInfo.totalAmount | Number | 是 | 合同总金额 | 大于0 |
| contractInfo.paymentPeriod | Object | 是 | 付款周期信息 | 来自AI解析的时间要素 |
| actualPayments | Array | 是 | 实际付款信息列表 | 最多50笔付款记录 |
| actualPayments[].paymentDate | String | 是 | 实际付款日期 | yyyy-MM-dd格式 |
| actualPayments[].amount | Number | 是 | 实际付款金额 | 大于0 |
| actualPayments[].paymentMethod | String | 是 | 付款方式 | 最大50字符 |
| actualPayments[].description | String | 否 | 付款描述 | 最大200字符 |
| generateOptions | Object | 否 | 生成选项 | 控制分录生成行为 |
| generateOptions.includeAccruals | Boolean | 否 | 是否包含预提分录 | 默认true |
| generateOptions.autoBalanceWithExpense | Boolean | 否 | 是否用费用科目平衡差额 | 默认true |
| generateOptions.baseCurrency | String | 否 | 基础货币 | 默认CNY |

#### 请求示例
```
POST /api/contracts/123/journal-entries
Content-Type: application/json

{
  "contractInfo": {
    "contractId": 123,
    "totalAmount": 120000.00,
    "paymentPeriod": {
      "type": "MONTHLY",
      "duration": 12,
      "description": "按月服务，共12个月"
    }
  },
  "actualPayments": [
    {
      "paymentDate": "2025-01-15",
      "amount": 30000.00,
      "paymentMethod": "银行转账",
      "description": "首期付款"
    }
  ],
  "generateOptions": {
    "includeAccruals": true,
    "autoBalanceWithExpense": true,
    "baseCurrency": "CNY"
  }
}
```

#### 成功响应 (HTTP 201)
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 123,
    "journalEntries": [
      {
        "entryId": "JE-2025-001",
        "bookingDate": "2025-01-15",
        "description": "合同预付款及费用确认",
        "reference": "Contract-123-Payment-1",
        "lines": [
          {
            "lineNumber": 1,
            "bookingDate": "2025-01-15",
            "glAccount": "1221",
            "glAccountName": "预付账款",
            "enteredDr": 30000.00,
            "enteredCr": 0.00,
            "description": "确认预付服务费"
          },
          {
            "lineNumber": 2,
            "bookingDate": "2025-01-15",
            "glAccount": "1001",
            "glAccountName": "活期存款",
            "enteredDr": 0.00,
            "enteredCr": 30000.00,
            "description": "银行转账付款"
          }
        ],
        "totalDr": 30000.00,
        "totalCr": 30000.00,
        "balanced": true
      },
      {
        "entryId": "JE-2025-002",
        "bookingDate": "2025-01-31",
        "description": "1月份服务费用摊销",
        "reference": "Contract-123-Amortization-2025-01",
        "lines": [
          {
            "lineNumber": 1,
            "bookingDate": "2025-01-31",
            "glAccount": "6001",
            "glAccountName": "服务费用",
            "enteredDr": 10000.00,
            "enteredCr": 0.00,
            "description": "1月份服务费摊销"
          },
          {
            "lineNumber": 2,
            "bookingDate": "2025-01-31",
            "glAccount": "1221",
            "glAccountName": "预付账款",
            "enteredDr": 0.00,
            "enteredCr": 10000.00,
            "description": "预付账款摊销"
          }
        ],
        "totalDr": 10000.00,
        "totalCr": 10000.00,
        "balanced": true
      },
      {
        "entryId": "JE-2025-003",
        "bookingDate": "2025-02-28",
        "description": "2月份服务费用摊销",
        "reference": "Contract-123-Amortization-2025-02",
        "lines": [
          {
            "lineNumber": 1,
            "bookingDate": "2025-02-28",
            "glAccount": "6001",
            "glAccountName": "服务费用",
            "enteredDr": 10000.00,
            "enteredCr": 0.00,
            "description": "2月份服务费摊销"
          },
          {
            "lineNumber": 2,
            "bookingDate": "2025-02-28",
            "glAccount": "1221",
            "glAccountName": "预付账款",
            "enteredDr": 0.00,
            "enteredCr": 10000.00,
            "description": "预付账款摊销"
          }
        ],
        "totalDr": 10000.00,
        "totalCr": 10000.00,
        "balanced": true
      }
    ],
    "summary": {
      "totalEntries": 3,
      "totalPaymentEntries": 1,
      "totalAmortizationEntries": 2,
      "totalDrAmount": 50000.00,
      "totalCrAmount": 50000.00,
      "contractTotalAmount": 120000.00,
      "paidAmount": 30000.00,
      "remainingAmount": 90000.00,
      "amortizedAmount": 20000.00,
      "prepaidBalance": 10000.00
    },
    "accountingPrinciples": {
      "paymentRecognition": "实际付款日期确认预付账款",
      "expenseRecognition": "按服务期间摊销确认费用",
      "balancingRule": "借贷必须平衡",
      "accrualBasis": "权责发生制"
    },
    "glAccountMapping": {
      "1001": "活期存款 (银行账户)",
      "1221": "预付账款 (资产科目)",
      "2201": "应付账款 (负债科目)",
      "6001": "服务费用 (费用科目)",
      "6999": "其他费用 (差额调整)"
    },
    "generatedTime": "2025-01-01T10:00:00Z"
  },
  "traceId": "trace-123456"
}
```

#### 会计分录生成规则

##### 1. 付款确认分录
- **借**: 预付账款 (1221) - 实际付款金额
- **贷**: 活期存款 (1001) - 实际付款金额
- **日期**: 实际付款日期

##### 2. 费用摊销分录 (按月生成)
- **借**: 服务费用 (6001) - 月度摊销金额
- **贷**: 预付账款 (1221) - 月度摊销金额  
- **日期**: 每月最后一天

##### 3. 差额调整分录 (如需要)
- 当实际付款与合同金额不匹配时:
- **借**: 其他费用 (6999) - 差额
- **贷**: 预付账款 (1221) - 差额

##### 4. 科目编码规范
| 科目代码 | 科目名称 | 科目类型 | 使用场景 |
|---------|---------|---------|---------|
| 1001 | 活期存款 | 资产 | 银行付款 |
| 1221 | 预付账款 | 资产 | 预付服务费 |
| 2201 | 应付账款 | 负债 | 应付未付款项 |
| 6001 | 服务费用 | 费用 | 服务费摊销 |
| 6999 | 其他费用 | 费用 | 差额调整 |

#### 错误响应
| HTTP状态码 | 错误码 | 说明 |
|-----------|--------|------|
| 400 | 40005 | 请求参数校验失败 |
| 404 | 40401 | 合同不存在 |
| 422 | 42203 | 付款信息与合同不匹配 |
| 422 | 42204 | 会计分录无法平衡 |
| 500 | 50005 | 会计分录生成异常 |

---

## 2. AI扫描提取字段详细说明

### 2.1 金额要素 (amountElements)
| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| totalAmount | Number | 合同总金额 | 100000.00 |
| unitPrice | Number | 单价 | 5000.00 |
| quantity | Number | 数量 | 20 |

### 2.2 时间要素 (timeElements)

#### 2.2.1 服务周期 (servicePeriod)
| 字段名 | 类型 | 说明 | 可选值 |
|--------|------|------|-------|
| type | String | 服务周期类型 | ONCE(一次性)/WEEKLY(周)/MONTHLY(月)/QUARTERLY(季度)/YEARLY(年) |
| duration | Number | 持续时长 | 根据type类型的数值 |
| description | String | 描述信息 | 人工可读的描述 |

#### 2.2.2 交付节点 (deliveryNodes)
| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| milestone | String | 里程碑名称 | "项目启动"、"中期交付"、"项目完成" |
| percentage | Number | 交付百分比 | 30、40、30 (总和应为100) |
| dueDate | String | 预期交付日期 | "2025-01-15" (yyyy-MM-dd格式) |

### 2.3 AI扫描结果完整示例
```json
{
  "aiResult": {
    "extractedFields": {
      "amountElements": {
        "totalAmount": 100000.00,
        "unitPrice": 5000.00,
        "quantity": 20
      },
      "timeElements": {
        "servicePeriod": {
          "type": "MONTHLY",
          "duration": 12,
          "description": "按月服务，共12个月"
        },
        "deliveryNodes": [
          {
            "milestone": "项目启动",
            "percentage": 30,
            "dueDate": "2025-01-15"
          },
          {
            "milestone": "中期交付", 
            "percentage": 40,
            "dueDate": "2025-06-15"
          },
          {
            "milestone": "项目完成",
            "percentage": 30,
            "dueDate": "2025-12-15"
          }
        ]
      },
      "paymentMethod": "银行转账",
      "contractDate": "2025-01-01",
      "parties": ["甲方公司", "乙方公司"]
    },
    "confidence": 0.95
  }
}
```

---

## 3. 通用响应格式

### 3.1 统一响应结构
所有API响应都遵循以下统一格式：
```json
{
  "code": 0,
  "message": "success",
  "data": { ... },
  "traceId": "trace-123456"
}
```

### 3.2 响应字段说明
| 字段名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码，0表示成功，非0表示失败 |
| message | String | 响应消息 |
| data | Object | 响应数据，成功时包含具体数据，失败时可能为空 |
| traceId | String | 链路追踪ID |

### 3.3 分页响应格式
查询列表类接口的响应格式：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "items": [ ... ]
  },
  "traceId": "trace-123456"
}
```

## 4. 错误码规范

### 4.1 HTTP状态码使用
| HTTP状态码 | 使用场景 |
|-----------|----------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 413 | 请求实体过大 |
| 415 | 不支持的媒体类型 |
| 422 | 请求格式正确但语义错误 |
| 500 | 服务器内部错误 |

### 4.2 业务错误码
| 错误码 | 说明 |
|--------|------|
| 40001 | 参数校验失败 |
| 40002 | 参数格式错误 |
| 40003 | 参数校验失败 |
| 40004 | 参数校验失败 |
| 40005 | 请求参数校验失败 |
| 40401 | 合同不存在 |
| 40901 | 合同状态不允许修改 |
| 41301 | 文件大小超限 |
| 41501 | 不支持的文件格式 |
| 42201 | 日期范围超过限制 |
| 42202 | 合同缺少必要的AI扫描结果数据 |
| 42203 | 付款信息与合同不匹配 |
| 42204 | 会计分录无法平衡 |
| 50001 | AI模型处理异常 |
| 50002 | 数据更新异常 |
| 50003 | 报表生成异常 |
| 50004 | 摊销表生成异常 |
| 50005 | 会计分录生成异常 |
