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
import fr.lebarapp.api.domain.Size;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.dto.SizePriceRequest;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.security.JwtService;
import fr.lebarapp.api.service.CocktailService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CocktailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CocktailService cocktailService;

    // Jeton d'un barmaker authentifie, pour les endpoints protegies
    private String barmakerAuth() {
        return "Bearer " + jwtService.generateToken("barmaker@lebarapp.fr", "BARMAKER");
    }

    @Test
    void lectureDeTousLesCoktailsEstPublique() throws Exception {
        when(cocktailService.getAllCocktails())
            .thenReturn(List.of(new CocktailResponse(1L, "Mojito", "Classic", "img.jpg", true, 1L, "Signatures", List.of(), List.of())));

        mockMvc.perform(get("/api/cocktails"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Mojito"));
    }

    @Test
    void lectureDesCoktailsParCategorieEstPublique() throws Exception {
        when(cocktailService.getCocktailsByCategory(1L))
            .thenReturn(List.of(new CocktailResponse(1L, "Mojito", "Classic", "img.jpg", true, 1L, "Signatures", List.of(), List.of())));

        mockMvc.perform(get("/api/cocktails?categoryId=1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Mojito"));
    }

    @Test
    void lectureParIdEstPublique() throws Exception {
        when(cocktailService.getCocktailById(1L))
            .thenReturn(new CocktailResponse(1L, "Margarita", "Mexican", "img.jpg", true, 1L, "Signatures", List.of(), List.of()));

        mockMvc.perform(get("/api/cocktails/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Margarita"));
    }

    @Test
    void creationRefuseeSansAuthentification() throws Exception {
        CocktailRequest request = new CocktailRequest(
            "Daiquiri",
            "Classic",
            "img.jpg",
            true,
            1L,
            Set.of(1L),
            List.of(new SizePriceRequest(Size.S, new BigDecimal("8.0")))
        );

        mockMvc.perform(post("/api/cocktails")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void creationParBarmakerRenvoie201() throws Exception {
        when(cocktailService.createCocktail(any()))
            .thenReturn(new CocktailResponse(2L, "Daiquiri", "Classic", "img.jpg", true, 1L, "Signatures", List.of(), List.of()));

        CocktailRequest request = new CocktailRequest(
            "Daiquiri",
            "Classic",
            "img.jpg",
            true,
            1L,
            Set.of(1L),
            List.of(new SizePriceRequest(Size.S, new BigDecimal("8.0")))
        );

        mockMvc.perform(post("/api/cocktails")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void payloadInvalideRenvoie400() throws Exception {
        CocktailRequest request = new CocktailRequest(
            "",
            "Classic",
            "img.jpg",
            true,
            1L,
            Set.of(1L),
            List.of(new SizePriceRequest(Size.S, new BigDecimal("8.0")))
        );

        mockMvc.perform(post("/api/cocktails")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void payloadSansTaillesRenvoie400() throws Exception {
        CocktailRequest request = new CocktailRequest(
            "Margarita",
            "Mexican",
            "img.jpg",
            true,
            1L,
            Set.of(1L),
            List.of()
        );

        mockMvc.perform(post("/api/cocktails")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void coktailInexistantRenvoie404() throws Exception {
        when(cocktailService.getCocktailById(99L))
            .thenThrow(new ResourceNotFoundException("Cocktail introuvable"));

        mockMvc.perform(get("/api/cocktails/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void miseAJourRenvoie200() throws Exception {
        when(cocktailService.updateCocktail(eq(1L), any()))
            .thenReturn(new CocktailResponse(1L, "Mojito Premium", "Classic", "img.jpg", true, 1L, "Signatures", List.of(), List.of()));

        CocktailRequest request = new CocktailRequest(
            "Mojito Premium",
            "Classic",
            "img.jpg",
            true,
            1L,
            Set.of(1L),
            List.of(new SizePriceRequest(Size.L, new BigDecimal("10.0")))
        );

        mockMvc.perform(put("/api/cocktails/1")
                .header("Authorization", barmakerAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void suppressionRenvoie204() throws Exception {
        mockMvc.perform(delete("/api/cocktails/1")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isNoContent());
    }

    @Test
    void miseAJourSansTokenRenvoie4xx() throws Exception {
        CocktailRequest request = new CocktailRequest(
            "Mojito Premium",
            "Classic",
            "img.jpg",
            true,
            1L,
            Set.of(1L),
            List.of(new SizePriceRequest(Size.L, new BigDecimal("10.0")))
        );

        mockMvc.perform(put("/api/cocktails/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void suppressionSansTokenRenvoie4xx() throws Exception {
        mockMvc.perform(delete("/api/cocktails/1"))
            .andExpect(status().is4xxClientError());
    }
}
