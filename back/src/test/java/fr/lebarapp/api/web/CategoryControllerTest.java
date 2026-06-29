package fr.lebarapp.api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lebarapp.api.dto.CategoryRequest;
import fr.lebarapp.api.dto.CategoryResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.security.JwtService;
import fr.lebarapp.api.service.CategoryService;
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
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    // Jeton d'un barmaker authentifie, pour les endpoints protegies
    private String barmakerAuth() {
        return "Bearer " + jwtService.generateToken("barmaker@lebarapp.fr", "BARMAKER");
    }

    @Test
    void lectureDeLaCarteEstPublique() throws Exception {
        when(categoryService.getAllCategories())
            .thenReturn(List.of(new CategoryResponse(1L, "Signatures", 0)));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Signatures"));
    }

    @Test
    void creationRefuseeSansAuthentification() throws Exception {
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Classiques", 1))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void creationParBarmakerRenvoie201() throws Exception {
        when(categoryService.createCategory(any()))
            .thenReturn(new CategoryResponse(2L, "Classiques", 1));

        mockMvc.perform(post("/api/categories")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Classiques", 1))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void payloadInvalideRenvoie400() throws Exception {
        mockMvc.perform(post("/api/categories")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("", 0))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void categorieInexistanteRenvoie404() throws Exception {
        when(categoryService.getCategoryById(99L))
            .thenThrow(new ResourceNotFoundException("Categorie introuvable"));

        mockMvc.perform(get("/api/categories/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void miseAJourRenvoie200() throws Exception {
        when(categoryService.updateCategory(eq(1L), any()))
            .thenReturn(new CategoryResponse(1L, "Maj", 2));

        mockMvc.perform(put("/api/categories/1")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CategoryRequest("Maj", 2))))
            .andExpect(status().isOk());
    }

    @Test
    void suppressionRenvoie204() throws Exception {
        mockMvc.perform(delete("/api/categories/1")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isNoContent());
    }
}
