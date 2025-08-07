package com.example.task_collaboration.infrastructure.websocket;

import com.example.task_collaboration.application.dto.MessageRequestDTO;
import com.example.task_collaboration.application.dto.MessageResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.MessageService;
import com.example.task_collaboration.domain.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatWebSocketHandler {

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

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, 
                          SimpMessageHeaderAccessor headerAccessor) {
        try {
            Principal principal = headerAccessor.getUser();
            if (principal == null) {
                throw new RuntimeException("User not authenticated");
            }

            // Получаем пользователя
            User user = userService.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Создаем DTO для сервиса
            MessageRequestDTO dto = new MessageRequestDTO(request.content(), request.projectId());

            // Отправляем сообщение через сервис
            MessageResponseDTO response = messageService.sendMessage(user, dto);

            // Отправляем сообщение всем участникам проекта
            messagingTemplate.convertAndSend("/topic/project/" + request.projectId(), response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageRequest request, 
                       SimpMessageHeaderAccessor headerAccessor) {
        // Добавляем пользователя в WebSocket сессию
        headerAccessor.getSessionAttributes().put("username", request.senderName());
        headerAccessor.getSessionAttributes().put("projectId", request.projectId());
        
        // Отправляем уведомление о подключении пользователя
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