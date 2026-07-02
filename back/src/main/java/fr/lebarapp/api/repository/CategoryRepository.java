package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// Accès aux catégories de la carte, renvoyées dans l'ordre d'affichage voulu.
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByPositionAsc();
}
