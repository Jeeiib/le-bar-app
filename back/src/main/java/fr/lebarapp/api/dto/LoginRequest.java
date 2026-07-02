package fr.lebarapp.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Identifiants de connexion saisis par un barmaker.
public record LoginRequest(
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est requis")
    String email,

    @NotBlank(message = "Le mot de passe est requis")
    String password
) {}
