package com.subscriptiq.user.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.common.exception.ResourceNotFoundException;
import com.subscriptiq.user.dto.CreateStaffRequest;
import com.subscriptiq.user.dto.UserResponse;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createStaff(CreateStaffRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("User with email '" + normalizedEmail + "' already exists");
        }

        User staffUser = User.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(staffUser);
        log.info("Admin created new STAFF user with id [{}] and email [{}]", savedUser.getId(), savedUser.getEmail());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, UserStatus newStatus) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Safety rule: Prevent deactivating the last remaining active admin
        if (user.getRole() == UserRole.ADMIN && newStatus == UserStatus.INACTIVE) {
            long activeAdminCount = userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE);
            if (activeAdminCount <= 1 && user.getStatus() == UserStatus.ACTIVE) {
                throw new BadRequestException("Operation aborted: Cannot deactivate the last remaining active administrator account.");
            }
        }

        user.setStatus(newStatus);
        User updated = userRepository.save(user);
        log.info("User [{}] status changed to [{}]", updated.getEmail(), newStatus);

        return UserResponse.fromEntity(updated);
    }
}

