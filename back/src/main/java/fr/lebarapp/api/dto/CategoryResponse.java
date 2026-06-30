package fr.lebarapp.api.dto;

public record CategoryResponse(
    Long id,
    String name,
    int position
) {}
