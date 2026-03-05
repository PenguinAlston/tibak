package com.qwen.chat.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("messages")
public class MessageMybatis {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("role")
    private String role; // USER or ASSISTANT

    @TableField("content")
    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
