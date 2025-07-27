package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.ProfileRequestDTO;
import com.example.task_collaboration.application.dto.ProfileResponseDTO;
import com.example.task_collaboration.domain.model.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapperImpl {

    public Profile toEntity(ProfileRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Profile profile = new Profile();
        profile.setBio(dto.getBio());
        profile.setLocation(dto.getLocation());
        profile.setJobTitle(dto.getJobTitle());
        return profile;
    }

    public ProfileResponseDTO toDto(Profile profile) {
        if (profile == null) {
            return null;
        }

        ProfileResponseDTO profileResponseDTO = new ProfileResponseDTO();
        if (profile.getId() != null) {
            profileResponseDTO.setId(profile.getId());
        }
        profileResponseDTO.setDisplayName(profile.getDisplayName());
        profileResponseDTO.setAvatarUrl(profile.getAvatarUrl());
        profileResponseDTO.setBio(profile.getBio());
        profileResponseDTO.setLocation(profile.getLocation());
        profileResponseDTO.setJobTitle(profile.getJobTitle());
        return profileResponseDTO;
    }

    public void updateProfileFromDto(ProfileRequestDTO profileRequest, Profile existingProfile) {
        if (profileRequest == null || existingProfile == null) {
            return;
        }

        existingProfile.setBio(profileRequest.getBio());
        existingProfile.setLocation(profileRequest.getLocation());
        existingProfile.setJobTitle(profileRequest.getJobTitle());
    }
}