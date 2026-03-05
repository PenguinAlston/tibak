package com.qwen.chat.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    private String id;

    @JsonProperty("user_id")
    private String userId;

    private String username;

    private String title;

    private String model;

    @JsonProperty("is_public")
    private Boolean isPublic;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private List<MessageDTO> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDTO {
        private String id;

        @JsonProperty("conversation_id")
        private String conversationId;

        private String role;

        private String content;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;
    }
}
