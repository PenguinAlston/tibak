package com.qwen.chat.chat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.qwen.chat.chat", "com.qwen.chat.auth"})
@EntityScan(basePackages = {"com.qwen.chat.chat", "com.qwen.chat.auth"})
@MapperScan("com.qwen.chat.chat.repository")
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }
}
