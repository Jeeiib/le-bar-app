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
import fr.lebarapp.api.dto.IngredientRequest;
import fr.lebarapp.api.dto.IngredientResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.security.JwtService;
import fr.lebarapp.api.service.IngredientService;
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
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private IngredientService ingredientService;

    // Jeton d'un barmaker authentifie, pour les endpoints protegies
    private String barmakerAuth() {
        return "Bearer " + jwtService.generateToken("barmaker@lebarapp.fr", "BARMAKER");
    }

    @Test
    void lectureDesIngredientsEstPublique() throws Exception {
        when(ingredientService.getAllIngredients())
            .thenReturn(List.of(new IngredientResponse(1L, "Vodka")));

        mockMvc.perform(get("/api/ingredients"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Vodka"));
    }

    @Test
    void creationRefuseeSansAuthentification() throws Exception {
        mockMvc.perform(post("/api/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientRequest("Vodka"))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void creationParBarmakerRenvoie201() throws Exception {
        when(ingredientService.createIngredient(any()))
            .thenReturn(new IngredientResponse(2L, "Rhum"));

        mockMvc.perform(post("/api/ingredients")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientRequest("Rhum"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void payloadInvalideRenvoie400() throws Exception {
        mockMvc.perform(post("/api/ingredients")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientRequest(""))))
            .andExpect(status().isBadRequest());
    }

    @Test
    void ingredientInexistantRenvoie404() throws Exception {
        when(ingredientService.getIngredientById(99L))
            .thenThrow(new ResourceNotFoundException("Ingredient introuvable"));

        mockMvc.perform(get("/api/ingredients/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void miseAJourRenvoie200() throws Exception {
        when(ingredientService.updateIngredient(eq(1L), any()))
            .thenReturn(new IngredientResponse(1L, "Gin Premium"));

        mockMvc.perform(put("/api/ingredients/1")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientRequest("Gin Premium"))))
            .andExpect(status().isOk());
    }

    @Test
    void suppressionRenvoie204() throws Exception {
        mockMvc.perform(delete("/api/ingredients/1")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isNoContent());
    }

    @Test
    void miseAJourSansTokenRenvoie4xx() throws Exception {
        mockMvc.perform(put("/api/ingredients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new IngredientRequest("Gin Premium"))))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void suppressionSansTokenRenvoie4xx() throws Exception {
        mockMvc.perform(delete("/api/ingredients/1"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void lectureDUnIngredientParIdEstPublique() throws Exception {
        when(ingredientService.getIngredientById(1L))
            .thenReturn(new IngredientResponse(1L, "Tequila"));

        mockMvc.perform(get("/api/ingredients/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Tequila"));
    }
}
