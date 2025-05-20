package com.twowheelhub.backend.controller;

import com.twowheelhub.backend.dto.AuthResponse;
import com.twowheelhub.backend.dto.LoginRequest;
import com.twowheelhub.backend.dto.RegisterRequest;
import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.TokenType;
import com.twowheelhub.backend.security.JwtService;
import com.twowheelhub.backend.service.TokenService;
import com.twowheelhub.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService, TokenService tokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("Successfully registered");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody @Valid LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        AppUser user = (AppUser) auth.getPrincipal();

        String accessToken = jwtService.generateToken(user, TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(user, TokenType.REFRESH);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, accessToken, TokenType.ACCESS);
        tokenService.saveUserToken(user, refreshToken, TokenType.REFRESH);

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout () {
        return ResponseEntity.ok("Successfully logged out");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken (HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(jwtService.refreshToken(request, response));
    }
}
