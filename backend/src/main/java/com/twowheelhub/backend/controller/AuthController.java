package com.twowheelhub.backend.controller;

import com.twowheelhub.backend.dto.AuthResponse;
import com.twowheelhub.backend.dto.LoginRequest;
import com.twowheelhub.backend.dto.RegisterRequest;
import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.security.JwtService;
import com.twowheelhub.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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

        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken((AppUser) user);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
