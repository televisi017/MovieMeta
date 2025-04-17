package com.moviemeta.exceptions;

public class MovieNotFoundException extends Exception{
    public MovieNotFoundException(Integer MovieId){
        super("Movie with id: "+MovieId.toString()+ " not found");
    }
}
