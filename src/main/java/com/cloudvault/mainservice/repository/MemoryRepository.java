package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.Memory;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {
  Optional<Memory> findByIdAndUserIdAndDeletedAtIsNull(UUID id, Long userId);

  long countByUserIdAndDeletedAtIsNull(Long userId);

  @Query("select m from Memory m where m.userId=:userId and m.deletedAt is null and " +
      "(:search is null or lower(m.title) like lower(concat('%',:search,'%')) or lower(coalesce(m.description,'')) like lower(concat('%',:search,'%')) or lower(coalesce(m.location,'')) like lower(concat('%',:search,'%')) or lower(coalesce(m.place.name,'')) like lower(concat('%',:search,'%'))) and "
      +
      "(:from is null or m.date >= :from) and (:to is null or m.date <= :to) and (:place is null or lower(coalesce(m.place.name,'')) like lower(concat('%',:place,'%'))) and (:category is null or m.category = :category)")
  Page<Memory> findVisible(Long userId, String search, LocalDate from, LocalDate to, String place,
      com.cloudvault.mainservice.entity.MemoryCategory category, Pageable pageable);

  Page<Memory> findByUserIdAndPlaceIdAndDeletedAtIsNull(Long userId, UUID placeId, Pageable pageable);

  List<Memory> findByUserIdAndDeletedAtIsNullOrderByDateDesc(Long userId);

  List<Memory> findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

  @Modifying
  @Query("update Memory m set m.deletedAt = CURRENT_TIMESTAMP where m.userId = :userId and m.deletedAt is null")
  int softDeleteAllByUserId(@Param("userId") Long userId);
}
