package com.moviemeta.exceptions;

public class RefreshTokenExpiredException extends Exception{
    public RefreshTokenExpiredException(){
        super("Provided refresh token has been expired");
    }
}
