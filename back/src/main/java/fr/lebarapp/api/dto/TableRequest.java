package fr.lebarapp.api.dto;

import jakarta.validation.constraints.NotBlank;

// Données reçues du barmaker pour créer une nouvelle table du bar (le slug du QR en est déduit).
public record TableRequest(
    @NotBlank(message = "Le nom de la table ne peut pas être vide")
    String label
) {}
