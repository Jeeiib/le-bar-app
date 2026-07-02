package fr.lebarapp.api.web;

import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.service.TableService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // Public : le client scanne le QR (slug) et recupere sa table pour commander.
    @GetMapping("/{qrSlug}")
    public ResponseEntity<TableResponse> getByQrSlug(@PathVariable String qrSlug) {
        return ResponseEntity.ok(tableService.getByQrSlug(qrSlug));
    }
}
