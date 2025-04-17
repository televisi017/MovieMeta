package com.moviemeta.services;
import com.moviemeta.exceptions.FileUploadException;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public interface FileService {

    // returns uploaded file name
    String uploadFile(String path, MultipartFile file) throws IOException, FileUploadException;

    FileInputStream getResourceFile(String path, String fileName) throws FileNotFoundException;
}
