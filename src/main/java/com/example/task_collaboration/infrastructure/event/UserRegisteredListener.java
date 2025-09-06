package com.example.task_collaboration.infrastructure.event;

import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.infrastructure.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredListener {
    private static final Logger logger = LoggerFactory.getLogger(UserRegisteredListener.class);

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        logger.info("New user registered with email: {}", user.getEmail());

    }
}