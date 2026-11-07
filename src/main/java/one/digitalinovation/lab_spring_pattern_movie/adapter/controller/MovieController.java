package one.digitalinovation.lab_spring_pattern_movie.adapter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import one.digitalinovation.lab_spring_pattern_movie.model.Movie;
import one.digitalinovation.lab_spring_pattern_movie.service.tmdb.TmdbService;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@ApiResponse(responseCode = "502", description = "Falha ou resposta inválida do TMDB",
        content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "504", description = "Tempo limite do TMDB excedido",
        content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
public class MovieController {
    private final TmdbService tmdbService;

    public MovieController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @Operation(summary = "Consulta um filme pelo ID no TMDB")
    @ApiResponse(responseCode = "200", description = "Filme encontrado")
    @ApiResponse(responseCode = "400", description = "ID inválido",
            content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Filme não encontrado",
            content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping(value = "/movie/{id}", produces = APPLICATION_JSON_VALUE)
    public Movie getMovie(@Parameter(description = "ID do filme no TMDB", example = "603")
                          @PathVariable @Min(1) Integer id) {
        return tmdbService.getMovie(id);
    }

    @Operation(summary = "Busca filmes pelo título")
    @ApiResponse(responseCode = "200", description = "Filmes da primeira página de resultados")
    @ApiResponse(responseCode = "400", description = "Consulta ausente ou em branco",
            content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping(value = "/search/movie", produces = APPLICATION_JSON_VALUE)
    public List<Movie> searchMovies(@Parameter(description = "Título ou trecho do título", example = "The Matrix")
                                    @RequestParam @NotBlank String query) {
        return tmdbService.searchMovies(query);
    }
}
