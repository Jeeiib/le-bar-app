package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Accès aux comptes staff : recherche par email (connexion) et test d'existence (inscription).
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
