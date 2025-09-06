package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.application.dto.MessageRequestDTO;
import com.example.task_collaboration.application.dto.MessageResponseDTO;
import com.example.task_collaboration.application.mapper.MessageMapper;
import com.example.task_collaboration.domain.model.Message;
import com.example.task_collaboration.domain.model.Project;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.MessageRepository;
import com.example.task_collaboration.domain.repository.ProjectMemberRepository;
import com.example.task_collaboration.infrastructure.exсeption.AccessDeniedException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMemberRepository projectMemberRepository;

    public MessageService(MessageRepository messageRepository, ProjectService projectService,
                          UserService userService, ProjectMemberRepository projectMemberRepository) {
        this.messageRepository = messageRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public MessageResponseDTO sendMessage(User sender, MessageRequestDTO dto) {
        Project project = projectService.findProjectByIdAndUser(dto.projectId(), sender.getId())
                .orElseThrow(() -> new AccessDeniedException("Access denied - not a project member"));

        Message message = MessageMapper.toEntity(dto);
        message.setSender(sender);
        message.setProject(project);

        Message savedMessage = messageRepository.save(message);
        return MessageMapper.toDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getProjectMessages(UUID projectId, UUID userId) {
        projectService.findProjectByIdAndUser(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("Access denied - not a project member"));

        return messageRepository.findByProjectIdOrderBySentAtAsc(projectId)
                .stream()
                .map(MessageMapper::toDto)
                .toList();
    }

    @Async // Многопоточность: выполняется в отдельном потоке
    @Transactional
    public CompletableFuture<Void> markMessagesAsRead(UUID projectId, UUID userId) {
        return CompletableFuture.runAsync(() -> {
            projectService.findProjectByIdAndUser(projectId, userId)
                    .orElseThrow(() -> new AccessDeniedException("Access denied - not a project member"));

            List<Message> unreadMessages = messageRepository.findByProjectIdOrderBySentAtAsc(projectId)
                    .stream()
                    .filter(m -> !m.isRead() && !m.getSender().getId().equals(userId))
                    .toList();

            unreadMessages.forEach(message -> message.setRead(true));
            messageRepository.saveAll(unreadMessages);
        });
    }

    @Transactional(readOnly = true)
    public Long getUnreadMessageCount(UUID projectId, UUID userId) {
        projectService.findProjectByIdAndUser(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("Access denied - not a project member"));

        return messageRepository.countUnreadMessagesByProjectAndUser(projectId, userId);
    }
}