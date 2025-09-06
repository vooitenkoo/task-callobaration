package com.example.task_collaboration.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Минимум 10 потоков всегда доступны
        executor.setMaxPoolSize(20); // Максимум 20 потоков при высокой нагрузке
        executor.setQueueCapacity(50); // Очередь на 50 задач, если все потоки заняты
        executor.setThreadNamePrefix("AsyncTask-"); // Имена потоков для отладки
        executor.initialize(); // Инициализация пула
        return executor;
    }
}