package one.digitalinovation.lab_spring_pattern_movie.adapter.handler;

import feign.FeignException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.SocketTimeoutException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TmdbErrorHandler {
    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleUpstreamFailure(FeignException exception) {
        if (NestedExceptionUtils.getMostSpecificCause(exception) instanceof SocketTimeoutException) {
            return ProblemDetail.forStatusAndDetail(GATEWAY_TIMEOUT, "O TMDB excedeu o tempo limite de resposta.");
        }
        return ProblemDetail.forStatusAndDetail(BAD_GATEWAY, "Falha ao consultar o TMDB.");
    }
}
