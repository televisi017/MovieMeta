package com.moviemeta.exceptions;

public class RefreshTokenNotFound extends Exception{
    public RefreshTokenNotFound(){
        super("Provided refresh token doesn't exist in database");
    }
}
