package fr.lebarapp.api.dto;

// Une catégorie de la carte renvoyée par l'API.
public record CategoryResponse(
    Long id,
    String name,
    int position
) {}
