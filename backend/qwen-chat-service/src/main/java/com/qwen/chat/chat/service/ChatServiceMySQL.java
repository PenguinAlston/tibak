package com.qwen.chat.chat.service;

import com.qwen.chat.chat.config.AiConfig;
import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.entity.ConversationMybatis;
import com.qwen.chat.chat.entity.MessageMybatis;
import com.qwen.chat.chat.repository.ConversationMapper;
import com.qwen.chat.chat.repository.MessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "qwen.chat.use-mongodb", havingValue = "false", matchIfMissing = true)
public class ChatServiceMySQL implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final AiConfig aiConfig;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        return Flux.create(sink -> {
            // 如果 AI 未启用，返回占位回复
            if (!aiConfig.isEnabled() || "your-api-key-here".equals(aiConfig.getApiKey())) {
                String mockResponse = "您好！AI 功能尚未配置。要使用真实的 AI 对话功能，请设置环境变量 DASHSCOPE_API_KEY。\n\n" +
                    "您发送的消息是：" + request.getUserMessage();
                sink.next(mockResponse);
                sink.complete();
                saveConversation(userId, request, mockResponse);
                return;
            }

            try {
                // 构建请求体
                String requestBody = buildRequestBody(request.getUserMessage());

                // 创建 HTTP 请求
                Request httpRequest = new Request.Builder()
                    .url(aiConfig.getApiUrl())
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

                // 异步执行请求
                httpClient.newCall(httpRequest).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        log.error("HTTP request failed: {}", e.getMessage(), e);
                        sink.error(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            if (!response.isSuccessful()) {
                                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                                log.error("API response failed: {}", errorBody);
                                sink.error(new RuntimeException("API 请求失败：" + errorBody));
                                return;
                            }

                            String responseBody = response.body().string();
                            JsonNode jsonNode = objectMapper.readTree(responseBody);

                            // 解析响应
                            JsonNode outputNode = jsonNode.get("output");
                            if (outputNode != null && outputNode.has("text")) {
                                String content = outputNode.get("text").asText();
                                sink.next(content);
                                sink.complete();

                                // 保存对话到数据库
                                saveConversation(userId, request, content);
                            } else {
                                sink.error(new RuntimeException("无法解析 AI 响应"));
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse response: {}", e.getMessage(), e);
                            sink.error(e);
                        }
                    }
                });

            } catch (Exception e) {
                log.error("Chat error: {}", e.getMessage(), e);
                sink.error(e);
            }
        });
    }

    private String buildRequestBody(String userMessage) throws IOException {
        ObjectNode inputNode = objectMapper.createObjectNode();
        ArrayNode messagesNode = inputNode.putArray("messages");
        ObjectNode messageNode = messagesNode.addObject();
        messageNode.put("role", "user");
        messageNode.put("content", userMessage);

        ObjectNode parametersNode = objectMapper.createObjectNode();
        parametersNode.put("model", aiConfig.getModel());

        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.set("input", inputNode);
        rootNode.set("parameters", parametersNode);
        rootNode.put("model", aiConfig.getModel());

        return objectMapper.writeValueAsString(rootNode);
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
