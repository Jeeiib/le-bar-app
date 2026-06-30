package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fr.lebarapp.api.domain.BarTable;
import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.repository.BarTableRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private BarTableRepository barTableRepository;

    @InjectMocks
    private TableService tableService;

    @Test
    void resolutionParSlugRenvoieLaTable() {
        BarTable table = new BarTable();
        table.setLabel("Table 5");
        when(barTableRepository.findByQrSlug("table-5")).thenReturn(Optional.of(table));

        TableResponse response = tableService.getByQrSlug("table-5");

        assertEquals("Table 5", response.label());
    }

    @Test
    void slugInconnuLeveResourceNotFound() {
        when(barTableRepository.findByQrSlug("inconnu")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tableService.getByQrSlug("inconnu"));
    }
}
