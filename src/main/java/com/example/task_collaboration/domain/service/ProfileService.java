// com.example.task_collaboration.domain.service.ProfileService.java
package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.application.dto.ProfileRequestDTO;
import com.example.task_collaboration.application.dto.ProfileResponseDTO;
import com.example.task_collaboration.domain.model.Profile;
import com.example.task_collaboration.domain.model.User;

import java.util.UUID;

public interface ProfileService {
    ProfileResponseDTO getProfileByUserId(Long userId);
    ProfileResponseDTO updateProfile(User currentUser, ProfileRequestDTO profileRequest);
}