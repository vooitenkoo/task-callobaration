package com.example.task_collaboration.infrastructure.controller;

import com.example.task_collaboration.application.dto.MessageRequestDTO;
import com.example.task_collaboration.application.dto.MessageResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.domain.service.MessageService;
import com.example.task_collaboration.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Messages", description = "Message management endpoints")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Operation(summary = "Send a message", description = "Sends a message to a project chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User or project not found")
    })
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

    @Operation(summary = "Get project messages", description = "Retrieves all messages for a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public List<MessageResponseDTO> getProjectMessages(
            @Parameter(description = "Project ID")
            @PathVariable UUID projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return messageService.getProjectMessages(projectId, userDetails.getUser().getId());
    }

    @Operation(summary = "Mark messages as read", description = "Marks all messages in a project as read for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages marked as read successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PutMapping("/project/{projectId}/read")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<Void> markMessagesAsRead(
            @Parameter(description = "Project ID")
            @PathVariable UUID projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return messageService.markMessagesAsRead(projectId, userDetails.getUser().getId());
    }

    @Operation(summary = "Get unread message count", description = "Gets the count of unread messages for a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Long getUnreadMessageCount(
            @Parameter(description = "Project ID")
            @PathVariable UUID projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return messageService.getUnreadMessageCount(projectId, userDetails.getUser().getId());
    }
}