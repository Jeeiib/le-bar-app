package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.Ingredient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Accès aux ingrédients ; recherche insensible à la casse pour réutiliser un ingrédient déjà connu.
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findByNameIgnoreCase(String name);
}
