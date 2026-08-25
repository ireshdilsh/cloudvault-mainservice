package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.AppNotification;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AppNotificationRepository extends JpaRepository<AppNotification, UUID> {
    Page<AppNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<AppNotification> findByIdAndUserId(UUID id, Long userId);

    long countByUserIdAndReadFalse(Long userId);

    @Modifying
    @Query("update AppNotification n set n.read=true where n.userId=:userId and n.read=false")
    int markAllRead(Long userId);
}
