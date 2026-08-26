package com.cloudvault.mainservice.controller;

import com.cloudvault.mainservice.dto.ProfileRequest;
import com.cloudvault.mainservice.entity.*;
import com.cloudvault.mainservice.exception.*;
import com.cloudvault.mainservice.repository.*;
import com.cloudvault.mainservice.security.CurrentUserService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final CurrentUserService current;
    private final UserAccountRepository users;
    private final MemoryRepository memories;
    private final PlaceRepository places;
    private final MemoryPhotoRepository photos;

    @GetMapping
    public Map<String, Object> profile() {
        UserAccount u = user();
        return Map.of("data",
                Map.of());
    }

    @PutMapping
    @Transactional
    public Map<String, Object> update(@Valid @RequestBody ProfileRequest r) {
        UserAccount u = user();
        // if (users.findByUsernameIgnoreCase(r.username()).isPresent())
        //     throw new DuplicateResourceException("Username is already in use");
        // u.setName(r.name().trim());
        // u.setUsername(r.username().trim());
        return profile();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Long id = current.getCurrentUserId();
        return Map.of("data", Map.of("memoryCount", memories.countByUserIdAndDeletedAtIsNull(id), "placeCount",
                places.countByUserId(id), "photoCount", photos.countByMemoryIdIn(memories
                        .findByUserIdAndDeletedAtIsNullOrderByDateDesc(id).stream().map(Memory::getId).toList())));
    }

    private UserAccount user() {
        return users.findByIdAndStatusNot(current.getCurrentUserId(), UserStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }
}
