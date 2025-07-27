package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.domain.model.Profile;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void registerUser(User user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }

        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }

        if (user.getProfile() == null) {
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setDisplayName(user.getName());
            profile.setBio("Hello, I am a new user!");
            profile.setAvatarUrl("default_avatar.png");
            profile.setLocation("");
            profile.setJobTitle("");
            user.setProfile(profile);
        }

        userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> findById(Long newLeadId) {
        User user = userRepository.findById(newLeadId);
        return Optional.ofNullable(user);
    }

}