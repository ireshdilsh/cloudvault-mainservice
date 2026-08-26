package com.cloudvault.mainservice.controller;

import com.cloudvault.mainservice.entity.UserAccount;
import com.cloudvault.mainservice.repository.UserAccountRepository;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserAccountRepository users;

    @Value("${app.internal-service-key}")
    private String internalServiceKey;

    @PostMapping
    public ResponseEntity<Void> create(@RequestHeader("X-Internal-Service-Key") String key,
            @RequestBody Map<String, Object> request) {
        if (!internalServiceKey.equals(key)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        Long id = Long.valueOf(request.get("id").toString());
        String email = request.get("email").toString().trim().toLowerCase(Locale.ROOT);
        users.findById(id).orElseGet(() -> {
            UserAccount user = new UserAccount();
            user.setId(id);
            user.setEmail(email);
            String base = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_.-]", "-");
            // user.setName(base.isBlank() ? "CloudVault user" : base);
            // user.setUsername(uniqueUsername(base, id));
            return users.save(user);
        });
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private String uniqueUsername(String base, Long id) {
        String candidate = base.isBlank() ? "user" : base;
        // return users.findByUsernameIgnoreCase(candidate).isEmpty() ? candidate : candidate + "-" + id;
        return candidate;
    }
}
