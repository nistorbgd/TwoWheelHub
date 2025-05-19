package com.twowheelhub.backend.repository;

import com.twowheelhub.backend.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUsername(String username);
}
