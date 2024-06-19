package one.digitalinovation.lab_spring_pattern_movie.adapter.handler;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
