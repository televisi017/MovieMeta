package com.moviemeta.auth.services;

import com.moviemeta.auth.entities.User;
import com.moviemeta.auth.entities.UserRole;
import com.moviemeta.auth.repository.UserRepository;
import com.moviemeta.auth.utils.AuthResponse;
import com.moviemeta.auth.utils.LoginRequest;
import com.moviemeta.auth.utils.RegisterRequest;
import com.moviemeta.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest registerRequest) throws UserNotFoundException {

        //build a new user
        var user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .build();

        //save in db
        User savedUser = userRepository.save(user);

        //create access and refresh tokens for the user
        var accessToken = jwtService.generateToken(savedUser);
        var refreshToken = refreshTokenService.createRefreshToken(savedUser.getEmail());

        //return the auth response with the tokens to the user so he/she can use it
        return AuthResponse.builder()
                .accessToken(accessToken)
                .RefreshToken(refreshToken.getRefreshToken())
                .build();
    }

    public AuthResponse login(LoginRequest loginRequest) throws UserNotFoundException {

        //to authenticate the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        var user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new UserNotFoundException(loginRequest.getEmail()));

        //create new refresh and access tokens for the user that is logging in
        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(loginRequest.getEmail());

        //return the auth response with the tokens to the user so he/she can use it
        return AuthResponse.builder()
                .accessToken(accessToken)
                .RefreshToken(refreshToken.getRefreshToken())
                .build();
    }
}
