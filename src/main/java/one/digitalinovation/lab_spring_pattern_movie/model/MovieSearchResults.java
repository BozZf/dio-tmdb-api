package one.digitalinovation.lab_spring_pattern_movie.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MovieSearchResults(List<Movie> results) {
}
