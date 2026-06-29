package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.Cocktail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CocktailRepository extends JpaRepository<Cocktail, Long> {
    List<Cocktail> findByCategoryId(Long categoryId);
    List<Cocktail> findByAvailableTrue();
}
