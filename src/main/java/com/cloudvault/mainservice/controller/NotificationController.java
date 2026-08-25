package com.cloudvault.mainservice.controller;

import com.cloudvault.mainservice.entity.AppNotification;
import com.cloudvault.mainservice.exception.ResourceNotFoundException;
import com.cloudvault.mainservice.repository.AppNotificationRepository;
import com.cloudvault.mainservice.security.CurrentUserService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final AppNotificationRepository notifications;
    private final CurrentUserService current;

    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AppNotification> p = notifications.findByUserIdOrderByCreatedAtDesc(current.getCurrentUserId(),
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
        return Map.of("data", p.getContent().stream().map(this::view).toList(), "page", p.getNumber(), "size",
                p.getSize(), "totalElements", p.getTotalElements(), "totalPages", p.getTotalPages());
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unread() {
        return Map.of("data", Map.of("count", notifications.countByUserIdAndReadFalse(current.getCurrentUserId())));
    }

    @PutMapping("/{id}/read")
    @Transactional
    public Map<String, Object> read(@PathVariable UUID id) {
        AppNotification n = owned(id);
        n.setRead(true);
        return Map.of("data", view(n));
    }

    @PutMapping("/read-all")
    @Transactional
    public ResponseEntity<Void> all() {
        notifications.markAllRead(current.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notifications.delete(owned(id));
        return ResponseEntity.noContent().build();
    }

    private AppNotification owned(UUID id) {
        return notifications.findByIdAndUserId(id, current.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private Map<String, Object> view(AppNotification n) {
        return Map.of("id", n.getId(), "type", n.getType(), "title", n.getTitle(), "message", n.getMessage(), "read",
                n.isRead(), "createdAt", n.getCreatedAt());
    }
}
