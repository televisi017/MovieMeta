package com.moviemeta.exceptions;

public class FileRemoveException extends Exception{
    public FileRemoveException(String typeF){
        super("failed to delete file");
    }
}
