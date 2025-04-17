package com.moviemeta.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer movieId;

    @NotBlank(message = "Please provide movie's title!")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Please provide movie's director!")
    @Column(nullable = false, length = 200)
    private String director;

    @NotBlank(message = "Please provide movie's studio!")
    @Column(nullable = false, length = 200)
    private String studio;

    @ElementCollection
    @CollectionTable(name = "movie_cast")
    private Set<String> movieCast;

    @Column(nullable = false)
    private Integer releaseYear;

    @NotBlank(message = "Please provide movie's poster!")
    @Column(nullable = false, length = 200)
    private String poster;
}
