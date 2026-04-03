package offeria.material_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base custom exception for the Material Service.
 */
@Getter
public class MaterialServiceException extends RuntimeException {
    private final HttpStatus status;

    public MaterialServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
