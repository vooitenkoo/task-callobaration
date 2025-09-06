package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.application.dto.ProfileRequestDTO;
import com.example.task_collaboration.application.dto.ProfileResponseDTO;
import com.example.task_collaboration.application.mapper.ProfileMapperImpl;
import com.example.task_collaboration.domain.model.Profile;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.ProfileRepository;
import com.example.task_collaboration.domain.repository.UserRepository;
import com.example.task_collaboration.infrastructure.exсeption.ProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private final ProfileRepository profileRepository;
    private final MinioService minioStorageService;
    private final ProfileMapperImpl profileMapper;
    private final UserRepository userRepository;

    @Autowired
    public ProfileServiceImpl(ProfileRepository profileRepository, MinioService minioStorageService, ProfileMapperImpl profileMapper, UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.minioStorageService = minioStorageService;
        this.profileMapper = profileMapper;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponseDTO getProfileByUserId(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        logger.debug("Profile from DB: {}", profile);
        return profileMapper.toDto(profile);
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfile(User currentUser, ProfileRequestDTO profileRequest) {
        Profile existingProfile = profileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentUser.getId()));

        // Асинхронная загрузка аватара
        if (profileRequest.getAvatarFile() != null && !profileRequest.getAvatarFile().isEmpty()) {
            CompletableFuture<String> uploadFuture = uploadAvatarAsync(profileRequest.getAvatarFile());
            existingProfile.setAvatarUrl(uploadFuture.join()); // Ждём внутри транзакции
        }

        profileMapper.updateProfileFromDto(profileRequest, existingProfile);
        existingProfile.setUpdatedAt(Instant.now());
        profileRepository.save(existingProfile);

        if (currentUser.getProfile() != null && currentUser.getProfile().getId() != null) {
            userRepository.updateProfileId(currentUser.getId(), currentUser.getProfile().getId());
        }

        return profileMapper.toDto(existingProfile);
    }

    @Async // Многопоточность: выполняется в отдельном потоке
    public CompletableFuture<String> uploadAvatarAsync(MultipartFile avatarFile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return minioStorageService.uploadAvatar(avatarFile);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
            }
        });
    }
}