package fr.lebarapp.api.error;

// Violation d'une règle métier (ex : supprimer un cocktail encore présent dans des commandes),
// traduite en HTTP 409 par le GlobalExceptionHandler.
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
