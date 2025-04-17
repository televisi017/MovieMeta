package com.moviemeta.services;

import com.moviemeta.dto.MovieDto;
import com.moviemeta.dto.MoviePageResponse;
import com.moviemeta.entities.Movie;
import com.moviemeta.exceptions.FileRemoveException;
import com.moviemeta.exceptions.MovieNotFoundException;
import com.moviemeta.repository.MovieRepository;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class MovieServiceImplementation implements MovieService{

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;
    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImplementation(MovieRepository movieRepository, FileService fileService){
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    //manual mappers
    public MovieDto movieToMovieDto(Movie movie, String posterUrl){

        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }

    public Movie movieDtoToMovie(MovieDto movieDto){

        return new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws FileUploadException {

        String posterName = new String();

        try{

            // 1. upload the file (and get the file name)
            if(!file.isEmpty()) posterName = fileService.uploadFile(path, file);

            // 2. set the value of field 'poster' as filename.
            movieDto.setPoster(posterName);
        }
        catch (IOException e){
            throw new FileUploadException("poster");
        }

        // 3. map DTO to movie entity object
        Movie movie = movieDtoToMovie(movieDto);

        // 4. save movie object using repository.
        Movie savedMovie = movieRepository.save(movie);

        // 5. generate posterUrl
        String posterUrl = baseUrl +"/file/download/"+ posterName;

        // 6. map saved movie object to dto and return it
        MovieDto response = movieToMovieDto(movie, posterUrl);

        return response;
    }

    @Override
    public MovieDto getMovie(Integer MovieId) throws MovieNotFoundException{
        return movieRepository.findById(MovieId).map((m)->{
            String posterUrl = baseUrl +"/file/download/"+ m.getPoster();
            return movieToMovieDto(m, posterUrl);
        }).orElseThrow(()->new MovieNotFoundException(MovieId));
    }

    @Override
    public List<MovieDto> getAllMovies() {
        return movieRepository.findAll().stream().map((m)->{
            String posterUrl = baseUrl +"/file/download/"+ m.getPoster();
            return movieToMovieDto(m, posterUrl);
        }).collect(Collectors.toList());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {

        //page request
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize); //page number <pageNumber>, <pageSize> items per page

        //This query fetches a single "page" of Movie entities from the database based on the pageRequest object
        Page<Movie> moviePage = movieRepository.findAll(pageRequest);

        //list of all movies in the page
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = movies.stream().map((m)->{
            String posterUrl = baseUrl + "/file/download/" + m.getPoster();
            return movieToMovieDto(m, posterUrl);
        }).collect(Collectors.toList());

        return new MoviePageResponse(
                movieDtos,
                pageNumber,
                moviePage.getNumberOfElements(),
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {

        Sort sort = dir.equalsIgnoreCase("asc")? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        //page request
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort); //page number <pageNumber>, <pageSize> items per page

        //This query fetches a single "page" of Movie entities from the database based on the pageRequest object
        Page<Movie> moviePage = movieRepository.findAll(pageRequest);

        //list of all movies in the page
        List<Movie> movies = moviePage.getContent();

        List<MovieDto> movieDtos = movies.stream().map((m)->{
            String posterUrl = baseUrl + "/file/download/" + m.getPoster();
            return movieToMovieDto(m, posterUrl);
        }).collect(Collectors.toList());

        return new MoviePageResponse(
                movieDtos,
                pageNumber,
                moviePage.getNumberOfElements(),
                moviePage.getTotalElements(),
                moviePage.getTotalPages(),
                moviePage.isLast()
        );
    }

    @Override
    public MovieDto updateMovie(Integer MovieId, MovieDto movieDto, MultipartFile file) throws MovieNotFoundException, FileUploadException, FileRemoveException {

        // 1. check if movie exists
        if(movieRepository.existsById(MovieId)){

            Movie oldMovie = movieRepository.findById(MovieId).orElseThrow(()->new MovieNotFoundException(MovieId));
            Movie newMovie;

            if(movieDto != null){
                newMovie = movieDtoToMovie(movieDto);
                newMovie.setMovieId(MovieId);
                if(newMovie.getTitle() == null) newMovie.setTitle(oldMovie.getTitle());
                if(newMovie.getDirector() == null) newMovie.setDirector(oldMovie.getDirector());
                if(newMovie.getStudio() == null) newMovie.setStudio(oldMovie.getStudio());
                if(newMovie.getMovieCast() == null) newMovie.setMovieCast(oldMovie.getMovieCast());
                if(newMovie.getReleaseYear() == null) newMovie.setReleaseYear(oldMovie.getReleaseYear());
            }
            else newMovie = oldMovie;

            // 2. check if user has provided a new poster file (file->null do nothing) (else delete the existing file)
            if(file != null && !file.isEmpty()){

                // 3. check if the file with the given name exists already

                File f = new File(path + oldMovie.getPoster());
                System.out.println(path + file.getOriginalFilename());

                // 4. if the given file already exists then it must be deleted
                if(f.exists()){
                    if(f.delete()){
                        System.out.println("old poster file deleted successfully");
                    } else throw new FileRemoveException("poster");
                }

                // 5. upload the file given by the user.

                try {
                    String fileName = fileService.uploadFile(path,file);
                    newMovie.setPoster(fileName);
                }
                catch (IOException e){
                    throw new FileUploadException("poster");
                }
            }

            else if(movieDto != null){
                newMovie.setPoster(oldMovie.getPoster());
            }

            Movie savedMovie = movieRepository.save(newMovie);

            String posterUrl = baseUrl + "/file/download/" + savedMovie.getPoster();

            return movieToMovieDto(savedMovie, posterUrl);
        }
        else throw new MovieNotFoundException(MovieId);
    }

    @Override
    public void removeMovie(Integer MovieId) throws MovieNotFoundException, FileNotFoundException, FileRemoveException {

        if(movieRepository.existsById(MovieId)){

            Movie movie = movieRepository.findById(MovieId).orElseThrow(()->new MovieNotFoundException(MovieId));

            File f = new File(path + movie.getPoster());

            if(f.exists()){
                if(f.delete()){
                    System.out.println("file deleted successfully");
                }
                else throw new FileRemoveException("poster");
            }
            else throw new FileNotFoundException("file not found");

            movieRepository.delete(movie);
        }
        else throw new MovieNotFoundException(MovieId);
    }
}
