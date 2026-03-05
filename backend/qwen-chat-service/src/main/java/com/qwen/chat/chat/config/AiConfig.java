package com.qwen.chat.chat.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.dashscope.DashscopeChatModel;
import org.springframework.ai.dashscope.api.DashscopeApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${ai.dashscope.api-key:}")
    private String apiKey;

    @Value("${ai.dashscope.model:qwen-plus}")
    private String model;

    @Bean
    public ChatClient chatClient(DashscopeChatModel dashscopeChatModel) {
        return ChatClient.builder(dashscopeChatModel).build();
    }

    @Bean
    public DashscopeChatModel dashscopeChatModel() {
        DashscopeApi dashscopeApi = DashscopeApi.builder()
                .apiKey(apiKey)
                .build();

        return DashscopeChatModel.builder()
                .dashscopeApi(dashscopeApi)
                .defaultOptions(
                    org.springframework.ai.dashscope.api.DashscopeApi.ChatOptions.builder()
                        .withModel(model)
                        .build()
                )
                .build();
    }
}
