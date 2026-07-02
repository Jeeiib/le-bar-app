package fr.lebarapp.api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lebarapp.api.domain.OrderStatus;
import fr.lebarapp.api.domain.Size;
import fr.lebarapp.api.dto.OrderItemRequest;
import fr.lebarapp.api.dto.OrderRequest;
import fr.lebarapp.api.dto.OrderResponse;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.security.JwtService;
import fr.lebarapp.api.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    // Jeton d'un barmaker authentifie, pour les endpoints protegies
    private String barmakerAuth() {
        return "Bearer " + jwtService.generateToken("barmaker@lebarapp.fr", "BARMAKER");
    }

    @Test
    void creationCommandeEstPublique() throws Exception {
        when(orderService.createOrder(any()))
            .thenReturn(new OrderResponse(1L, 1L, "Table 1", "Customer", OrderStatus.ORDERED, LocalDateTime.now(), List.of(), BigDecimal.ZERO));

        OrderRequest request = new OrderRequest(
            1L,
            "Customer",
            List.of(new OrderItemRequest(1L, Size.S))
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void lectureQueueNecessiteBarmaker() throws Exception {
        mockMvc.perform(get("/api/orders/queue"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void lectureQueueAvecTokenBarmakerRenvoie200() throws Exception {
        when(orderService.getQueue())
            .thenReturn(List.of(new OrderResponse(1L, 1L, "Table 1", "Customer", OrderStatus.ORDERED, LocalDateTime.now(), List.of(), BigDecimal.ZERO)));

        mockMvc.perform(get("/api/orders/queue")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void lectureParIdEstPublique() throws Exception {
        when(orderService.getOrderById(1L))
            .thenReturn(new OrderResponse(1L, 1L, "Table 1", "Customer", OrderStatus.IN_PREPARATION, LocalDateTime.now(), List.of(), BigDecimal.ZERO));

        mockMvc.perform(get("/api/orders/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void commandeInexistanteRenvoie404() throws Exception {
        when(orderService.getOrderById(99L))
            .thenThrow(new ResourceNotFoundException("Commande introuvable"));

        mockMvc.perform(get("/api/orders/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void advanceItemPreparationAvecTokenBarmakerRenvoie200() throws Exception {
        when(orderService.advanceItemPreparation(1L, 1L))
            .thenReturn(new OrderResponse(1L, 1L, "Table 1", "Customer", OrderStatus.IN_PREPARATION, LocalDateTime.now(), List.of(), BigDecimal.ZERO));

        mockMvc.perform(patch("/api/orders/1/items/1/advance")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void advanceItemPreparationSansTokenRenvoie4xx() throws Exception {
        mockMvc.perform(patch("/api/orders/1/items/1/advance"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void advanceItemPreparationArticleInexistantRenvoie404() throws Exception {
        when(orderService.advanceItemPreparation(1L, 99L))
            .thenThrow(new ResourceNotFoundException("Article introuvable"));

        mockMvc.perform(patch("/api/orders/1/items/99/advance")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isNotFound());
    }

    @Test
    void advanceItemPreparationAvecBusinessExceptionRenvoie409() throws Exception {
        when(orderService.advanceItemPreparation(1L, 1L))
            .thenThrow(new BusinessException("L'étape de préparation est déjà terminée"));

        mockMvc.perform(patch("/api/orders/1/items/1/advance")
                .header("Authorization", barmakerAuth()))
            .andExpect(status().isConflict());
    }

    @Test
    void creationCommandePayloadInvalideRenvoie400() throws Exception {
        OrderRequest request = new OrderRequest(
            1L,
            "Customer",
            List.of() // Pas d'articles - invalide
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void creationCommandeTableIdNullRenvoie400() throws Exception {
        OrderRequest request = new OrderRequest(
            null,
            "Customer",
            List.of(new OrderItemRequest(1L, Size.S))
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void creationCommandeTableInexistanteRenvoie404() throws Exception {
        when(orderService.createOrder(any()))
            .thenThrow(new ResourceNotFoundException("Table introuvable"));

        OrderRequest request = new OrderRequest(
            99L,
            "Customer",
            List.of(new OrderItemRequest(1L, Size.S))
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void creationCommandeCocktailInexistantRenvoie404() throws Exception {
        when(orderService.createOrder(any()))
            .thenThrow(new ResourceNotFoundException("Cocktail introuvable"));

        OrderRequest request = new OrderRequest(
            1L,
            "Customer",
            List.of(new OrderItemRequest(99L, Size.S))
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void creationCommandeSizePasSupporteeRenvoie409() throws Exception {
        when(orderService.createOrder(any()))
            .thenThrow(new BusinessException("Le cocktail n'a pas la taille demandée"));

        OrderRequest request = new OrderRequest(
            1L,
            "Customer",
            List.of(new OrderItemRequest(1L, Size.S))
        );

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }
}
