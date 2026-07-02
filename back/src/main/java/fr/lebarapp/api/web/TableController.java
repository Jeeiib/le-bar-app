package fr.lebarapp.api.web;

import fr.lebarapp.api.dto.TableRequest;
import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.service.TableService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints des tables du bar : la liste et l'accès par slug de QR code sont publics (client),
// la création d'une table est réservée au barmaker (voir SecurityConfig).
@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    // Public : liste des tables pour la generation des QR codes cote barmaker.
    @GetMapping
    public ResponseEntity<List<TableResponse>> getAllTables() {
        return ResponseEntity.ok(tableService.getAllTables());
    }

    // Barmaker : ajoute une table ; son QR code sera généré côté front à partir du slug renvoyé.
    @PostMapping
    public ResponseEntity<TableResponse> createTable(@Valid @RequestBody TableRequest request) {
        TableResponse table = tableService.createTable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(table);
    }

    // Public : le client scanne le QR (slug) et recupere sa table pour commander.
    @GetMapping("/{qrSlug}")
    public ResponseEntity<TableResponse> getByQrSlug(@PathVariable String qrSlug) {
        return ResponseEntity.ok(tableService.getByQrSlug(qrSlug));
    }
}
