package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
}
