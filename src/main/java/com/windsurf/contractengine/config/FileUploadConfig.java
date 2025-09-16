package com.windsurf.contractengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.List;

/**
 * 文件上传配置
 */
@Configuration
@ConfigurationProperties(prefix = "app.file")
@Data
public class FileUploadConfig {

    /**
     * 文件上传目录
     */
    private String uploadDir = "./uploads";

    /**
     * 允许的文件类型
     */
    private List<String> allowedTypes = List.of("pdf", "doc", "docx", "txt");

    /**
     * 最大文件大小（字节）
     */
    private long maxFileSize = 50 * 1024 * 1024; // 50MB

    /**
     * 检查文件类型是否允许
     */
    public boolean isAllowedFileType(String fileExtension) {
        return allowedTypes.contains(fileExtension.toLowerCase());
    }

    /**
     * 获取文件存储路径
     */
    public String getStoragePath(String filename) {
        return uploadDir + "/" + filename;
    }
}
