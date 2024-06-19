package one.digitalinovation.lab_spring_pattern_movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class LabSpringPatternMovieApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabSpringPatternMovieApplication.class, args);
	}

}
