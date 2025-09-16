package com.windsurf.contractengine.service;

import com.windsurf.contractengine.entity.Contract;
import com.windsurf.contractengine.entity.ContractField;

import java.util.List;
import java.util.Map;

/**
 * 字段提取服务接口
 */
public interface FieldExtractionService {

    /**
     * 从合同文本中提取字段
     * 
     * @param contract 合同实体
     * @return 提取的字段列表
     */
    List<ContractField> extractFields(Contract contract);

    /**
     * 根据合同类型获取字段提取模板
     * 
     * @param contractType 合同类型
     * @return 字段提取模板
     */
    Map<String, Object> getExtractionTemplate(String contractType);

    /**
     * 验证提取字段的完整性
     * 
     * @param fields 提取的字段列表
     * @return 验证结果
     */
    boolean validateExtractedFields(List<ContractField> fields);

    /**
     * 计算字段提取的置信度
     * 
     * @param fieldValue 字段值
     * @param extractionMethod 提取方法
     * @return 置信度分数
     */
    Double calculateConfidenceScore(String fieldValue, String extractionMethod);

    /**
     * 支持的合同模板类型
     * 
     * @return 支持的模板类型列表
     */
    List<String> getSupportedTemplateTypes();

    /**
     * 从PDF文档中提取文本
     * 
     * @param filePath 文件路径
     * @return 提取的文本内容
     */
    String extractTextFromPdf(String filePath);

    /**
     * 从Word文档中提取文本
     * 
     * @param filePath 文件路径
     * @return 提取的文本内容
     */
    String extractTextFromWord(String filePath);

    /**
     * 使用规则引擎提取字段
     * 
     * @param text 合同文本
     * @param contractType 合同类型
     * @return 提取的字段映射
     */
    Map<String, String> extractFieldsByRules(String text, String contractType);

    /**
     * 使用NLP模型提取字段
     * 
     * @param text 合同文本
     * @param contractType 合同类型
     * @return 提取的字段映射
     */
    Map<String, String> extractFieldsByNLP(String text, String contractType);
}
