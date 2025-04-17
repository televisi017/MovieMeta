package com.moviemeta.auth.repository;

import com.moviemeta.auth.entities.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword,Integer> {
}
