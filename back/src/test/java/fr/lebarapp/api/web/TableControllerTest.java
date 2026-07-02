package fr.lebarapp.api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lebarapp.api.dto.TableRequest;
import fr.lebarapp.api.dto.TableResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.security.JwtService;
import fr.lebarapp.api.service.TableService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TableService tableService;

    private String barmakerAuth() {
        return "Bearer " + jwtService.generateToken("barmaker@lebarapp.fr", "BARMAKER");
    }

    @Test
    void listeDesTablesEstPublique() throws Exception {
        when(tableService.getAllTables()).thenReturn(List.of(
            new TableResponse(1L, "Table 1", "table-1"),
            new TableResponse(2L, "Table 2", "table-2")
        ));

        mockMvc.perform(get("/api/tables"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].label").value("Table 1"))
            .andExpect(jsonPath("$[0].qrSlug").value("table-1"))
            .andExpect(jsonPath("$[1].label").value("Table 2"))
            .andExpect(jsonPath("$[1].qrSlug").value("table-2"));
    }

    @Test
    void resolutionTableEstPublique() throws Exception {
        when(tableService.getByQrSlug("table-1")).thenReturn(new TableResponse(1L, "Table 1", "table-1"));

        mockMvc.perform(get("/api/tables/table-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.label").value("Table 1"))
            .andExpect(jsonPath("$.qrSlug").value("table-1"));
    }

    @Test
    void tableInexistanteRenvoie404() throws Exception {
        when(tableService.getByQrSlug("table-999"))
            .thenThrow(new ResourceNotFoundException("Table introuvable"));

        mockMvc.perform(get("/api/tables/table-999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void creationTableRefuseeSansAuthentification() throws Exception {
        mockMvc.perform(post("/api/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TableRequest("Terrasse 1"))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void creationTableParBarmakerRenvoie201() throws Exception {
        when(tableService.createTable(any()))
            .thenReturn(new TableResponse(13L, "Terrasse 1", "terrasse-1"));

        mockMvc.perform(post("/api/tables")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TableRequest("Terrasse 1"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.qrSlug").value("terrasse-1"));
    }
}
