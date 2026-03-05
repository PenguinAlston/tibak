package com.qwen.chat.chat.service;

import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.entity.Conversation;
import com.qwen.chat.chat.entity.ConversationMybatis;
import com.qwen.chat.chat.entity.Message;
import com.qwen.chat.chat.entity.MessageMybatis;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {

    ConversationMybatis createConversation(String userId, String title);

    ConversationMybatis getConversation(String userId, String conversationId);

    List<ConversationMybatis> getConversations(String userId);

    void deleteConversation(String userId, String conversationId);

    ConversationMybatis renameConversation(String userId, String conversationId, String newTitle);

    Flux<String> streamChat(String userId, ChatRequest request);

    MessageMybatis saveMessage(String conversationId, MessageMybatis message);

    /**
     * 获取或创建对话 ID
     * 如果提供了 conversationId 且存在，则返回该 ID
     * 否则创建一个新对话并返回 ID
     */
    String getOrCreateConversationId(String userId, String conversationId, String userMessage);
}
