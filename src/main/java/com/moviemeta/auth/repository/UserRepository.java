package com.moviemeta.auth.repository;

import com.moviemeta.auth.entities.RefreshToken;
import com.moviemeta.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String username);

    Optional<User> findByRefreshToken(RefreshToken refreshToken);
}
