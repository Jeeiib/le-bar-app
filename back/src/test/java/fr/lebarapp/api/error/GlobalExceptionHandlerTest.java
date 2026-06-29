package fr.lebarapp.api.error;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("handleResourceNotFound should return 404 with appropriate message")
  void testHandleResourceNotFound() {
    ResourceNotFoundException exception = new ResourceNotFoundException("Entity not found");

    ResponseEntity<ApiError> response = globalExceptionHandler.handleResourceNotFound(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().status());
    assertEquals("Entity not found", response.getBody().message());
  }

  @Test
  @DisplayName("handleBusinessException should return 409 with appropriate message")
  void testHandleBusinessException() {
    BusinessException exception = new BusinessException("Business rule violated");

    ResponseEntity<ApiError> response = globalExceptionHandler.handleBusinessException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(409, response.getBody().status());
    assertEquals("Business rule violated", response.getBody().message());
  }

  @Test
  @DisplayName("handleResourceNotFound should preserve exact message")
  void testHandleResourceNotFound_PreservesMessage() {
    String message = "User with ID 123 not found";
    ResourceNotFoundException exception = new ResourceNotFoundException(message);

    ResponseEntity<ApiError> response = globalExceptionHandler.handleResourceNotFound(exception);

    assertEquals(message, response.getBody().message());
  }

  @Test
  @DisplayName("handleBusinessException should preserve exact message")
  void testHandleBusinessException_PreservesMessage() {
    String message = "Email already registered";
    BusinessException exception = new BusinessException(message);

    ResponseEntity<ApiError> response = globalExceptionHandler.handleBusinessException(exception);

    assertEquals(message, response.getBody().message());
  }
}
