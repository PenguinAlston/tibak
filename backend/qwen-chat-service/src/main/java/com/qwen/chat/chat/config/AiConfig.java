package com.qwen.chat.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.dashscope")
public class AiConfig {

    private boolean enabled = true;
    private String apiKey;
    private String model = "qwen-plus";
    private String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
}
