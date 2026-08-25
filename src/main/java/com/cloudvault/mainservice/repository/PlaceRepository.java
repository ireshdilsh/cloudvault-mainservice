package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.Place;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, UUID> {
    Optional<Place> findByIdAndUserId(UUID id, Long userId);
    Optional<Place> findByUserIdAndNameIgnoreCase(Long userId, String name);
    Page<Place> findByUserIdAndNameContainingIgnoreCase(Long userId, String search, Pageable pageable);
    Page<Place> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);
}
