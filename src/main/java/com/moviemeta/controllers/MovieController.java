package com.moviemeta.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviemeta.dto.MovieDto;
import com.moviemeta.dto.MoviePageResponse;
import com.moviemeta.exceptions.FileRemoveException;
import com.moviemeta.exceptions.MovieNotFoundException;
import com.moviemeta.services.MovieService;
import com.moviemeta.utils.AppConstants;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService){
        this.movieService = movieService;
    }

    //serialising json to movie entity object.
    private MovieDto toMovieDtoObject(String movieDtoObj) throws JsonProcessingException {

        if(movieDtoObj == null) return null;

        MovieDto movieDto = new MovieDto();

        ObjectMapper objectMapper = new ObjectMapper();

        movieDto = objectMapper.readValue(movieDtoObj, MovieDto.class);

        return movieDto;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(
            @RequestPart MultipartFile file,
            @RequestPart String movieDto
            ) throws JsonProcessingException, FileUploadException {

            MovieDto movieDtoObject = toMovieDtoObject(movieDto);

            return new ResponseEntity<>(movieService.addMovie(movieDtoObject, file), HttpStatus.CREATED);
    }

    @GetMapping("/{MovieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer MovieId) throws MovieNotFoundException {
        return new ResponseEntity<>(movieService.getMovie(MovieId), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler(){
        return new ResponseEntity<>(movieService.getAllMovies(), HttpStatus.OK);
    }

    @PutMapping("/update/{MovieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer MovieId,
                                                       @RequestPart(required = false) String movieDto,
                                                       @RequestPart(required = false) MultipartFile file)
            throws MovieNotFoundException, JsonProcessingException, FileUploadException, FileRemoveException {
        MovieDto response = movieService.updateMovie(MovieId, toMovieDtoObject(movieDto), file);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/delete/{MovieId}")
    public ResponseEntity<String> deleteMovieHandler (@PathVariable Integer MovieId) throws MovieNotFoundException, FileNotFoundException, FileRemoveException {
        movieService.removeMovie(MovieId);
        return new ResponseEntity<>("Movie deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/allMoviesPage")
    public ResponseEntity<MoviePageResponse>  getMoviesWithPagination(
            @RequestParam(required = false, defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize
    ){
        return new ResponseEntity<>(movieService.getAllMoviesWithPagination(pageNumber,pageSize),HttpStatus.OK);
    }

    @GetMapping("/allMoviesPageSort")
    public ResponseEntity<MoviePageResponse>  getMoviesWithPaginationAndSort(
            @RequestParam(required = false, defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(required = false, defaultValue = AppConstants.SORT_BY) String sortBy,
            @RequestParam(required = false, defaultValue = AppConstants.DIR) String dir
    ){
        return new ResponseEntity<>(movieService.getAllMoviesWithPaginationAndSorting(pageNumber,pageSize,sortBy,dir)
                ,HttpStatus.OK);
    }
}
