package offeria.material_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler to catch and process all exceptions across the service.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles custom MaterialServiceException.
     */
    @ExceptionHandler(MaterialServiceException.class)
    public ResponseEntity<ErrorResponse> handleMaterialServiceException(MaterialServiceException ex, WebRequest request) {
        log.error("Service Exception: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), ex.getStatus(), request, null);
    }

    /**
     * Handles Validation exceptions (e.g., @NotBlank, @Size).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error("Validation failed: {}", errors);
        return buildErrorResponse("Validation failed", HttpStatus.BAD_REQUEST, request, errors);
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: ", ex);
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR, request, null);
    }

    /**
     * Helper method to construct the ErrorResponse object.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, WebRequest request, Map<String, String> validationErrors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(validationErrors)
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
}
