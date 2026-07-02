package fr.lebarapp.api.error;

import java.time.Instant;

// Corps JSON standard renvoyé au client quand une erreur survient.
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message
) {}
