package one.digitalinovation.lab_spring_pattern_movie.service.tmdb;

import one.digitalinovation.lab_spring_pattern_movie.model.Movie;
import one.digitalinovation.lab_spring_pattern_movie.model.MovieSearchResults;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "tmdb", url = "https://api.themoviedb.org/3")
public interface TmdbService {
    @GetMapping("/movie/{movieId}")
    Movie getMovie(@PathVariable("movieId") Integer movieId, @RequestParam("api_key") String apiKey);

    @GetMapping("/search/movie")
    MovieSearchResults searchMovies(@RequestParam("query") String query, @RequestParam("api_key") String apiKey);
}
