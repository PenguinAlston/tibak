package com.qwen.chat.chat.service;

import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.entity.Conversation;
import com.qwen.chat.chat.entity.Message;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {

    Conversation createConversation(String userId, String title);

    Conversation getConversation(String userId, String conversationId);

    List<Conversation> getConversations(String userId);

    void deleteConversation(String userId, String conversationId);

    Conversation renameConversation(String userId, String conversationId, String newTitle);

    Flux<String> streamChat(String userId, ChatRequest request);

    Message saveMessage(String conversationId, Message message);
}
