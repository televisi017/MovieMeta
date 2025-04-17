package com.moviemeta.controllers;

import com.moviemeta.auth.entities.RefreshToken;
import com.moviemeta.auth.entities.User;
import com.moviemeta.auth.services.AuthService;
import com.moviemeta.auth.services.JwtService;
import com.moviemeta.auth.services.RefreshTokenService;
import com.moviemeta.auth.utils.AuthResponse;
import com.moviemeta.auth.utils.LoginRequest;
import com.moviemeta.auth.utils.RefreshTokenRequest;
import com.moviemeta.auth.utils.RegisterRequest;
import com.moviemeta.exceptions.RefreshTokenExpiredException;
import com.moviemeta.exceptions.RefreshTokenNotFound;
import com.moviemeta.exceptions.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService, JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) throws UserNotFoundException {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) throws UserNotFoundException {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws RefreshTokenExpiredException, RefreshTokenNotFound, UserNotFoundException {

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());

        User user = refreshToken.getUser();

        String accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .RefreshToken(refreshToken.getRefreshToken())
                .accessToken(accessToken)
                .build());
    }
}
