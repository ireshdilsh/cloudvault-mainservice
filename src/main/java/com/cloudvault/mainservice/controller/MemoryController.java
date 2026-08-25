package com.cloudvault.mainservice.controller;

import com.cloudvault.mainservice.dto.MemoryRequest;
import com.cloudvault.mainservice.entity.*;
import com.cloudvault.mainservice.exception.ResourceNotFoundException;
import com.cloudvault.mainservice.repository.*;
import com.cloudvault.mainservice.security.CurrentUserService;
import com.cloudvault.mainservice.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/memories")
@RequiredArgsConstructor
public class MemoryController {
    private final MemoryRepository memories;
    private final MemoryPhotoRepository photos;
    private final PlaceRepository places;
    private final CurrentUserService current;
    private final FileStorageService storage;

    @Operation(summary = "Create a memory")
    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody MemoryRequest request) {
        Memory m = apply(new Memory(), request, current.getCurrentUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", view(memories.save(m))));
    }

    @Operation(summary = "List current user's memories")
    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String search,
            @RequestParam(required = false) LocalDate dateFrom, @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) String place, @RequestParam(required = false) MemoryCategory category,
            @RequestParam(defaultValue = "newest") String sort, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long user = current.getCurrentUserId();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort(sort));
        Page<Memory> result = memories.findVisible(user, blank(search), dateFrom, dateTo, blank(place), category,
                pageable);
        return page(result);
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return page(memories.findVisible(current.getCurrentUserId(), blank(q), null, null, null, null, PageRequest
                .of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "date"))));
    }

    @GetMapping("/recent")
    public Map<String, Object> recent(@RequestParam(defaultValue = "5") int limit) {
        return Map.of("data",
                memories.findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(current.getCurrentUserId()).stream()
                        .limit(Math.min(Math.max(1, limit), 10)).map(this::view).toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable UUID id) {
        return Map.of("data", view(owned(id)));
    }

    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> update(@PathVariable UUID id, @Valid @RequestBody MemoryRequest request) {
        Memory m = apply(owned(id), request, current.getCurrentUserId());
        return Map.of("data", view(m));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Memory m = owned(id);
        m.setDeletedAt(java.time.Instant.now());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<Map<String, Object>> upload(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        Memory m = owned(id);
        FileStorageService.StoredFile stored = storage.store(id, file);
        MemoryPhoto p = new MemoryPhoto();
        p.setMemoryId(id);
        p.setStorageKey(stored.key());
        p.setContentType(stored.contentType());
        p.setOriginalName(stored.originalName());
        p.setSize(stored.size());
        p = photos.save(p);
        if (m.getCoverPhotoId() == null)
            m.setCoverPhotoId(p.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", photoView(p)));
    }

    @DeleteMapping("/{memoryId}/photos/{photoId}")
    @Transactional
    public ResponseEntity<Void> deletePhoto(@PathVariable UUID memoryId, @PathVariable UUID photoId) {
        Memory m = owned(memoryId);
        MemoryPhoto p = photos.findByIdAndMemoryId(photoId, memoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
        storage.delete(p.getStorageKey());
        photos.delete(p);
        if (photoId.equals(m.getCoverPhotoId()))
            m.setCoverPhotoId(photos.findByMemoryIdOrderByCreatedAtAsc(memoryId).stream()
                    .filter(x -> !x.getId().equals(photoId)).map(MemoryPhoto::getId).findFirst().orElse(null));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{memoryId}/photos/{photoId}/cover")
    @Transactional
    public Map<String, Object> cover(@PathVariable UUID memoryId, @PathVariable UUID photoId) {
        Memory m = owned(memoryId);
        photos.findByIdAndMemoryId(photoId, memoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found"));
        m.setCoverPhotoId(photoId);
        return Map.of("data", view(m));
    }

    private Memory owned(UUID id) {
        return memories.findByIdAndUserIdAndDeletedAtIsNull(id, current.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Memory not found"));
    }

    private Memory apply(Memory m, MemoryRequest r, Long user) {
        m.setUserId(user);
        m.setTitle(r.title().trim());
        m.setDescription(blank(r.description()));
        m.setDate(r.date());
        m.setLocation(blank(r.location()));
        m.setLatitude(r.latitude());
        m.setLongitude(r.longitude());
        m.setCategory(r.category() == null ? MemoryCategory.OTHER : r.category());
        m.setVisibility(r.visibility() == null ? Visibility.PRIVATE : r.visibility());
        m.setTags(r.tags() == null ? new LinkedHashSet<>()
                : r.tags().stream().map(String::trim).filter(s -> !s.isBlank())
                        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        if (m.getLocation() != null && !m.getLocation().isBlank()) {
            Place p = places.findByUserIdAndNameIgnoreCase(user, m.getLocation()).orElseGet(() -> {
                Place x = new Place();
                x.setUserId(user);
                x.setName(m.getLocation());
                x.setLatitude(r.latitude());
                x.setLongitude(r.longitude());
                return places.save(x);
            });
            m.setPlace(p);
        }
        return m;
    }

    private Map<String, Object> view(Memory m) {
        List<Map<String, Object>> ps = photos.findByMemoryIdOrderByCreatedAtAsc(m.getId()).stream().map(this::photoView)
                .toList();
        return new LinkedHashMap<>(Map.of("id", m.getId(), "title", m.getTitle(), "date", m.getDate(), "category",
                m.getCategory(), "visibility", m.getVisibility(), "createdAt", m.getCreatedAt(), "updatedAt",
                m.getUpdatedAt(), "photos", ps, "tags", m.getTags()));
    }

    private Map<String, Object> photoView(MemoryPhoto p) {
        String url = "/api/v1/memories/" + p.getMemoryId() + "/photos/" + p.getId();
        return Map.of("id", p.getId(), "memoryId", p.getMemoryId(), "url", url, "thumbnailUrl", url, "originalName",
                p.getOriginalName(), "size", p.getSize(), "contentType", p.getContentType(), "createdAt",
                p.getCreatedAt());
    }

    private Map<String, Object> page(Page<Memory> p) {
        return Map.of("data", p.getContent().stream().map(this::view).toList(), "page", p.getNumber(), "size",
                p.getSize(), "totalElements", p.getTotalElements(), "totalPages", p.getTotalPages());
    }

    private Sort sort(String s) {
        return switch (s) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "date");
            case "updated" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            case "title" -> Sort.by("title");
            default -> Sort.by(Sort.Direction.DESC, "date");
        };
    }

    private String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
