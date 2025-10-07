package com.windsurf.contractengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 合同分析引擎主应用程序
 * 
 * @author Windsurf Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableAsync
@EnableTransactionManagement
public class ContractEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContractEngineApplication.class, args);
    }
}
