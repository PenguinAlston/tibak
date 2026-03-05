package com.qwen.chat.chat.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String id;

    @Field("conversation_id")
    private String conversationId;

    @Field("role")
    @JsonProperty("role")
    private String role; // USER or ASSISTANT

    @Field("content")
    @JsonProperty("content")
    private String content;

    @Field("created_at")
    private LocalDateTime createdAt;
}
