package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.BarTable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Accès aux tables du bar ; recherche par slug de QR code pour ouvrir la bonne table.
public interface BarTableRepository extends JpaRepository<BarTable, Long> {
    Optional<BarTable> findByQrSlug(String qrSlug);
}
