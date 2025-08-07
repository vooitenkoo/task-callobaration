package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.MessageRequestDTO;
import com.example.task_collaboration.application.dto.MessageResponseDTO;
import com.example.task_collaboration.domain.model.Message;
import com.example.task_collaboration.domain.model.User;

import java.time.Instant;

public class MessageMapper {

    public static Message toEntity(MessageRequestDTO dto) {
        Message message = new Message();
        message.setContent(dto.content());
        message.setSentAt(Instant.now());
        message.setRead(false);
        return message;
    }

    public static MessageResponseDTO toDto(Message entity) {
        return new MessageResponseDTO(
                entity.getId(),
                entity.getContent(),
                entity.getSentAt(),
                entity.isRead(),
                entity.getProject().getId(),
                toSenderDto(entity.getSender())
        );
    }

    public static MessageResponseDTO.SenderDTO toSenderDto(User user) {
        return new MessageResponseDTO.SenderDTO(
                user.getId(),
                user.getName()
        );
    }
} 