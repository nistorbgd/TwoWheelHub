package com.twowheelhub.backend.service;

import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.TokenType;

public interface TokenService {
    void saveUserToken(AppUser user, String jwtToken, TokenType tokenType);
    void revokeAllUserTokens(AppUser user);
}
