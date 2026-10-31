package com.subscriptiq.user.service;

import com.subscriptiq.user.dto.CreateStaffRequest;
import com.subscriptiq.user.dto.UserResponse;
import com.subscriptiq.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createStaff(CreateStaffRequest request);

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse updateUserStatus(Long id, UserStatus status);
}

