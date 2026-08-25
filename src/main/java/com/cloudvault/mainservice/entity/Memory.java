package com.cloudvault.mainservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "memory", indexes = {
        @Index(name = "idx_memory_user_date", columnList = "user_id,memory_date"),
        @Index(name = "idx_memory_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_memory_place", columnList = "place_id") })
public class Memory {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 160) private String title;
    @Column(length = 4000) private String description;
    @Column(name = "memory_date", nullable = false) private LocalDate date;
    @Column(length = 200) private String location;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "place_id") private Place place;
    private Double latitude;
    private Double longitude;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private MemoryCategory category = MemoryCategory.OTHER;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Visibility visibility = Visibility.PRIVATE;
    @Column(name = "cover_photo_id") private UUID coverPhotoId;
    @ElementCollection @CollectionTable(name = "memory_tag", joinColumns = @JoinColumn(name = "memory_id"))
    @Column(name = "tag", length = 60) private Set<String> tags = new LinkedHashSet<>();
    @Column(name = "deleted_at") private Instant deletedAt;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @PrePersist void created() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate void updated() { updatedAt = Instant.now(); }
}
