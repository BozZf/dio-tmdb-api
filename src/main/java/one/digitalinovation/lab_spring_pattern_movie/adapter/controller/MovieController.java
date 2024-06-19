package one.digitalinovation.lab_spring_pattern_movie.adapter.controller;

import one.digitalinovation.lab_spring_pattern_movie.model.Movie;
import one.digitalinovation.lab_spring_pattern_movie.model.MovieSearchResults;
import one.digitalinovation.lab_spring_pattern_movie.service.tmdb.TmdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
public class MovieController {
    private final TmdbService tmdbService;
    private final String apiKey;

    @Autowired
    public MovieController(TmdbService tmdbService, @Value("${tmdb.api.key}") String apiKey) {
        this.tmdbService = tmdbService;
        this.apiKey = apiKey;
    }

    @GetMapping("/movie/{id}")
    public ResponseEntity<Movie> getMovie(@PathVariable Integer id) {
        Movie movie = tmdbService.getMovie(id, apiKey);
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/search/movie")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String query) {
        MovieSearchResults results = tmdbService.searchMovies(query, apiKey);
        return ResponseEntity.ok(results.getResults());
    }

}
