package com.twowheelhub.backend.service.impl;

import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.AuthProvider;
import com.twowheelhub.backend.entity.Role;
import com.twowheelhub.backend.entity.TokenType;
import com.twowheelhub.backend.exception.InvalidRoleException;
import com.twowheelhub.backend.repository.AppUserRepository;
import com.twowheelhub.backend.repository.RoleRepository;
import com.twowheelhub.backend.security.JwtService;
import com.twowheelhub.backend.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandlerImpl implements AuthenticationSuccessHandler {
    private final AppUserRepository userRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        AuthProvider provider = switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "facebook" -> AuthProvider.FACEBOOK;
            default -> AuthProvider.CLASSIC;
        };

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new InvalidRoleException("Default role not found!"));

        AppUser user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setEmail(email);
                    newUser.setUsername(oAuth2User.getAttribute("name"));
                    newUser.setProvider(provider);
                    newUser.setRole(userRole);
                    return userRepository.save(newUser);
                });

        String accessToken = jwtService.generateToken(user, TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(user, TokenType.REFRESH);
        tokenService.saveUserToken(user, accessToken, TokenType.ACCESS);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = """
                {
                    "access_token": "%s",
                    "refresh_token": "%s",
                }
                """.formatted(accessToken, refreshToken);
        response.getWriter().write(json);
    }


}
