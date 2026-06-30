package fr.lebarapp.api.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JwtService Tests")
class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    String secret =
        "mySecretKeyThatIsLongEnoughForHS256AlgorithmToWorkProperly123456789";
    long expirationMs = 3600000; // 1 hour
    jwtService = new JwtService(secret, expirationMs);
  }

  @Test
  @DisplayName("generateToken should create valid JWT token")
  void testGenerateToken_Success() {
    String token = jwtService.generateToken("test@example.com", "BARMAKER");

    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.contains("."));
  }

  @Test
  @DisplayName("extractEmail should return correct email from token")
  void testExtractEmail_Success() {
    String token = jwtService.generateToken("test@example.com", "BARMAKER");
    String email = jwtService.extractEmail(token);

    assertEquals("test@example.com", email);
  }

  @Test
  @DisplayName("extractRole should return correct role from token")
  void testExtractRole_Success() {
    String token = jwtService.generateToken("test@example.com", "BARMAKER");
    String role = jwtService.extractRole(token);

    assertEquals("BARMAKER", role);
  }

  @Test
  @DisplayName("isValid should return true for fresh token")
  void testIsValid_ValidToken() {
    String token = jwtService.generateToken("test@example.com", "BARMAKER");

    assertTrue(jwtService.isValid(token));
  }

  @Test
  @DisplayName("isValid should return false for invalid token")
  void testIsValid_InvalidToken() {
    String invalidToken = "invalid.token.here";

    assertFalse(jwtService.isValid(invalidToken));
  }

  @Test
  @DisplayName("isValid should return false for empty token")
  void testIsValid_EmptyToken() {
    assertFalse(jwtService.isValid(""));
  }

  @Test
  @DisplayName("isValid should return false for malformed token")
  void testIsValid_MalformedToken() {
    String malformedToken = "this-is-not-a-valid-jwt";

    assertFalse(jwtService.isValid(malformedToken));
  }

  @Test
  @DisplayName("extractEmail and extractRole should be consistent with generateToken")
  void testExtractEmailAndRole_Consistency() {
    String email = "barmaker@example.com";
    String role = "BARMAKER";
    String token = jwtService.generateToken(email, role);

    assertEquals(email, jwtService.extractEmail(token));
    assertEquals(role, jwtService.extractRole(token));
  }

  @Test
  @DisplayName("multiple tokens should have different signatures")
  void testMultipleTokens_DifferentSignatures() {
    String token1 = jwtService.generateToken("test1@example.com", "BARMAKER");
    String token2 = jwtService.generateToken("test2@example.com", "BARMAKER");

    assertNotEquals(token1, token2);
  }

  @Test
  @DisplayName("token with different role should be extracted correctly")
  void testExtractRole_DifferentRoles() {
    String token1 = jwtService.generateToken("test@example.com", "BARMAKER");
    String token2 = jwtService.generateToken("test@example.com", "ADMIN");

    assertEquals("BARMAKER", jwtService.extractRole(token1));
    assertEquals("ADMIN", jwtService.extractRole(token2));
  }
}
