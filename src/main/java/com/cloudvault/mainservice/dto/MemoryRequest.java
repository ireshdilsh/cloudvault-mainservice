package com.cloudvault.mainservice.dto;

import com.cloudvault.mainservice.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

public record MemoryRequest(@NotBlank @Size(max = 160) String title, @Size(max = 4000) String description,
        @NotNull LocalDate date, @Size(max = 200) String location,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude, MemoryCategory category,
        Set<@Size(max = 60) String> tags, Visibility visibility) {
}
