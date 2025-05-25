package com.twowheelhub.backend.service.impl;

import com.twowheelhub.backend.dto.LoginRequest;
import com.twowheelhub.backend.dto.RegisterRequest;
import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.AuthProvider;
import com.twowheelhub.backend.entity.Role;
import com.twowheelhub.backend.exception.InvalidCredentialsException;
import com.twowheelhub.backend.exception.InvalidRoleException;
import com.twowheelhub.backend.exception.UserAlreadyExistsException;
import com.twowheelhub.backend.repository.AppUserRepository;
import com.twowheelhub.backend.repository.RoleRepository;
import com.twowheelhub.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            log.debug("Email: {} already exists!", request.getEmail());
            throw new UserAlreadyExistsException("Email already in use!");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new InvalidRoleException("Default role not found!"));

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .provider(AuthProvider.CLASSIC)
                .role(userRole)
                .build();

        log.debug("Registering user: {}", user);
        userRepository.save(user);
    }

    @Override
    public void login(LoginRequest request) {

        AppUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Wrong email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Wrong email or password");
        }
    }
}