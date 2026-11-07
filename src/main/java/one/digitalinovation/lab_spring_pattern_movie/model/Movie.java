package one.digitalinovation.lab_spring_pattern_movie.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Movie(
        @JsonAlias("poster_path") String posterPath,
        Boolean adult,
        String overview,
        @JsonAlias("release_date") String releaseDate,
        @JsonAlias("genre_ids") List<Integer> genreIds,
        Integer id,
        @JsonAlias("original_title") String originalTitle,
        @JsonAlias("original_language") String originalLanguage,
        String title,
        @JsonAlias("backdrop_path") String backdropPath,
        Double popularity,
        @JsonAlias("vote_count") Integer voteCount,
        Boolean video,
        @JsonAlias("vote_average") Double voteAverage
) {
}
