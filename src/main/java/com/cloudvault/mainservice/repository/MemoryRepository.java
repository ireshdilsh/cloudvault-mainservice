package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.Memory;
import java.time.LocalDate;
import java.util.*;

import com.cloudvault.mainservice.entity.MemoryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {
//  Optional<Memory> findByIdAndUserIdAndDeletedAtIsNull(UUID id, Long userId);
//
//  long countByUserIdAndDeletedAtIsNull(Long userId);
//
////  @Query("select m from Memory m where m.userId=:userId and m.deletedAt is null and " +
////      "(:search is null or lower(m.title) like lower(concat('%',:search,'%')) or lower(coalesce(m.description,'')) like lower(concat('%',:search,'%')) or lower(coalesce(m.location,'')) like lower(concat('%',:search,'%')) or lower(coalesce(m.place.name,'')) like lower(concat('%',:search,'%'))) and "
////      +
////      "(:from is null or m.date >= :from) and (:to is null or m.date <= :to) and (:place is null or lower(coalesce(m.place.name,'')) like lower(concat('%',:place,'%'))) and (:category is null or m.category = :category)")
////  Page<Memory> findVisible(Long userId, String search, LocalDate from, LocalDate to, String place,
////      com.cloudvault.mainservice.entity.MemoryCategory category, Pageable pageable);
//
//  @Query("""
//    SELECT m
//    FROM Memory m
//    LEFT JOIN m.place p
//    WHERE m.userId = :userId
//      AND m.deletedAt IS NULL
//
//      AND (
//          :search IS NULL
//          OR m.title ILIKE CONCAT('%', :search, '%')
//          OR COALESCE(m.description, '') ILIKE CONCAT('%', :search, '%')
//          OR COALESCE(m.location, '') ILIKE CONCAT('%', :search, '%')
//          OR COALESCE(p.name, '') ILIKE CONCAT('%', :search, '%')
//      )
//
//      AND (
//          :from IS NULL
//          OR m.date >= :from
//      )
//
//      AND (
//          :to IS NULL
//          OR m.date <= :to
//      )
//
//      AND (
//          :place IS NULL
//          OR COALESCE(p.name, '') ILIKE CONCAT('%', :place, '%')
//      )
//
//      AND (
//          :category IS NULL
//          OR m.category = :category
//      )
//    """)
//  Page<Memory> findVisible(
//          @Param("userId") Long userId,
//          @Param("search") String search,
//          @Param("from") LocalDate from,
//          @Param("to") LocalDate to,
//          @Param("place") String place,
//          @Param("category") MemoryCategory category,
//          Pageable pageable
//  );
//
//  Page<Memory> findByUserIdAndPlaceIdAndDeletedAtIsNull(Long userId, UUID placeId, Pageable pageable);
//
//  List<Memory> findByUserIdAndDeletedAtIsNullOrderByDateDesc(Long userId);
//
//  List<Memory> findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);
//
//  @Modifying
//  @Query("update Memory m set m.deletedAt = CURRENT_TIMESTAMP where m.userId = :userId and m.deletedAt is null")
//  int softDeleteAllByUserId(@Param("userId") Long userId);

// ============================================================
// FIND ONE MEMORY
// ============================================================

Optional<Memory> findByIdAndUserIdAndDeletedAtIsNull(
        UUID id,
        Long userId
);


  // ============================================================
  // COUNT
  // ============================================================

  long countByUserIdAndDeletedAtIsNull(
          Long userId
  );


  // ============================================================
  // FIND MEMORIES
  // ============================================================

  @Query("""
        SELECT m
        FROM Memory m
        LEFT JOIN m.place p
        WHERE m.userId = :userId
          AND m.deletedAt IS NULL

          AND (
              :search IS NULL
              OR :search = ''
              OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(m.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(m.location, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))
          )

          AND (
              :fromDate IS NULL
              OR m.date >= :fromDate
          )

          AND (
              :toDate IS NULL
              OR m.date <= :toDate
          )

          AND (
              :placeSearch IS NULL
              OR :placeSearch = ''
              OR LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', :placeSearch, '%'))
          )

          AND (
              :category IS NULL
              OR m.category = :category
          )
        """)
  Page<Memory> findVisible(
          @Param("userId") Long userId,
          @Param("search") String search,
          @Param("fromDate") LocalDate fromDate,
          @Param("toDate") LocalDate toDate,
          @Param("placeSearch") String placeSearch,
          @Param("category") MemoryCategory category,
          Pageable pageable
  );


  // ============================================================
  // FIND BY PLACE
  // ============================================================

  Page<Memory> findByUserIdAndPlaceIdAndDeletedAtIsNull(
          Long userId,
          UUID placeId,
          Pageable pageable
  );


  // ============================================================
  // ALL MEMORIES
  // ============================================================

  List<Memory> findByUserIdAndDeletedAtIsNullOrderByDateDesc(
          Long userId
  );


  // ============================================================
  // RECENT MEMORIES
  // ============================================================

  List<Memory> findTop10ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
          Long userId
  );


  // ============================================================
  // DELETE ALL USER MEMORIES
  // ============================================================

  @Modifying
  @Query("""
        UPDATE Memory m
        SET m.deletedAt = CURRENT_TIMESTAMP
        WHERE m.userId = :userId
          AND m.deletedAt IS NULL
        """)
  int softDeleteAllByUserId(
          @Param("userId") Long userId
  );
}
