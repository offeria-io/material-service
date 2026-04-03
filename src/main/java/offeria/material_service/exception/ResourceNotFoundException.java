package offeria.material_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a material is not found.
 */
public class ResourceNotFoundException extends MaterialServiceException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
