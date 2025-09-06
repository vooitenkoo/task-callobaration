package com.example.task_collaboration.infrastructure.websocket;

import com.example.task_collaboration.application.dto.MessageRequestDTO;
import com.example.task_collaboration.application.dto.MessageResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.MessageService;
import com.example.task_collaboration.domain.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Controller
public class ChatWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(MessageService messageService, UserService userService,
                                SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    @MessageMapping("/chat.sendMessage")
    public CompletableFuture<Void> sendMessage(@Payload ChatMessageRequest request,
                                               SimpMessageHeaderAccessor headerAccessor) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.debug("Processing message for project: {}", request.projectId());
                Principal principal = headerAccessor.getUser();
                if (principal == null) {
                    throw new RuntimeException("User not authenticated");
                }

                User user = userService.findByEmail(principal.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                MessageRequestDTO dto = new MessageRequestDTO(request.content(), request.projectId());
                MessageResponseDTO response = messageService.sendMessage(user, dto);
                messagingTemplate.convertAndSend("/topic/project/" + request.projectId(), response);
                logger.debug("Message sent successfully for project: {}", request.projectId());
            } catch (Exception e) {
                logger.error("Failed to send message: {}", e.getMessage());
                throw new RuntimeException("Failed to send message: " + e.getMessage());
            }
        });
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageRequest request,
                        SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", request.senderName());
        headerAccessor.getSessionAttributes().put("projectId", request.projectId());

        MessageResponseDTO systemMessage = new MessageResponseDTO(
                UUID.randomUUID(),
                request.senderName() + " joined the chat",
                java.time.Instant.now(),
                false,
                request.projectId(),
                new MessageResponseDTO.SenderDTO(UUID.randomUUID(), "System")
        );

        messagingTemplate.convertAndSend("/topic/project/" + request.projectId(), systemMessage);
    }

    public record ChatMessageRequest(
            String content,
            UUID projectId,
            String senderName
    ) {}
}