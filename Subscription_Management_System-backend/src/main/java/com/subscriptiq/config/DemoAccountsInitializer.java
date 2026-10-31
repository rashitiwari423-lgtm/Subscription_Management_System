package com.subscriptiq.config;

import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "application.bootstrap.demo-users.enabled", havingValue = "true")
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class DemoAccountsInitializer implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Password@123";

    private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
            new DemoAccount("Platform Administrator", "admin@subscriptiq.com", UserRole.ADMIN),
            new DemoAccount("Operations & Billing Executive", "staff@subscriptiq.com", UserRole.STAFF),
            new DemoAccount("Client Portal User", "customer@subscriptiq.com", UserRole.CUSTOMER)
    );

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (DemoAccount account : DEMO_ACCOUNTS) {
            User user = userRepository.findByEmail(account.email()).orElseGet(() -> User.builder()
                    .name(account.name())
                    .email(account.email())
                    .role(account.role())
                    .build());

            if (user.getRole() != account.role()) {
                throw new IllegalStateException(
                        "Demo account email is already assigned to a different role: " + account.email());
            }

            user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        log.info("Development demo accounts are ready for ADMIN, STAFF, and CUSTOMER login.");
    }

    private record DemoAccount(String name, String email, UserRole role) {
    }
}
