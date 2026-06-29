package fr.lebarapp.api.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.lebarapp.api.external.ExternalCocktailDto;
import fr.lebarapp.api.external.ExternalIngredientDto;
import fr.lebarapp.api.external.TheCocktailDbClient;
import fr.lebarapp.api.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExternalCocktailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TheCocktailDbClient theCocktailDbClient;

    private String barmakerAuth() {
        return "Bearer " + jwtService.generateToken("barmaker@lebarapp.fr", "BARMAKER");
    }

    @Test
    void searchSansTokenRenvoie4xx() throws Exception {
        mockMvc.perform(get("/api/external/cocktails?name=Mojito"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void searchAvecBarmakerRenvoie200() throws Exception {
        ExternalCocktailDto dto = new ExternalCocktailDto(
            "Mojito",
            "Alcohol",
            "Mix and serve",
            "https://example.com/mojito.jpg",
            List.of(new ExternalIngredientDto("Rum", "45ml"))
        );

        when(theCocktailDbClient.search("Mojito"))
            .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/external/cocktails?name=Mojito")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Mojito"));
    }

    @Test
    void searchRetourneListe() throws Exception {
        ExternalCocktailDto dto1 = new ExternalCocktailDto(
            "Mojito",
            "Alcohol",
            "Mix and serve",
            "https://example.com/mojito.jpg",
            List.of()
        );
        ExternalCocktailDto dto2 = new ExternalCocktailDto(
            "Margarita",
            "Alcohol",
            "Mix and serve",
            "https://example.com/margarita.jpg",
            List.of()
        );

        when(theCocktailDbClient.search(anyString()))
            .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/external/cocktails?name=test")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchRetourneListeVide() throws Exception {
        when(theCocktailDbClient.search(anyString()))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/external/cocktails?name=unknown")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }
}
