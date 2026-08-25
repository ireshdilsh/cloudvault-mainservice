package com.cloudvault.mainservice.dto;
import jakarta.validation.constraints.*;
public record ProfileRequest(@NotBlank @Size(max=100) String name, @NotBlank @Pattern(regexp="^[A-Za-z0-9_.-]{3,100}$") String username) {}
