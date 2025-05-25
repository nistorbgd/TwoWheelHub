package com.twowheelhub.backend.repository;

import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.Token;
import com.twowheelhub.backend.entity.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllByUser(AppUser user);
    Optional<Token> findByToken(String token);

    @Query("""
        SELECT t FROM Token t
        WHERE t.user = :user AND t.isExpired = false AND t.isRevoked = false
    """)
    List<Token> findAllValidTokenByUser(@Param("user") AppUser user);

    Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);


}
