package fr.lebarapp.api.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.service.TableService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TableService tableService;

    @Test
    void resolutionTableEstPublique() throws Exception {
        when(tableService.getByQrSlug("table-1")).thenReturn(new TableResponse(1L, "Table 1"));

        mockMvc.perform(get("/api/tables/table-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.label").value("Table 1"));
    }

    @Test
    void tableInexistanteRenvoie404() throws Exception {
        when(tableService.getByQrSlug("table-999"))
            .thenThrow(new ResourceNotFoundException("Table introuvable"));

        mockMvc.perform(get("/api/tables/table-999"))
            .andExpect(status().isNotFound());
    }
}
