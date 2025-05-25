package com.twowheelhub.backend.service;

import com.twowheelhub.backend.dto.LoginRequest;
import com.twowheelhub.backend.dto.RegisterRequest;

public interface UserService {

    void register(RegisterRequest request);

    void login(LoginRequest request);
}
