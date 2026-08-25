package com.cloudvault.mainservice.service.impl;

import com.cloudvault.mainservice.entity.UserAccount;
import com.cloudvault.mainservice.entity.UserStatus;
import com.cloudvault.mainservice.exception.ResourceNotFoundException;
import com.cloudvault.mainservice.repository.UserAccountRepository;
import com.cloudvault.mainservice.repository.MemoryRepository;
import com.cloudvault.mainservice.security.CurrentUserService;
import com.cloudvault.mainservice.service.AccountService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserAccountRepository userAccountRepository;
    private final CurrentUserService currentUserService;
    private final MemoryRepository memoryRepository;

    @Override
    @Transactional
    public void softDeleteCurrentAccount() {
        Long userId = currentUserService.getCurrentUserId();

        UserAccount user = userAccountRepository.findByIdAndStatusNot(userId, UserStatus.DELETED)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (user.getStatus() == UserStatus.DELETED) {
            return;
        }

        user.setStatus(UserStatus.DELETED);
        user.setActive(false);
        user.setDeletedAt(Instant.now());
        memoryRepository.softDeleteAllByUserId(userId);
    }
}
