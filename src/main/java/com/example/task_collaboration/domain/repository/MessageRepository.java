package com.example.task_collaboration.domain.repository;

import com.example.task_collaboration.domain.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    @Query("SELECT m FROM Message m WHERE m.project.id = :projectId ORDER BY m.sentAt DESC")
    List<Message> findByProjectIdOrderBySentAtDesc(@Param("projectId") UUID projectId);
    
    @Query("SELECT m FROM Message m WHERE m.project.id = :projectId ORDER BY m.sentAt ASC")
    List<Message> findByProjectIdOrderBySentAtAsc(@Param("projectId") UUID projectId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.project.id = :projectId AND m.sender.id = :userId AND m.isRead = false")
    Long countUnreadMessagesByProjectAndUser(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
} 