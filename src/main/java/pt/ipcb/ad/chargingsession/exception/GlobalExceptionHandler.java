package pt.ipcb.ad.chargingsession.exception;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Translates exceptions into HTTP responses in the standard "problem details" format (RFC 9457),
 * so controllers and services do not need to deal with HTTP status codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({ChargingPointUnavailableException.class, InvalidSessionStateException.class})
    ProblemDetail handleConflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> String.valueOf(error.getDefaultMessage()),
                        (first, second) -> first));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(FeignException.NotFound.class)
    ProblemDetail handleRemoteNotFound(FeignException.NotFound ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Resource not found in a dependent service");
    }

    @ExceptionHandler(FeignException.class)
    ProblemDetail handleRemoteFailure(FeignException ex) {
        log.error("Call to a dependent service failed", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "A dependent service is unavailable");
    }
}
