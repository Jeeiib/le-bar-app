package fr.lebarapp.api.error;

import java.time.Instant;

public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message
) {}
