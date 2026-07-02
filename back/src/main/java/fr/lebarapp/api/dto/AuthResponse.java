package fr.lebarapp.api.dto;

// Réponse d'authentification : le jeton JWT et les infos d'affichage de l'utilisateur.
public record AuthResponse(
    String token,
    String name,
    String role
) {}
