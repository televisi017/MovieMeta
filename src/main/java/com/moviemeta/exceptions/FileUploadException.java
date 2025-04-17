package com.moviemeta.exceptions;

import java.io.IOException;

public class FileUploadException extends IOException {
    public FileUploadException(){
        super("Problem in uploading file");
    }
}
