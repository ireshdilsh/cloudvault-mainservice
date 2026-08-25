package com.cloudvault.mainservice.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "place", indexes = @Index(name = "idx_place_user_name", columnList = "user_id,name"))
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(length = 120)
    private String country;
    private Double latitude;
    private Double longitude;
}
