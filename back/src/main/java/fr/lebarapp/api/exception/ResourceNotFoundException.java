package fr.lebarapp.api.exception;

// Ressource demandée introuvable, traduite en HTTP 404 par le GlobalExceptionHandler.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
