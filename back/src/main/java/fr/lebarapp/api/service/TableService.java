package fr.lebarapp.api.service;

import fr.lebarapp.api.domain.BarTable;
import fr.lebarapp.api.dto.TableRequest;
import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.repository.BarTableRepository;
import java.text.Normalizer;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Gère les tables du bar : résolution à partir du slug encodé dans le QR code (client),
// liste et création d'une nouvelle table (barmaker).
@Service
public class TableService {

    private final BarTableRepository barTableRepository;

    public TableService(BarTableRepository barTableRepository) {
        this.barTableRepository = barTableRepository;
    }

    @Transactional
    public TableResponse createTable(TableRequest request) {
        // Le slug du QR est déduit du nom saisi (ex : "Terrasse 1" -> "terrasse-1").
        String slug = slugify(request.label());
        if (slug.isEmpty()) {
            throw new BusinessException("Le nom de la table ne permet pas de générer un identifiant valide.");
        }
        if (barTableRepository.findByQrSlug(slug).isPresent()) {
            throw new BusinessException("Une table portant ce nom existe déjà.");
        }
        BarTable table = new BarTable();
        table.setLabel(request.label().trim());
        table.setQrSlug(slug);
        table = barTableRepository.save(table);
        return new TableResponse(table.getId(), table.getLabel(), table.getQrSlug());
    }

    // Transforme un nom libre en slug d'URL : sans accents, en minuscules, tirets à la place
    // de tout ce qui n'est ni lettre ni chiffre.
    private String slugify(String label) {
        String noAccents = Normalizer.normalize(label, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return noAccents.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
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
