package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fr.lebarapp.api.domain.BarTable;
import fr.lebarapp.api.dto.TableRequest;
import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.repository.BarTableRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private BarTableRepository barTableRepository;

    @InjectMocks
    private TableService tableService;

    @Test
    void resolutionParSlugRenvoieLaTable() {
        BarTable table = new BarTable();
        table.setId(1L);
        table.setLabel("Table 5");
        table.setQrSlug("table-5");
        when(barTableRepository.findByQrSlug("table-5")).thenReturn(Optional.of(table));

        TableResponse response = tableService.getByQrSlug("table-5");

        assertEquals("Table 5", response.label());
        assertEquals("table-5", response.qrSlug());
    }

    @Test
    void slugInconnuLeveResourceNotFound() {
        when(barTableRepository.findByQrSlug("inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.getByQrSlug("inconnu"));
    }

    @Test
    void getAllTablesRetourneLalisteTriee() {
        BarTable table1 = new BarTable();
        table1.setId(1L);
        table1.setLabel("Table 1");
        table1.setQrSlug("table-1");

        BarTable table2 = new BarTable();
        table2.setId(2L);
        table2.setLabel("Table 2");
        table2.setQrSlug("table-2");

        when(barTableRepository.findAll(Sort.by("id"))).thenReturn(List.of(table1, table2));

        List<TableResponse> responses = tableService.getAllTables();

        assertEquals(2, responses.size());
        assertEquals("Table 1", responses.get(0).label());
        assertEquals("table-1", responses.get(0).qrSlug());
        assertEquals("Table 2", responses.get(1).label());
        assertEquals("table-2", responses.get(1).qrSlug());
    }

    @Test
    void creationGenereLeSlugDepuisLeNom() {
        when(barTableRepository.findByQrSlug("terrasse-1")).thenReturn(Optional.empty());
        when(barTableRepository.save(any(BarTable.class))).thenAnswer(invocation -> {
            BarTable saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        TableResponse response = tableService.createTable(new TableRequest("Terrasse 1"));

        assertEquals(10L, response.id());
        assertEquals("Terrasse 1", response.label());
        assertEquals("terrasse-1", response.qrSlug());
    }

    @Test
    void creationRefuseeSiSlugDejaPris() {
        BarTable existante = new BarTable();
        existante.setQrSlug("terrasse-1");
        when(barTableRepository.findByQrSlug("terrasse-1")).thenReturn(Optional.of(existante));

        assertThrows(BusinessException.class,
            () -> tableService.createTable(new TableRequest("Terrasse 1")));
    }

    @Test
    void creationRefuseeSiNomSansCaractereValide() {
        assertThrows(BusinessException.class,
            () -> tableService.createTable(new TableRequest("!!!")));
    }
}
