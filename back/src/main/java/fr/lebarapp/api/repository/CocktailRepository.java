package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.Cocktail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Accès aux cocktails. Spring Data JPA fournit déjà le CRUD via JpaRepository et génère
// le SQL des méthodes ci-dessous à partir de leur nom (findByCategoryId → WHERE category_id = ?,
// findByAvailableTrue → WHERE available = true) : aucune requête à écrire à la main.
public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    List<Cocktail> findByCategoryId(Long categoryId);
    List<Cocktail> findByAvailableTrue();
}
