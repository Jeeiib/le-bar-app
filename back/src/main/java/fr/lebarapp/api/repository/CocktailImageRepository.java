package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.CocktailImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CocktailImageRepository extends JpaRepository<CocktailImage, Long> {
    Optional<CocktailImage> findByCocktailId(Long cocktailId);
}
