package com.windsurf.contractengine.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.windsurf.contractengine.dto.ContractUpdateRequest;
import com.windsurf.contractengine.entity.Contract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 合同更新工具类
 * 提供清洁的字段更新逻辑，减少手动验空代码
 */
@Component
@Slf4j
public class ContractUpdateUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 更新合同基本字段
     * 使用函数式接口简化更新逻辑
     */
    public List<String> updateContractFields(Contract contract, ContractUpdateRequest request) {
        List<String> updatedFields = new ArrayList<>();

        // 基本字段更新
        updateIfPresent(request.getContractName(), contract::setContractName, "contractName", updatedFields);
        updateIfPresent(request.getContractType(), contract::setContractType, "contractType", updatedFields);
        updateIfPresent(request.getCounterparty(), contract::setCounterparty, "counterparty", updatedFields);
        updateIfPresent(request.getStartDate(), contract::setStartDate, "startDate", updatedFields);
        updateIfPresent(request.getEndDate(), contract::setEndDate, "endDate", updatedFields);
        updateIfPresent(request.getStatus(), contract::setStatus, "status", updatedFields);
        updateIfPresent(request.getCurrency(), contract::setCurrency, "currency", updatedFields);
        updateIfPresent(request.getPaymentFrequency(), contract::setPaymentFrequency, "paymentFrequency", updatedFields);
        updateIfPresent(request.getPaymentMethod(), contract::setPaymentMethod, "paymentMethod", updatedFields);
        updateIfPresent(request.getTotalAmount(), contract::setTotalAmount, "totalAmount", updatedFields);
        updateIfPresent(request.getTaxRate(), contract::setTaxRate, "taxRate", updatedFields);
        updateIfPresent(request.getRemarks(), contract::setRemarks, "remarks", updatedFields);
        updateIfPresent(request.getContractDate(), contract::setContractDate, "contractDate", updatedFields);

        // JSON字段更新
        updateJsonFieldIfPresent(request.getPaymentDates(), contract::setPaymentDates, "paymentDates", updatedFields);
        updateJsonFieldIfPresent(request.getParties(), contract::setParties, "parties", updatedFields);

        return updatedFields;
    }

    /**
     * 更新复杂对象字段（金额要素和时间要素）
     */
    public void updateComplexFields(Contract contract, ContractUpdateRequest request, List<String> updatedFields) {
        // 更新金额要素
        if (request.getAmountElements() != null) {
            updateJsonFieldIfPresent(request.getAmountElements(), contract::setAmountElements, "amountElements", updatedFields);
            
            // 同步更新顶层字段（如果金额要素中有值）
            ContractUpdateRequest.AmountElements amountElements = request.getAmountElements();
            updateIfPresent(amountElements.getTotalAmount(), contract::setTotalAmount, null, null);
            updateIfPresent(amountElements.getUnitPrice(), contract::setUnitPrice, null, null);
            updateIfPresent(amountElements.getQuantity(), contract::setQuantity, null, null);
        }

        // 更新时间要素
        if (request.getTimeElements() != null) {
            updateJsonFieldIfPresent(request.getTimeElements(), contract::setTimeElements, "timeElements", updatedFields);
            
            // 同步更新服务周期字段
            if (request.getTimeElements().getServicePeriod() != null) {
                ContractUpdateRequest.ServicePeriod servicePeriod = request.getTimeElements().getServicePeriod();
                if (servicePeriod.getType() != null) {
                    contract.setServicePeriodType(com.windsurf.contractengine.enums.ServicePeriodType.valueOf(servicePeriod.getType()));
                }
                updateIfPresent(servicePeriod.getDuration(), contract::setServiceDuration, null, null);
                updateIfPresent(servicePeriod.getDescription(), contract::setServiceDescription, null, null);
            }
        }
    }

    /**
     * 通用字段更新方法
     * 只有当值不为null时才执行更新
     */
    private <T> void updateIfPresent(T value, Consumer<T> setter, String fieldName, List<String> updatedFields) {
        if (value != null) {
            setter.accept(value);
            if (fieldName != null && updatedFields != null) {
                updatedFields.add(fieldName);
            }
        }
    }

    /**
     * JSON字段更新方法
     * 处理对象到JSON字符串的转换
     */
    private <T> void updateJsonFieldIfPresent(T value, Consumer<String> setter, String fieldName, List<String> updatedFields) {
        if (value != null) {
            try {
                String json = objectMapper.writeValueAsString(value);
                setter.accept(json);
                if (fieldName != null && updatedFields != null) {
                    updatedFields.add(fieldName);
                }
            } catch (Exception e) {
                log.error("序列化{}失败: {}", fieldName, e.getMessage());
                throw new RuntimeException("序列化" + fieldName + "失败", e);
            }
        }
    }
}
