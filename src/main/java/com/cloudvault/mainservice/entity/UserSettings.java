package com.cloudvault.mainservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    private Long userId;
    private boolean memoryReminders = true;
    private boolean accountActivity = true;
    private boolean productUpdates;
    private boolean privateMemories = true;
    private boolean profileVisible;
    private boolean activityVisible;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Theme theme = Theme.SYSTEM;

    public enum Theme {
        LIGHT, DARK, SYSTEM
    }
}
