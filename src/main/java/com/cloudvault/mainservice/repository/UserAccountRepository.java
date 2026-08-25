package com.cloudvault.mainservice.repository;

import com.cloudvault.mainservice.entity.UserAccount;
import com.cloudvault.mainservice.entity.UserStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByIdAndStatusNot(Long id, UserStatus status);

    Optional<UserAccount> findByUsernameIgnoreCase(String username);
}
