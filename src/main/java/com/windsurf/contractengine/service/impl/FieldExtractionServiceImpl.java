package com.windsurf.contractengine.service.impl;

import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.entity.ContractField;
import com.windsurf.contractengine.exception.BusinessException;
import com.windsurf.contractengine.service.FieldExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字段提取服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FieldExtractionServiceImpl implements FieldExtractionService {

    private static final List<String> SUPPORTED_TEMPLATES = Arrays.asList(
            "SERVICE_CONTRACT",      // 服务合同
            "PURCHASE_CONTRACT",     // 采购合同
            "LEASE_CONTRACT",        // 租赁合同
            "CONSULTING_CONTRACT",   // 咨询合同
            "EMPLOYMENT_CONTRACT",   // 雇佣合同
            "SALES_CONTRACT",        // 销售合同
            "PARTNERSHIP_CONTRACT",  // 合作合同
            "MAINTENANCE_CONTRACT"   // 维护合同
    );

    @Override
    public List<ContractField> extractFields(Contract contract) {
        log.info("开始提取合同字段: contractId={}, type={}", contract.getId(), contract.getContractType());
        
        List<ContractField> fields = new ArrayList<>();
        
        try {
            // 1. 提取文本内容（如果还没有提取）
            String text = contract.getExtractedText();
            if (text == null || text.isEmpty()) {
                text = extractTextFromFile(contract.getFilePath());
                contract.setExtractedText(text);
            }
            
            // 2. 根据合同类型获取提取模板
            Map<String, Object> template = getExtractionTemplate(contract.getContractType());
            
            // 3. 使用规则引擎提取字段
            Map<String, String> extractedData = extractFieldsByRules(text, contract.getContractType());
            
            // 4. 将提取结果转换为ContractField实体
            for (Map.Entry<String, String> entry : extractedData.entrySet()) {
                ContractField field = new ContractField();
                field.setContract(contract);
                field.setFieldName(entry.getKey());
                field.setFieldValue(entry.getValue());
                field.setFieldType(determineFieldType(entry.getKey()));
                field.setExtractionMethod("RULE_BASED");
                field.setConfidenceScore(calculateConfidenceScore(entry.getValue(), "RULE_BASED"));
                fields.add(field);
            }
            
            log.info("字段提取完成: contractId={}, 提取字段数={}", contract.getId(), fields.size());
            
        } catch (Exception e) {
            log.error("字段提取失败: contractId={}", contract.getId(), e);
            throw new BusinessException("字段提取失败: " + e.getMessage(), e);
        }
        
        return fields;
    }

    @Override
    public String extractTextFromPdf(String filePath) {
        log.debug("从PDF提取文本: {}", filePath);
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(document);
            
            log.debug("PDF文本提取成功，长度: {}", text.length());
            return text;
            
        } catch (IOException e) {
            log.error("PDF文本提取失败: {}", filePath, e);
            throw new BusinessException("PDF文本提取失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractTextFromWord(String filePath) {
        log.debug("从Word提取文本: {}", filePath);
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            
            log.debug("Word文本提取成功，长度: {}", text.length());
            return text.toString();
            
        } catch (IOException e) {
            log.error("Word文本提取失败: {}", filePath, e);
            throw new BusinessException("Word文本提取失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> extractFieldsByRules(String text, String contractType) {
        log.debug("使用规则引擎提取字段: contractType={}", contractType);
        
        Map<String, String> extractedFields = new HashMap<>();
        Map<String, List<String>> patterns = getFieldPatterns(contractType);
        
        for (Map.Entry<String, List<String>> entry : patterns.entrySet()) {
            String fieldName = entry.getKey();
            List<String> regexPatterns = entry.getValue();
            
            // 尝试每个正则表达式模式
            for (String regex : regexPatterns) {
                Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(text);
                
                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    extractedFields.put(fieldName, value);
                    log.debug("字段提取成功: {}={}", fieldName, value);
                    break; // 找到第一个匹配就停止
                }
            }
        }
        
        return extractedFields;
    }

    @Override
    public Map<String, String> extractFieldsByNLP(String text, String contractType) {
        // TODO: 实现基于NLP的字段提取（可选功能）
        log.warn("NLP字段提取尚未实现");
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getExtractionTemplate(String contractType) {
        Map<String, Object> template = new HashMap<>();
        template.put("contractType", contractType);
        template.put("patterns", getFieldPatterns(contractType));
        template.put("requiredFields", getRequiredFields(contractType));
        return template;
    }

    @Override
    public boolean validateExtractedFields(List<ContractField> fields) {
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        
        // 检查必填字段
        Set<String> extractedFieldNames = new HashSet<>();
        for (ContractField field : fields) {
            extractedFieldNames.add(field.getFieldName());
        }
        
        // 至少应该包含基本字段
        List<String> basicFields = Arrays.asList("total_amount", "start_date", "end_date");
        for (String basicField : basicFields) {
            if (!extractedFieldNames.contains(basicField)) {
                log.warn("缺少必填字段: {}", basicField);
                return false;
            }
        }
        
        return true;
    }

    @Override
    public BigDecimal calculateConfidenceScore(String fieldValue, String extractionMethod) {
        if (fieldValue == null || fieldValue.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double score = 0.5; // 基础分数
        
        // 根据提取方法调整分数
        if ("RULE_BASED".equals(extractionMethod)) {
            score += 0.3;
        } else if ("NLP".equals(extractionMethod)) {
            score += 0.2;
        }
        
        // 根据字段值的完整性调整分数
        if (fieldValue.length() > 5) {
            score += 0.1;
        }
        
        // 检查是否包含数字（对金额、日期等字段）
        if (fieldValue.matches(".*\\d+.*")) {
            score += 0.1;
        }
        
        return BigDecimal.valueOf(Math.min(score, 1.0));
    }

    @Override
    public List<String> getSupportedTemplateTypes() {
        return SUPPORTED_TEMPLATES;
    }

    /**
     * 从文件中提取文本
     */
    private String extractTextFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new BusinessException("文件路径为空");
        }
        
        String extension = getFileExtension(filePath);
        
        switch (extension.toLowerCase()) {
            case "pdf":
                return extractTextFromPdf(filePath);
            case "doc":
            case "docx":
                return extractTextFromWord(filePath);
            case "txt":
                return extractTextFromTxt(filePath);
            default:
                throw new BusinessException("不支持的文件格式: " + extension);
        }
    }

    /**
     * 从TXT文件提取文本
     */
    private String extractTextFromTxt(String filePath) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
        } catch (IOException e) {
            throw new BusinessException("TXT文件读取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filePath.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 确定字段类型
     */
    private String determineFieldType(String fieldName) {
        if (fieldName.contains("amount") || fieldName.contains("price")) {
            return "AMOUNT";
        } else if (fieldName.contains("date")) {
            return "DATE";
        } else if (fieldName.contains("name") || fieldName.contains("party")) {
            return "TEXT";
        } else if (fieldName.contains("number") || fieldName.contains("code")) {
            return "NUMBER";
        }
        return "TEXT";
    }

    /**
     * 获取字段提取的正则表达式模式
     */
    private Map<String, List<String>> getFieldPatterns(String contractType) {
        Map<String, List<String>> patterns = new HashMap<>();
        
        // 通用字段模式
        patterns.put("contract_number", Arrays.asList(
            "合同编号[：:：]?\\s*([A-Z0-9\\-]+)",
            "编号[：:：]?\\s*([A-Z0-9\\-]+)"
        ));
        
        patterns.put("total_amount", Arrays.asList(
            "合同总金额[：:：]?\\s*[¥￥]?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)",
            "总金额[：:：]?\\s*[¥￥]?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)",
            "金额[：:：]?\\s*[¥￥]?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)"
        ));
        
        patterns.put("start_date", Arrays.asList(
            "合同开始日期[：:：]?\\s*(\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2})",
            "起始日期[：:：]?\\s*(\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2})",
            "生效日期[：:：]?\\s*(\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2})"
        ));
        
        patterns.put("end_date", Arrays.asList(
            "合同结束日期[：:：]?\\s*(\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2})",
            "终止日期[：:：]?\\s*(\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2})",
            "到期日期[：:：]?\\s*(\\d{4}[年\\-/]\\d{1,2}[月\\-/]\\d{1,2})"
        ));
        
        patterns.put("party_a", Arrays.asList(
            "甲方[：:：]?\\s*([^\\n（(]+?)(?=[\\n（(]|$)",
            "委托方[：:：]?\\s*([^\\n（(]+?)(?=[\\n（(]|$)"
        ));
        
        patterns.put("party_b", Arrays.asList(
            "乙方[：:：]?\\s*([^\\n（(]+?)(?=[\\n（(]|$)",
            "受托方[：:：]?\\s*([^\\n（(]+?)(?=[\\n（(]|$)"
        ));
        
        patterns.put("payment_method", Arrays.asList(
            "支付方式[：:：]?\\s*([^\\n]+)",
            "付款方式[：:：]?\\s*([^\\n]+)"
        ));
        
        patterns.put("payment_frequency", Arrays.asList(
            "支付周期[：:：]?\\s*([^\\n]+)",
            "付款周期[：:：]?\\s*([^\\n]+)"
        ));
        
        // 根据合同类型添加特定字段模式
        addTypeSpecificPatterns(patterns, contractType);
        
        return patterns;
    }

    /**
     * 添加特定合同类型的字段模式
     */
    private void addTypeSpecificPatterns(Map<String, List<String>> patterns, String contractType) {
        switch (contractType) {
            case "SERVICE_CONTRACT":
                patterns.put("service_scope", Arrays.asList(
                    "服务范围[：:：]?\\s*([^\\n]+)",
                    "服务内容[：:：]?\\s*([^\\n]+)"
                ));
                patterns.put("service_period", Arrays.asList(
                    "服务期限[：:：]?\\s*([^\\n]+)"
                ));
                break;
                
            case "LEASE_CONTRACT":
                patterns.put("lease_object", Arrays.asList(
                    "租赁物[：:：]?\\s*([^\\n]+)",
                    "租赁标的[：:：]?\\s*([^\\n]+)"
                ));
                patterns.put("monthly_rent", Arrays.asList(
                    "月租金[：:：]?\\s*[¥￥]?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{2})?)"
                ));
                break;
                
            case "PURCHASE_CONTRACT":
                patterns.put("product_name", Arrays.asList(
                    "产品名称[：:：]?\\s*([^\\n]+)",
                    "货物名称[：:：]?\\s*([^\\n]+)"
                ));
                patterns.put("quantity", Arrays.asList(
                    "数量[：:：]?\\s*(\\d+)",
                    "采购数量[：:：]?\\s*(\\d+)"
                ));
                break;
        }
    }

    /**
     * 获取必填字段列表
     */
    private List<String> getRequiredFields(String contractType) {
        List<String> required = new ArrayList<>(Arrays.asList(
            "total_amount", "start_date", "end_date", "party_a", "party_b"
        ));
        
        // 根据合同类型添加特定必填字段
        switch (contractType) {
            case "SERVICE_CONTRACT":
                required.add("service_scope");
                break;
            case "LEASE_CONTRACT":
                required.add("lease_object");
                required.add("monthly_rent");
                break;
            case "PURCHASE_CONTRACT":
                required.add("product_name");
                required.add("quantity");
                break;
        }
        
        return required;
    }
}
