package com.twowheelhub.backend.controller;

import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.AuthProvider;
import com.twowheelhub.backend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AppUserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<AppUser> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }
}
