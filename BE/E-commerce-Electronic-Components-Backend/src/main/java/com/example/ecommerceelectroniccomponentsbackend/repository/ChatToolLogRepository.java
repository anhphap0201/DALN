package com.example.ecommerceelectroniccomponentsbackend.repository;

import com.example.ecommerceelectroniccomponentsbackend.entity.ChatToolLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatToolLogRepository extends JpaRepository<ChatToolLog, Long> {
    List<ChatToolLog> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    List<ChatToolLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(String status);

    @Query("SELECT AVG(c.durationMs) FROM ChatToolLog c WHERE c.durationMs IS NOT NULL")
    Double getAverageExecutionTime();

    @Query("SELECT c.toolName, COUNT(c) as cnt FROM ChatToolLog c GROUP BY c.toolName ORDER BY cnt DESC")
    List<Object[]> getTopToolsUsed();

    List<ChatToolLog> findTop100ByOrderByCreatedAtDesc();
}
