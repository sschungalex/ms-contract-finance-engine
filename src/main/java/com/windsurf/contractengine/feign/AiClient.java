package com.windsurf.contractengine.feign;

import com.windsurf.contractengine.config.AiClientConfig;
import com.windsurf.contractengine.dto.AiRequest;
import com.windsurf.contractengine.dto.AiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", url = "${ai.api.url}", configuration = AiClientConfig.class)
public interface AiClient {

    @PostMapping("/chat")
    AiResponse chat(@RequestBody AiRequest request);

}
