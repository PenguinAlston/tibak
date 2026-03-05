package com.qwen.chat.chat.service;

import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.entity.Conversation;
import com.qwen.chat.chat.entity.Message;
import com.qwen.chat.chat.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "qwen.chat.use-mongodb", havingValue = "true")
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;

    @Override
    public Conversation createConversation(String userId, String title) {
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .title(title != null ? title : "新对话")
                .model("qwen-plus")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .messages(List.of())
                .build();
        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation getConversation(String userId, String conversationId) {
        Optional<Conversation> optional = conversationRepository.findById(conversationId);
        if (optional.isPresent()) {
            Conversation conversation = optional.get();
            if (!conversation.getUserId().equals(userId)) {
                throw new RuntimeException("无权访问该对话");
            }
            return conversation;
        }
        throw new RuntimeException("对话不存在");
    }

    @Override
    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    public void deleteConversation(String userId, String conversationId) {
        Conversation conversation = getConversation(userId, conversationId);
        conversationRepository.delete(conversation);
    }

    @Override
    public Conversation renameConversation(String userId, String conversationId, String newTitle) {
        Conversation conversation = getConversation(userId, conversationId);
        conversation.setTitle(newTitle);
        conversation.setUpdatedAt(LocalDateTime.now());
        return conversationRepository.save(conversation);
    }

    @Override
    public Flux<String> streamChat(String userId, ChatRequest request) {
        return Flux.create(sink -> {
            try {
                StringBuilder fullContent = new StringBuilder();

                chatClient.prompt()
                    .system(request.getSystemMessage() != null ? request.getSystemMessage() : "你是一个有用的 AI 助手。")
                    .user(request.getUserMessage())
                    .stream()
                    .content(content -> {
                        fullContent.append(content);
                        sink.next(content);
                    })
                    .onError(sink::error)
                    .onComplete(() -> {
                        // 保存对话到数据库
                        saveConversation(userId, request, fullContent.toString());
                        sink.complete();
                    });
            } catch (Exception e) {
                log.error("Chat error: {}", e.getMessage(), e);
                sink.error(e);
            }
        })
        .timeout(java.time.Duration.ofMinutes(5))
        .onErrorResume(e -> {
            log.error("Stream error: {}", e.getMessage());
            return Flux.error(e);
        });
    }

    private void saveConversation(String userId, ChatRequest request, String responseContent) {
        try {
            Conversation conversation;

            if (request.getConversationId() != null) {
                conversation = getConversation(userId, request.getConversationId());
            } else {
                // 创建新对话，使用用户消息的前 30 个字符作为标题
                String title = request.getUserMessage().length() > 30
                    ? request.getUserMessage().substring(0, 30) + "..."
                    : request.getUserMessage();
                conversation = createConversation(userId, title);
            }

            // 添加用户消息
            Message userMessage = Message.builder()
                    .conversationId(conversation.getId())
                    .role("USER")
                    .content(request.getUserMessage())
                    .createdAt(LocalDateTime.now())
                    .build();

            // 添加助手消息
            Message assistantMessage = Message.builder()
                    .conversationId(conversation.getId())
                    .role("ASSISTANT")
                    .content(responseContent)
                    .createdAt(LocalDateTime.now())
                    .build();

            conversation.getMessages().add(userMessage);
            conversation.getMessages().add(assistantMessage);
            conversation.setUpdatedAt(LocalDateTime.now());

            conversationRepository.save(conversation);
            log.info("Conversation saved: {}", conversation.getId());
        } catch (Exception e) {
            log.error("Failed to save conversation: {}", e.getMessage(), e);
        }
    }

    @Override
    public Message saveMessage(String conversationId, Message message) {
        message.setCreatedAt(LocalDateTime.now());
        // MongoDB 会自动处理
        return message;
    }

    @Override
    public String getOrCreateConversationId(String userId, String conversationId, String userMessage) {
        try {
            // 如果提供了 conversationId，检查是否存在
            if (conversationId != null && !conversationId.isEmpty()) {
                Conversation conversation = getConversation(userId, conversationId);
                return conversation.getId();
            }
        } catch (Exception e) {
            log.warn("Conversation not found, creating new one");
        }

        // 创建新对话，使用用户消息的前 30 个字符作为标题
        String title = userMessage != null && userMessage.length() > 30
            ? userMessage.substring(0, 30) + "..."
            : (userMessage != null ? userMessage : "新对话");

        Conversation conversation = createConversation(userId, title);
        return conversation.getId();
    }
}
