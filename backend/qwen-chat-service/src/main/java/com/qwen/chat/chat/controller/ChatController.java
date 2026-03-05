package com.qwen.chat.chat.controller;

import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.dto.ChatResponse;
import com.qwen.chat.chat.dto.ConversationDTO;
import com.qwen.chat.chat.entity.ConversationMybatis;
import com.qwen.chat.chat.entity.MessageMybatis;
import com.qwen.chat.chat.service.ChatService;
import com.qwen.chat.common.response.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qwen.chat.chat.repository.ConversationMapper;
import com.qwen.chat.chat.repository.MessageMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamCompletion(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails user) {

        // 获取或创建对话 ID
        String conversationId = chatService.getOrCreateConversationId(
            user.getUsername(),
            request.getConversationId(),
            request.getUserMessage()
        );

        // 更新请求中的 conversationId
        request.setConversationId(conversationId);

        return chatService.streamChat(user.getUsername(), request)
            .map(content -> new ChatResponse(content, null))
            .concatWith(Flux.defer(() -> {
                // 返回 conversation_id 给前端
                return Flux.just(new ChatResponse("[DONE]", conversationId));
            }))
            .onErrorResume(e -> {
                return Flux.just(new ChatResponse("Error: " + e.getMessage(), null));
            });
    }

    @GetMapping("/conversations")
    public ApiResponse<List<ConversationDTO>> getConversations(
            @AuthenticationPrincipal UserDetails user) {
        List<ConversationMybatis> conversations = chatService.getConversations(user.getUsername());
        List<ConversationDTO> dtos = conversations.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @PostMapping("/conversations")
    public ApiResponse<ConversationDTO> createConversation(
            @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        ConversationMybatis conversation = chatService.createConversation(user.getUsername(), request.getTitle());
        return ApiResponse.success(convertToDTO(conversation));
    }

    @GetMapping("/conversations/{id}")
    public ApiResponse<ConversationDTO> getConversation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        ConversationMybatis conversation = chatService.getConversation(user.getUsername(), id);
        ConversationDTO dto = convertToDTOWithMessages(conversation);
        return ApiResponse.success(dto);
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        chatService.deleteConversation(user.getUsername(), id);
        return ApiResponse.success(null);
    }

    @PutMapping("/conversations/{id}")
    public ApiResponse<ConversationDTO> renameConversation(
            @PathVariable String id,
            @RequestBody RenameConversationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        ConversationMybatis conversation = chatService.renameConversation(user.getUsername(), id, request.getTitle());
        return ApiResponse.success(convertToDTO(conversation));
    }

    private ConversationDTO convertToDTO(ConversationMybatis conversation) {
        return ConversationDTO.builder()
            .id(conversation.getId())
            .userId(conversation.getUserId())
            .username(conversation.getUsername())
            .title(conversation.getTitle())
            .model(conversation.getModel())
            .isPublic(conversation.getIsPublic())
            .createdAt(conversation.getCreatedAt())
            .updatedAt(conversation.getUpdatedAt())
            .build();
    }

    private ConversationDTO convertToDTOWithMessages(ConversationMybatis conversation) {
        LambdaQueryWrapper<MessageMybatis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageMybatis::getConversationId, conversation.getId())
               .orderByAsc(MessageMybatis::getCreatedAt);
        List<MessageMybatis> messages = messageMapper.selectList(wrapper);

        List<ConversationDTO.MessageDTO> messageDTOs = messages.stream()
            .map(m -> ConversationDTO.MessageDTO.builder()
                .id(m.getId())
                .conversationId(m.getConversationId())
                .role(m.getRole())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return ConversationDTO.builder()
            .id(conversation.getId())
            .userId(conversation.getUserId())
            .username(conversation.getUsername())
            .title(conversation.getTitle())
            .model(conversation.getModel())
            .isPublic(conversation.getIsPublic())
            .createdAt(conversation.getCreatedAt())
            .updatedAt(conversation.getUpdatedAt())
            .messages(messageDTOs)
            .build();
    }

    public static class CreateConversationRequest {
        private String title;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

    public static class RenameConversationRequest {
        private String title;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
