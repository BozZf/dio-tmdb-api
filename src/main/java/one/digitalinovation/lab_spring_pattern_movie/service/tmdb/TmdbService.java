package one.digitalinovation.lab_spring_pattern_movie.service.tmdb;

import feign.FeignException;
import one.digitalinovation.lab_spring_pattern_movie.adapter.tmdb.TmdbClient;
import one.digitalinovation.lab_spring_pattern_movie.model.Movie;
import one.digitalinovation.lab_spring_pattern_movie.model.MovieSearchResults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TmdbService {
    private final TmdbClient client;
    private final String apiKey;

    public TmdbService(TmdbClient client, @Value("${tmdb.api.key}") String apiKey) {
        Assert.hasText(apiKey, "Defina TMDB_API_KEY com uma chave da API do TMDB.");
        this.client = client;
        this.apiKey = apiKey;
    }

    public Movie getMovie(Integer id) {
        try {
            Movie movie = client.getMovie(id, apiKey);
            if (movie == null || !id.equals(movie.id())) {
                throw new ResponseStatusException(BAD_GATEWAY, "Resposta inválida do TMDB.");
            }
            return movie;
        } catch (FeignException.NotFound exception) {
            throw new ResponseStatusException(NOT_FOUND, "Filme não encontrado.");
        }
    }

    public List<Movie> searchMovies(String query) {
        MovieSearchResults response = client.searchMovies(query, apiKey);
        if (response == null || response.results() == null) {
            throw new ResponseStatusException(BAD_GATEWAY, "Resposta inválida do TMDB.");
        }
        return response.results();
    }
}
