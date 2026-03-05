package com.qwen.chat.chat.repository;

import com.qwen.chat.chat.entity.Conversation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId);

    List<Conversation> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    long countByUserId(String userId);
}
