package fr.lebarapp.api.service;

import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.repository.BarTableRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Resout la table du client a partir du slug encode dans le QR code.
@Service
public class TableService {

    private final BarTableRepository barTableRepository;

    public TableService(BarTableRepository barTableRepository) {
        this.barTableRepository = barTableRepository;
    }

    @Transactional(readOnly = true)
    public TableResponse getByQrSlug(String qrSlug) {
        return barTableRepository.findByQrSlug(qrSlug)
            .map(t -> new TableResponse(t.getId(), t.getLabel(), t.getQrSlug()))
            .orElseThrow(() -> new ResourceNotFoundException("Table introuvable pour le QR: " + qrSlug));
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getAllTables() {
        return barTableRepository.findAll(Sort.by("id"))
            .stream()
            .map(t -> new TableResponse(t.getId(), t.getLabel(), t.getQrSlug()))
            .toList();
    }
}
