package com.cloudvault.mainservice.controller;

import com.cloudvault.mainservice.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Soft delete current account",
            description = "Marks the authenticated user's account as DELETED and sets deletedAt without removing the row.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {
        accountService.softDeleteCurrentAccount();
        return ResponseEntity.noContent().build();
    }
}
