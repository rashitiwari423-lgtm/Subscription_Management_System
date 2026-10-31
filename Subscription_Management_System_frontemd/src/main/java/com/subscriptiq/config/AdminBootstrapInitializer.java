package com.subscriptiq.config;

import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.bootstrap.initial-admin.name:}")
    private String adminName;

    @Value("${application.bootstrap.initial-admin.email:}")
    private String adminEmail;

    @Value("${application.bootstrap.initial-admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            log.info("Bootstrap: Admin account already exists. Skipping initial admin creation.");
            return;
        }

        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.info("Bootstrap: No initial admin credentials configured (INITIAL_ADMIN_EMAIL / INITIAL_ADMIN_PASSWORD). Skipping.");
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Bootstrap: User with email [{}] exists but is not an ADMIN. Skipping.", normalizedEmail);
            return;
        }

        String name = StringUtils.hasText(adminName) ? adminName.trim() : "System Administrator";

        User adminUser = User.builder()
                .name(name)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(adminUser);
        log.info("Bootstrap: Successfully initialized default ADMIN user with email [{}]", normalizedEmail);
    }
}

