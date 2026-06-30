package fr.lebarapp.api.dto;

public record AuthResponse(
    String token,
    String name,
    String role
) {}
