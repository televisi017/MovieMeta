package com.moviemeta.auth.services;

import com.moviemeta.auth.entities.RefreshToken;
import com.moviemeta.auth.entities.User;
import com.moviemeta.auth.repository.RefreshTokenRepository;
import com.moviemeta.auth.repository.UserRepository;
import com.moviemeta.exceptions.RefreshTokenExpiredException;
import com.moviemeta.exceptions.RefreshTokenNotFound;
import com.moviemeta.exceptions.UserNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @PersistenceContext
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    public RefreshToken createRefreshToken(String username) throws UserNotFoundException{

        //check if user exists
        User user = userRepository.findByEmail(username)
                                .orElseThrow(()->new UserNotFoundException(username));

        //if yes then check if it has refresh token (return the existing refresh token)
        RefreshToken refreshToken = user.getRefreshToken();

        //if no refToken then generate one RefreshToken Entity
        if(refreshToken == null || refreshToken.getExpirationTime().compareTo(Instant.now()) < 0){

            //delete expired refresh token in the db
            user.setRefreshToken(null);
            User savedUser = userRepository.save(user);


            // make a new refresh token
            long refreshTokenValidity = 180*1000; // 3 min for dev purposes

            refreshToken = RefreshToken.builder()
                                    .refreshToken(UUID.randomUUID().toString())
                                    .expirationTime(Instant.now().plusMillis(refreshTokenValidity))
                                    .user(user)
                                    .build();

            RefreshToken savedRef = refreshTokenRepository.save(refreshToken);

            return savedRef;
        }
        //return the refToken
        return refreshToken;
    }

    @Transactional
    public RefreshToken verifyRefreshToken(String refreshToken) throws RefreshTokenNotFound, RefreshTokenExpiredException, UserNotFoundException {

        //check if refresh token exists in db, if not throw exception
        RefreshToken refToken = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()->new RefreshTokenNotFound());

        User user = userRepository.findByRefreshToken(refToken).orElseThrow(()->new UserNotFoundException(""));

        //check if refresh token is expired, if so delete and then throw exception
        if(refToken.getExpirationTime().compareTo(Instant.now()) < 0){
            user.setRefreshToken(null);
            User savedUser = userRepository.save(user);
            throw new RefreshTokenExpiredException();
        }

        //if all good return refresh token back
        return refToken;
    }


}
