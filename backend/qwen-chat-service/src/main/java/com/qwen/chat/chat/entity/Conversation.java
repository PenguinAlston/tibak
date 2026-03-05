package com.qwen.chat.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    private String id;

    @Field("user_id")
    private String userId;

    @Field("username")
    private String username;

    @Field("title")
    private String title;

    @Field("model")
    private String model;

    @Field("is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("messages")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
}
