package com.moviemeta.services;
import com.moviemeta.exceptions.FileUploadException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileServiceImplementation implements FileService{

    @Override
    public String uploadFile(String path, MultipartFile file) throws FileUploadException {

        //get file name
        String fileName = file.getOriginalFilename();

        //set path
        String filePath = path + File.separator + fileName;

        //create file object
        File f = new File(path);

        if(!f.exists()){
            boolean var =  f.mkdir();
        }

        //copy the given file to the path

        try{
            Files.copy(file.getInputStream(), Paths.get(filePath));
        }

        catch (IOException e){
            throw new FileUploadException();
        }

        return fileName;
    }

    @Override
    public FileInputStream getResourceFile(String path, String fileName) throws FileNotFoundException {
        String filePath = path + File.separator + fileName;
        File f = new File(filePath);
        if(!f.exists() || !f.isFile()) throw new FileNotFoundException("file not found");
        return new FileInputStream(filePath);
    }
}
