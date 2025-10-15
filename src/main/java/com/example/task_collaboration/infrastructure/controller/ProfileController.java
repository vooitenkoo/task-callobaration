package com.example.task_collaboration.infrastructure.controller;

import com.example.task_collaboration.application.dto.ProfileRequestDTO;
import com.example.task_collaboration.application.dto.ProfileResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.domain.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "User profile management endpoints")
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Get my profile", responses = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponseDTO> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        ProfileResponseDTO profile = profileService.getProfileByUserId(user.getId());
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Update my profile", responses = {
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input or file size exceeded"),
            @ApiResponse(responseCode = "404", description = "Profile not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error during upload")
    })
    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "jobTitle", required = false) String jobTitle,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {

        User user = userDetails.getUser();

        // Создаем DTO вручную
        ProfileRequestDTO profileRequest = new ProfileRequestDTO();
        profileRequest.setBio(bio);
        profileRequest.setLocation(location);
        profileRequest.setJobTitle(jobTitle);
        profileRequest.setAvatarFile(avatarFile);

        try {
            ProfileResponseDTO updatedProfile = profileService.updateProfile(user, profileRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (MaxUploadSizeExceededException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.error("Error updating profile: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}