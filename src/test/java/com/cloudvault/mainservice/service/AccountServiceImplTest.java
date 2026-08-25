package com.cloudvault.mainservice.service;

import com.cloudvault.mainservice.entity.UserAccount;
import com.cloudvault.mainservice.entity.UserStatus;
import com.cloudvault.mainservice.exception.ResourceNotFoundException;
import com.cloudvault.mainservice.repository.UserAccountRepository;
import com.cloudvault.mainservice.security.CurrentUserService;
import com.cloudvault.mainservice.service.impl.AccountServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void softDeleteCurrentAccount_marksAsDeletedWithoutRemovingRecord() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setStatus(UserStatus.ACTIVE);
        user.setActive(true);

        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(userAccountRepository.findByIdAndStatusNot(1L, UserStatus.DELETED)).thenReturn(Optional.of(user));

        accountService.softDeleteCurrentAccount();

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getDeletedAt()).isNotNull();
        verify(userAccountRepository).findByIdAndStatusNot(1L, UserStatus.DELETED);
    }

    @Test
    void softDeleteCurrentAccount_throwsWhenAccountNotFound() {
        when(currentUserService.getCurrentUserId()).thenReturn(99L);
        when(userAccountRepository.findByIdAndStatusNot(99L, UserStatus.DELETED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.softDeleteCurrentAccount())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Account not found");
    }
}
