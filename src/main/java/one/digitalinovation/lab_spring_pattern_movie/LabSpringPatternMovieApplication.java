package one.digitalinovation.lab_spring_pattern_movie;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "DIO TMDB API", version = "0.0.1",
        description = "Consulta e busca de filmes no The Movie Database."))
@EnableFeignClients
@SpringBootApplication
public class LabSpringPatternMovieApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabSpringPatternMovieApplication.class, args);
	}

}
