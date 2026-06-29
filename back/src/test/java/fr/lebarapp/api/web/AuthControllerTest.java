package fr.lebarapp.api.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.lebarapp.api.dto.AuthResponse;
import fr.lebarapp.api.dto.LoginRequest;
import fr.lebarapp.api.dto.RegisterRequest;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.security.JwtService;
import fr.lebarapp.api.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Test
    void registerReussite201() throws Exception {
        when(authService.register(any()))
            .thenReturn(new AuthResponse("token123", "John Doe", "BARMAKER"));

        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").value("token123"))
            .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void loginReussite200() throws Exception {
        when(authService.login(any()))
            .thenReturn(new AuthResponse("token456", "Jane Doe", "BARMAKER"));

        LoginRequest request = new LoginRequest("jane@example.com", "password456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token456"))
            .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    void registerPayloadInvalideRenvoie400() throws Exception {
        RegisterRequest request = new RegisterRequest("", "", "short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginPayloadInvalideRenvoie400() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginEmailInvalideRenvoie400() throws Exception {
        LoginRequest request = new LoginRequest("not-an-email", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registerEmailInvalideRenvoie400() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "not-an-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void registerMotDePasseCourtRenvoie400() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "john@example.com", "short");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginAvecBusinessExceptionRenvoie409() throws Exception {
        when(authService.login(any()))
            .thenThrow(new BusinessException("Email ou mot de passe incorrect"));

        LoginRequest request = new LoginRequest("wrong@example.com", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void registerAvecBusinessExceptionRenvoie409() throws Exception {
        when(authService.register(any()))
            .thenThrow(new BusinessException("Cet email est déjà utilisé"));

        RegisterRequest request = new RegisterRequest("John", "existing@example.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }
}
