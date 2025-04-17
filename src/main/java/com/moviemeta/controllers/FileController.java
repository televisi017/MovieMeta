package com.moviemeta.controllers;

import com.moviemeta.exceptions.EmptyFileException;
import com.moviemeta.exceptions.FileUploadException;
import com.moviemeta.exceptions.HttpResponseWriteException;
import com.moviemeta.services.FileService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Value("${project.poster}")
    private String path;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadResource(@RequestParam("image") MultipartFile file)
            throws FileUploadException, EmptyFileException {
        try{
            if(!file.isEmpty())fileService.uploadFile(path, file);
            else throw new EmptyFileException("Received file was empty");
        }
        catch (IOException e){
            throw new FileUploadException();
        }
        return new ResponseEntity<>("File uploaded successfully.", HttpStatus.OK);
    }

    @GetMapping("/download/{fileName}")
    public void downloadResource(@PathVariable String fileName, HttpServletResponse res)
            throws FileNotFoundException, HttpResponseWriteException {
        FileInputStream fis = fileService.getResourceFile(path, fileName);
        try{
            ServletOutputStream fos = res.getOutputStream();

            res.setContentType(MediaType.ALL_VALUE);

            byte [] bytes = new byte[1000];

            while(fis.read(bytes) != -1){
                fos.write(bytes);
            }

            res.setStatus(200);
        } catch (IOException e) {
            throw new HttpResponseWriteException("Failed to write data to HTTP/S response");
        }
    }
}
