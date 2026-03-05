package com.qwen.chat.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ChatRequest {

    @JsonProperty("system_message")
    private String systemMessage;

    @JsonProperty("user_message")
    @NotBlank(message = "消息内容不能为空")
    private String userMessage;

    private String model;

    @JsonProperty("conversation_id")
    private String conversationId;
}
