// com.example.task_collaboration.domain.model.Analytics.java
package com.example.task_collaboration.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analytics")
@Getter
@Setter
public class Analytics {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 50)
    private String metricType;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Instant timestamp;
}