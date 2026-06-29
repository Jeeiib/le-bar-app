package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.lebarapp.api.domain.User;
import fr.lebarapp.api.domain.UserRole;
import fr.lebarapp.api.dto.AuthResponse;
import fr.lebarapp.api.dto.LoginRequest;
import fr.lebarapp.api.dto.RegisterRequest;
import fr.lebarapp.api.error.BusinessException;
import fr.lebarapp.api.repository.UserRepository;
import fr.lebarapp.api.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;

  @InjectMocks private AuthService authService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setEmail("test@example.com");
    mockUser.setName("Test User");
    mockUser.setPasswordHash("$2a$10$encryptedPassword");
    mockUser.setRole(UserRole.BARMAKER);
  }

  @Test
  @DisplayName("register should create user with unique email")
  void testRegister_Success() {
    RegisterRequest request = new RegisterRequest("John Doe", "newuser@example.com", "password123");
    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setEmail("newuser@example.com");
    savedUser.setName("John Doe");
    savedUser.setPasswordHash("$2a$10$encodedPassword");
    savedUser.setRole(UserRole.BARMAKER);

    when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateToken("newuser@example.com", "BARMAKER"))
        .thenReturn("mock-jwt-token");

    AuthResponse response = authService.register(request);

    assertNotNull(response);
    assertEquals("mock-jwt-token", response.token());
    assertEquals("John Doe", response.name());
    assertEquals("BARMAKER", response.role());
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("password123");
  }

  @Test
  @DisplayName("register should throw BusinessException when email already exists")
  void testRegister_EmailAlreadyExists() {
    RegisterRequest request = new RegisterRequest("John Doe", "existing@example.com", "password123");

    when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

    assertThrows(BusinessException.class, () -> authService.register(request));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("register should assign BARMAKER role to new user")
  void testRegister_AssignsBarmakerRole() {
    RegisterRequest request = new RegisterRequest("Jane Doe", "jdoe@example.com", "password456");

    when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password456")).thenReturn("$2a$10$encodedPassword");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      assertEquals(UserRole.BARMAKER, user.getRole());
      user.setId(2L);
      return user;
    });
    when(jwtService.generateToken(any(), eq("BARMAKER"))).thenReturn("mock-token");

    authService.register(request);

    verify(userRepository).save(argThat(user -> user.getRole() == UserRole.BARMAKER));
  }

  @Test
  @DisplayName("register should encode password before saving")
  void testRegister_EncodesPassword() {
    RegisterRequest request = new RegisterRequest("Jane Doe", "user2@example.com", "plainPassword");

    when(userRepository.existsByEmail("user2@example.com")).thenReturn(false);
    when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(mockUser);
    when(jwtService.generateToken(any(), any())).thenReturn("mock-token");

    authService.register(request);

    verify(passwordEncoder).encode("plainPassword");
  }

  @Test
  @DisplayName("login should return token when credentials are correct")
  void testLogin_Success() {
    LoginRequest request = new LoginRequest("test@example.com", "password123");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("password123", mockUser.getPasswordHash())).thenReturn(true);
    when(jwtService.generateToken("test@example.com", "BARMAKER")).thenReturn("mock-jwt-token");

    AuthResponse response = authService.login(request);

    assertNotNull(response);
    assertEquals("mock-jwt-token", response.token());
    assertEquals("Test User", response.name());
    assertEquals("BARMAKER", response.role());
  }

  @Test
  @DisplayName("login should throw BusinessException when email not found")
  void testLogin_EmailNotFound() {
    LoginRequest request = new LoginRequest("nonexistent@example.com", "password123");

    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> authService.login(request));
  }

  @Test
  @DisplayName("login should throw BusinessException when password is incorrect")
  void testLogin_IncorrectPassword() {
    LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("wrongPassword", mockUser.getPasswordHash())).thenReturn(false);

    assertThrows(BusinessException.class, () -> authService.login(request));
  }

  @Test
  @DisplayName("login should verify password against hash")
  void testLogin_VerifiesPasswordMatch() {
    LoginRequest request = new LoginRequest("test@example.com", "password123");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("password123", mockUser.getPasswordHash())).thenReturn(true);
    when(jwtService.generateToken(any(), any())).thenReturn("mock-token");

    authService.login(request);

    verify(passwordEncoder).matches("password123", mockUser.getPasswordHash());
  }

  @Test
  @DisplayName("login should generate token with correct email and role")
  void testLogin_GeneratesTokenWithCorrectClaims() {
    LoginRequest request = new LoginRequest("test@example.com", "password123");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
    when(passwordEncoder.matches("password123", mockUser.getPasswordHash())).thenReturn(true);
    when(jwtService.generateToken("test@example.com", "BARMAKER")).thenReturn("mock-jwt-token");

    authService.login(request);

    verify(jwtService).generateToken("test@example.com", "BARMAKER");
  }
}
