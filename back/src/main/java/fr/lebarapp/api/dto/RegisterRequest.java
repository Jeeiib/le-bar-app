package fr.lebarapp.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Données de création d'un compte staff (nom, email, mot de passe).
public record RegisterRequest(
    @NotBlank(message = "Le nom est requis")
    String name,

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est requis")
    String email,

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password
) {}
