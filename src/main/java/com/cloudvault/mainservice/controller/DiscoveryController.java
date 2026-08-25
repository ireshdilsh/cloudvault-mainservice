package com.cloudvault.mainservice.controller;

import com.cloudvault.mainservice.entity.*;
import com.cloudvault.mainservice.exception.ResourceNotFoundException;
import com.cloudvault.mainservice.repository.*;
import com.cloudvault.mainservice.security.CurrentUserService;
import java.time.YearMonth;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DiscoveryController {
    private final CurrentUserService current;
    private final MemoryRepository memories;
    private final MemoryPhotoRepository photos;
    private final PlaceRepository places;
    @Value("${app.subscription.free-limit-bytes}")
    private long freeLimit;

    @GetMapping("/v1/dashboard")
    public Map<String, Object> dashboard() {
        Long u = current.getCurrentUserId();
        return Map.of("data", Map.of("memoryCount", memories.countByUserIdAndDeletedAtIsNull(u), "placeCount",
                places.countByUserId(u), "photoCount",
                photos.countByMemoryIdIn(
                        memories.findByUserIdAndDeletedAtIsNullOrderByDateDesc(u).stream().map(Memory::getId).toList()),
                "recentMemories", memories.findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(u).stream().limit(5)
                        .map(this::summary).toList(),
                "storage", storage(u)));
    }

    @GetMapping("/v1/storage")
    public Map<String, Object> storageEndpoint() {
        return Map.of("data", storage(current.getCurrentUserId()));
    }

    @GetMapping("/v1/places")
    public Map<String, Object> places(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Long u = current.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)));
        Page<Place> p = search == null || search.isBlank() ? places.findByUserId(u, pageable)
                : places.findByUserIdAndNameContainingIgnoreCase(u, search, pageable);
        return Map.of("data", p.getContent().stream().map(this::place).toList(), "page", p.getNumber(), "size",
                p.getSize(), "totalElements", p.getTotalElements(), "totalPages", p.getTotalPages());
    }

    @GetMapping("/v1/places/{id}")
    public Map<String, Object> place(@PathVariable UUID id) {
        return Map.of("data", place(ownedPlace(id)));
    }

    @GetMapping("/v1/places/{id}/memories")
    public Map<String, Object> placeMemories(@PathVariable UUID id, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ownedPlace(id);
        Page<Memory> p = memories.findByUserIdAndPlaceIdAndDeletedAtIsNull(current.getCurrentUserId(), id,
                PageRequest.of(page, Math.min(100, Math.max(1, size)), Sort.by(Sort.Direction.DESC, "date")));
        return Map.of("data", p.getContent().stream().map(this::summary).toList(), "page", p.getNumber(), "size",
                p.getSize(), "totalElements", p.getTotalElements(), "totalPages", p.getTotalPages());
    }

    @GetMapping("/v1/map/locations")
    public Map<String, Object> map() {
        return Map.of("data", places.findByUserId(current.getCurrentUserId(), Pageable.unpaged()).stream()
                .filter(p -> p.getLatitude() != null && p.getLongitude() != null).map(this::place).toList());
    }

    @GetMapping("/v1/timeline")
    public Map<String, Object> timeline() {
        Map<Integer, Map<YearMonth, List<Map<String, Object>>>> grouped = new TreeMap<>(Comparator.reverseOrder());
        for (Memory m : memories.findByUserIdAndDeletedAtIsNullOrderByDateDesc(current.getCurrentUserId())) {
            YearMonth ym = YearMonth.from(m.getDate());
            grouped.computeIfAbsent(ym.getYear(), x -> new TreeMap<>(Comparator.reverseOrder()))
                    .computeIfAbsent(ym, x -> new ArrayList<>()).add(summary(m));
        }
        return Map.of("data",
                grouped.entrySet().stream()
                        .map(y -> Map.of("year", y.getKey(), "months", y.getValue().entrySet().stream()
                                .map(x -> Map.of("month", x.getKey().getMonth().toString(), "memories", x.getValue()))
                                .toList()))
                        .toList());
    }

    private Place ownedPlace(UUID id) {
        return places.findByIdAndUserId(id, current.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Place not found"));
    }

    private Map<String, Object> place(Place p) {
        return Map.of("id", p.getId(), "name", p.getName(), "memoryCount", memories
                .findByUserIdAndPlaceIdAndDeletedAtIsNull(current.getCurrentUserId(), p.getId(), Pageable.unpaged())
                .getTotalElements());
    }

    private Map<String, Object> summary(Memory m) {
        return Map.of("id", m.getId(), "title", m.getTitle(), "date", m.getDate(), "category", m.getCategory());
    }

    private Map<String, Object> storage(Long u) {
        long used = photos.totalSizeForUser(u);
        return Map.of("usedBytes", used, "limitBytes", freeLimit, "usedPercentage",
                freeLimit == 0 ? 0 : (used * 100.0 / freeLimit), "photoBytes", used, "attachmentBytes", 0);
    }
}
