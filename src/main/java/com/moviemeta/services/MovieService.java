package com.moviemeta.services;

import com.moviemeta.dto.MovieDto;
import com.moviemeta.dto.MoviePageResponse;
import com.moviemeta.exceptions.FileRemoveException;
import com.moviemeta.exceptions.MovieNotFoundException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;

public interface MovieService {

    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws FileUploadException;

    public MovieDto getMovie(Integer MovieId) throws MovieNotFoundException;

    public List<MovieDto> getAllMovies();

    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize);

    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir);

    public MovieDto updateMovie(Integer MovieId, MovieDto movieDto, MultipartFile file) throws MovieNotFoundException, FileUploadException, FileRemoveException;

    public void removeMovie(Integer movieId) throws MovieNotFoundException, FileNotFoundException, FileRemoveException;
}
