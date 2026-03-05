package com.qwen.chat.chat.service;

import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.entity.ConversationMybatis;
import com.qwen.chat.chat.entity.MessageMybatis;
import com.qwen.chat.chat.repository.ConversationMapper;
import com.qwen.chat.chat.repository.MessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "qwen.chat.use-mongodb", havingValue = "false", matchIfMissing = true)
public class ChatServiceMySQL implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    public ConversationMybatis createConversation(String userId, String title) {
        ConversationMybatis conversation = ConversationMybatis.builder()
                .userId(userId)
                .title(title != null ? title : "新对话")
                .model("qwen-plus")
                .isPublic(false)
                .build();
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public ConversationMybatis getConversation(String userId, String conversationId) {
        ConversationMybatis conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问该对话");
        }
        return conversation;
    }

    @Override
    public List<ConversationMybatis> getConversations(String userId) {
        LambdaQueryWrapper<ConversationMybatis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationMybatis::getUserId, userId)
               .orderByDesc(ConversationMybatis::getUpdatedAt);
        return conversationMapper.selectList(wrapper);
    }

    @Override
    public void deleteConversation(String userId, String conversationId) {
        getConversation(userId, conversationId);
        // 先删除消息
        LambdaQueryWrapper<MessageMybatis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageMybatis::getConversationId, conversationId);
        messageMapper.delete(wrapper);
        // 再删除对话
        conversationMapper.deleteById(conversationId);
    }

    @Override
    public ConversationMybatis renameConversation(String userId, String conversationId, String newTitle) {
        ConversationMybatis conversation = getConversation(userId, conversationId);
        conversation.setTitle(newTitle);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.updateById(conversation);
        return conversation;
    }

    @Override
    public Flux<String> streamChat(String userId, ChatRequest request) {
        // TODO: 实现真实的 AI 调用
        // 需要配置 Spring AI DashScope 依赖后才能使用
        return Flux.create(sink -> {
            try {
                // 占位实现：返回固定回复
                String mockResponse = "您好！这是一个占位回复。要使用真实的 AI 对话功能，需要配置 Spring AI DashScope 依赖。\n\n" +
                    "请在 pom.xml 中添加：\n" +
                    "1. spring-ai-dashscope-spring-boot-starter\n" +
                    "2. 在 application.yml 中配置 dashscope.api-key\n\n" +
                    "您发送的消息是：" + request.getUserMessage();

                sink.next(mockResponse);
                sink.complete();

                // 保存对话到数据库
                saveConversation(userId, request, mockResponse);
            } catch (Exception e) {
                log.error("Chat error: {}", e.getMessage(), e);
                sink.error(e);
            }
        });
    }

    @Override
    public MessageMybatis saveMessage(String conversationId, MessageMybatis message) {
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return message;
    }

    @Override
    public String getOrCreateConversationId(String userId, String conversationId, String userMessage) {
        try {
            if (conversationId != null && !conversationId.isEmpty()) {
                ConversationMybatis conversation = getConversation(userId, conversationId);
                return conversation.getId();
            }
        } catch (Exception e) {
            log.warn("Conversation not found, creating new one");
        }

        String title = userMessage != null && userMessage.length() > 30
            ? userMessage.substring(0, 30) + "..."
            : (userMessage != null ? userMessage : "新对话");

        ConversationMybatis conversation = createConversation(userId, title);
        return conversation.getId();
    }

    private void saveConversation(String userId, ChatRequest request, String responseContent) {
        try {
            ConversationMybatis conversation;

            if (request.getConversationId() != null) {
                conversation = getConversation(userId, request.getConversationId());
            } else {
                String title = request.getUserMessage().length() > 30
                    ? request.getUserMessage().substring(0, 30) + "..."
                    : request.getUserMessage();
                conversation = createConversation(userId, title);
            }

            MessageMybatis userMessage = MessageMybatis.builder()
                    .conversationId(conversation.getId())
                    .role("USER")
                    .content(request.getUserMessage())
                    .build();

            MessageMybatis assistantMessage = MessageMybatis.builder()
                    .conversationId(conversation.getId())
                    .role("ASSISTANT")
                    .content(responseContent)
                    .build();

            messageMapper.insert(userMessage);
            messageMapper.insert(assistantMessage);

            conversation.setUpdatedAt(LocalDateTime.now());
            conversationMapper.updateById(conversation);

            log.info("Conversation saved: {}", conversation.getId());
        } catch (Exception e) {
            log.error("Failed to save conversation: {}", e.getMessage(), e);
        }
    }
}
