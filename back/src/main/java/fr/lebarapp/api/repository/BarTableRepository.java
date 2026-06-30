package fr.lebarapp.api.repository;

import fr.lebarapp.api.domain.BarTable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarTableRepository extends JpaRepository<BarTable, Long> {
    Optional<BarTable> findByQrSlug(String qrSlug);
}
