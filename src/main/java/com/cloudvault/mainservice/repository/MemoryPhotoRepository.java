package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.MemoryPhoto;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryPhotoRepository extends JpaRepository<MemoryPhoto, UUID> {
    List<MemoryPhoto> findByMemoryIdOrderByCreatedAtAsc(UUID memoryId);
    Optional<MemoryPhoto> findByIdAndMemoryId(UUID id, UUID memoryId);
    long countByMemoryIdIn(Collection<UUID> memoryIds);
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.size),0) from MemoryPhoto p where p.memoryId in (select m.id from Memory m where m.userId=:userId and m.deletedAt is null)")
    long totalSizeForUser(Long userId);
}
