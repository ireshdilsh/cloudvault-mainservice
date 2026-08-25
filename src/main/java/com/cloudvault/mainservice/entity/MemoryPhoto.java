package com.cloudvault.mainservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "memory_photo", indexes = @Index(name = "idx_photo_memory", columnList = "memory_id"))
public class MemoryPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "memory_id", nullable = false)
    private UUID memoryId;
    @Column(nullable = false, length = 512)
    private String storageKey;
    @Column(nullable = false, length = 255)
    private String originalName;
    @Column(nullable = false, length = 100)
    private String contentType;
    @Column(nullable = false)
    private long size;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void created() {
        createdAt = Instant.now();
    }
}
