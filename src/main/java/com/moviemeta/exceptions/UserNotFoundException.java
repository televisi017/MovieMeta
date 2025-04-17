package com.moviemeta.exceptions;

public class UserNotFoundException extends Exception{
    public UserNotFoundException(String username){
        super("User not found : " + username);
    }
}
