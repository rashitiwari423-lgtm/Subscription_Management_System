package com.subscriptiq.auth.service;

import com.subscriptiq.auth.dto.ForgotPasswordRequest;
import com.subscriptiq.auth.dto.LoginRequest;
import com.subscriptiq.auth.dto.LoginResponse;
import com.subscriptiq.auth.dto.RegisterRequest;
import com.subscriptiq.auth.dto.ResetPasswordRequest;
import com.subscriptiq.user.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

