package com.subscriptiq.user.service;

import com.subscriptiq.common.exception.BadRequestException;
import com.subscriptiq.common.exception.DuplicateResourceException;
import com.subscriptiq.user.dto.CreateStaffRequest;
import com.subscriptiq.user.dto.UserResponse;
import com.subscriptiq.user.entity.User;
import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import com.subscriptiq.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should create staff user with STAFF role")
    void testCreateStaff_Success() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .name("Staff Member")
                .email("staff@subscriptiq.com")
                .password("StaffSecret@123")
                .build();

        when(userRepository.existsByEmail("staff@subscriptiq.com")).thenReturn(false);
        when(passwordEncoder.encode("StaffSecret@123")).thenReturn("$2a$10$encodedStaffPass");

        User savedUser = User.builder()
                .id(5L)
                .name("Staff Member")
                .email("staff@subscriptiq.com")
                .password("$2a$10$encodedStaffPass")
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createStaff(request);

        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(UserRole.STAFF);
        assertThat(response.getEmail()).isEqualTo("staff@subscriptiq.com");
    }

    @Test
    @DisplayName("Should prevent creating staff with duplicate email")
    void testCreateStaff_DuplicateEmail() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .name("Staff Member")
                .email("staff@subscriptiq.com")
                .password("StaffSecret@123")
                .build();

        when(userRepository.existsByEmail("staff@subscriptiq.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createStaff(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should prevent deactivating the last active admin")
    void testPreventDeactivatingLastAdmin() {
        User adminUser = User.builder()
                .id(1L)
                .name("Super Admin")
                .email("admin@subscriptiq.com")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndStatus(UserRole.ADMIN, UserStatus.ACTIVE)).thenReturn(1L);

        assertThatThrownBy(() -> userService.updateUserStatus(1L, UserStatus.INACTIVE))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot deactivate the last remaining active administrator");
    }

    @Test
    @DisplayName("Should allow updating status when conditions are met")
    void testUpdateStatus_Success() {
        User customer = User.builder()
                .id(2L)
                .email("customer@example.com")
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(customer));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserStatus(2L, UserStatus.INACTIVE);

        assertThat(response.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(customer);
    }
}

