package com.qwen.chat.chat.controller;

import com.qwen.chat.chat.dto.ChatRequest;
import com.qwen.chat.chat.dto.ChatResponse;
import com.qwen.chat.chat.entity.Conversation;
import com.qwen.chat.chat.service.ChatService;
import com.qwen.chat.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> streamCompletion(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails user) {

        return chatService.streamChat(user.getUsername(), request)
            .map(content -> new ChatResponse(content, request.getConversationId()))
            .onErrorResume(e -> {
                return Flux.just(new ChatResponse("Error: " + e.getMessage(), null));
            });
    }

    @GetMapping("/conversations")
    public ApiResponse<List<Conversation>> getConversations(
            @AuthenticationPrincipal UserDetails user) {
        List<Conversation> conversations = chatService.getConversations(user.getUsername());
        return ApiResponse.success(conversations);
    }

    @PostMapping("/conversations")
    public ApiResponse<Conversation> createConversation(
            @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        Conversation conversation = chatService.createConversation(user.getUsername(), request.getTitle());
        return ApiResponse.success(conversation);
    }

    @GetMapping("/conversations/{id}")
    public ApiResponse<Conversation> getConversation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        Conversation conversation = chatService.getConversation(user.getUsername(), id);
        return ApiResponse.success(conversation);
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails user) {
        chatService.deleteConversation(user.getUsername(), id);
        return ApiResponse.success(null);
    }

    @PutMapping("/conversations/{id}")
    public ApiResponse<Conversation> renameConversation(
            @PathVariable String id,
            @RequestBody RenameConversationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        Conversation conversation = chatService.renameConversation(user.getUsername(), id, request.getTitle());
        return ApiResponse.success(conversation);
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
