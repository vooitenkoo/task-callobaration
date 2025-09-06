package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.domain.model.Profile;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.UserRepository;
import com.example.task_collaboration.infrastructure.event.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher; // Инжекция

    public UserServiceImpl(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

        userRepository.save(user); // Сохраняем пользователя с профилем
        userRepository.updateProfileId(user.getId(), user.getId());

        // Публикуем событие после успешного сохранения
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }
}