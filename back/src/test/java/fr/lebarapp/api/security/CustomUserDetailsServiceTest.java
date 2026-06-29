package fr.lebarapp.api.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.lebarapp.api.domain.User;
import fr.lebarapp.api.domain.UserRole;
import fr.lebarapp.api.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private CustomUserDetailsService customUserDetailsService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setEmail("test@example.com");
    mockUser.setName("Test User");
    mockUser.setPasswordHash("$2a$10$encodedPassword");
    mockUser.setRole(UserRole.BARMAKER);
  }

  @Test
  @DisplayName("loadUserByUsername should return UserDetails when user found")
  void testLoadUserByUsername_Success() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

    UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

    assertNotNull(userDetails);
    assertEquals("test@example.com", userDetails.getUsername());
    assertEquals("$2a$10$encodedPassword", userDetails.getPassword());
    assertTrue(userDetails.isEnabled());
    verify(userRepository).findByEmail("test@example.com");
  }

  @Test
  @DisplayName("loadUserByUsername should throw UsernameNotFoundException when user not found")
  void testLoadUserByUsername_NotFound() {
    when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class,
        () -> customUserDetailsService.loadUserByUsername("notfound@example.com"));
  }

  @Test
  @DisplayName("loadUserByUsername should have BARMAKER role")
  void testLoadUserByUsername_Role() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

    UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

    assertTrue(userDetails.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_BARMAKER")));
  }
}
