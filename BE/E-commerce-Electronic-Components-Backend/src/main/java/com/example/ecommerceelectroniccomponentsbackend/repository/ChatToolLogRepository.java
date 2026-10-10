package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatToolLogRepository extends JpaRepository<ChatToolLog, Long> {
    List<ChatToolLog> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    List<ChatToolLog> findByUserIdOrderByCreatedAtDesc(Long userId);
}
