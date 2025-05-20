package com.twowheelhub.backend.service.impl;

import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.Token;
import com.twowheelhub.backend.entity.TokenType;
import com.twowheelhub.backend.repository.TokenRepository;
import com.twowheelhub.backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    @Override
    public void saveUserToken(AppUser user, String jwtToken, TokenType tokenType) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(tokenType)
                .isExpired(false)
                .isRevoked(false)
                .build();

        tokenRepository.save(token);
    }

    @Override
    public void revokeAllUserTokens(AppUser user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user);
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }
}
