package com.windsurf.contractengine.validation;

import com.windsurf.contractengine.dto.ContractUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 合同更新请求验证测试
 * 验证@Valid和其他验证注解是否正常工作
 */
class ContractUpdateRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest_ShouldPass() {
        // 创建有效的请求对象
        ContractUpdateRequest request = new ContractUpdateRequest();
        request.setContractName("测试合同");
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setTaxRate(new BigDecimal("6.00"));

        // 验证应该通过
        Set<ConstraintViolation<ContractUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "有效请求应该通过验证");
    }

    @Test
    void testInvalidTaxRate_ShouldFail() {
        ContractUpdateRequest request = new ContractUpdateRequest();
        request.setTaxRate(new BigDecimal("150.00")); // 超过100%

        Set<ConstraintViolation<ContractUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效税率应该被拒绝");
        
        boolean foundTaxRateViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("taxRate"));
        assertTrue(foundTaxRateViolation, "应该包含税率验证错误");
    }

    @Test
    void testInvalidAmountElements_ShouldFail() {
        ContractUpdateRequest request = new ContractUpdateRequest();
        
        // 创建无效的金额要素
        ContractUpdateRequest.AmountElements amountElements = new ContractUpdateRequest.AmountElements();
        amountElements.setTotalAmount(new BigDecimal("-100.00")); // 负数金额
        amountElements.setQuantity(-5); // 负数数量
        
        request.setAmountElements(amountElements);

        Set<ConstraintViolation<ContractUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效金额要素应该被拒绝");
        
        // 检查是否有嵌套对象的验证错误
        boolean foundAmountViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("amountElements"));
        assertTrue(foundAmountViolation, "应该包含金额要素验证错误");
    }

    @Test
    void testInvalidServicePeriod_ShouldFail() {
        ContractUpdateRequest request = new ContractUpdateRequest();
        
        // 创建无效的时间要素
        ContractUpdateRequest.TimeElements timeElements = new ContractUpdateRequest.TimeElements();
        ContractUpdateRequest.ServicePeriod servicePeriod = new ContractUpdateRequest.ServicePeriod();
        servicePeriod.setType("INVALID_TYPE"); // 无效的服务周期类型
        servicePeriod.setDuration(-1); // 无效的持续时长
        
        timeElements.setServicePeriod(servicePeriod);
        request.setTimeElements(timeElements);

        Set<ConstraintViolation<ContractUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效服务周期应该被拒绝");
        
        // 检查是否有嵌套对象的验证错误
        boolean foundServicePeriodViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("servicePeriod"));
        assertTrue(foundServicePeriodViolation, "应该包含服务周期验证错误");
    }

    @Test
    void testNullValues_ShouldPass() {
        // 对于更新操作，null值应该被允许（部分更新）
        ContractUpdateRequest request = new ContractUpdateRequest();
        // 所有字段都是null

        Set<ConstraintViolation<ContractUpdateRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "null值应该被允许用于部分更新");
    }

    @Test
    void testStringLengthValidation() {
        ContractUpdateRequest request = new ContractUpdateRequest();
        
        // 测试字符串长度限制
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            longString.append("a");
        }
        request.setContractName(longString.toString()); // 超过200字符限制

        Set<ConstraintViolation<ContractUpdateRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "超长字符串应该被拒绝");
        
        boolean foundLengthViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("contractName"));
        assertTrue(foundLengthViolation, "应该包含字符串长度验证错误");
    }
}
