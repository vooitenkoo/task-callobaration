package com.example.task_collaboration.infrastructure.controller;

import com.example.task_collaboration.application.dto.MessageRequestDTO;
import com.example.task_collaboration.application.dto.MessageResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.domain.service.MessageService;
import com.example.task_collaboration.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDTO sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MessageRequestDTO dto) {
        User currentUser = userService.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return messageService.sendMessage(currentUser, dto);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public List<MessageResponseDTO> getProjectMessages(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return messageService.getProjectMessages(projectId, userDetails.getUser().getId());
    }

    @PutMapping("/project/{projectId}/read")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<Void> markMessagesAsRead(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return messageService.markMessagesAsRead(projectId, userDetails.getUser().getId());
    }

    @GetMapping("/project/{projectId}/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Long getUnreadMessageCount(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return messageService.getUnreadMessageCount(projectId, userDetails.getUser().getId());
    }
}