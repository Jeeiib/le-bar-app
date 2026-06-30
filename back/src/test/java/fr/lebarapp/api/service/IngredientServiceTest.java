package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.dto.IngredientRequest;
import fr.lebarapp.api.dto.IngredientResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.repository.IngredientRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("IngredientService Tests")
class IngredientServiceTest {

  @Mock private IngredientRepository ingredientRepository;

  @InjectMocks private IngredientService ingredientService;

  private Ingredient mockIngredient;

  @BeforeEach
  void setUp() {
    mockIngredient = new Ingredient();
    mockIngredient.setId(1L);
    mockIngredient.setName("Lime Juice");
  }

  @Test
  @DisplayName("getAllIngredients should return all ingredients")
  void testGetAllIngredients_Success() {
    Ingredient ingredient2 = new Ingredient();
    ingredient2.setId(2L);
    ingredient2.setName("Mint Leaves");

    List<Ingredient> ingredients = List.of(mockIngredient, ingredient2);

    when(ingredientRepository.findAll()).thenReturn(ingredients);

    List<IngredientResponse> response = ingredientService.getAllIngredients();

    assertNotNull(response);
    assertEquals(2, response.size());
    verify(ingredientRepository).findAll();
  }

  @Test
  @DisplayName("getIngredientById should return ingredient when found")
  void testGetIngredientById_Success() {
    when(ingredientRepository.findById(1L)).thenReturn(Optional.of(mockIngredient));

    IngredientResponse response = ingredientService.getIngredientById(1L);

    assertNotNull(response);
    assertEquals("Lime Juice", response.name());
    verify(ingredientRepository).findById(1L);
  }

  @Test
  @DisplayName("getIngredientById should throw ResourceNotFoundException when not found")
  void testGetIngredientById_NotFound() {
    when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> ingredientService.getIngredientById(999L));
  }

  @Test
  @DisplayName("createIngredient should save and return new ingredient")
  void testCreateIngredient_Success() {
    IngredientRequest request = new IngredientRequest("Mint Leaves");

    when(ingredientRepository.save(any(Ingredient.class))).thenReturn(mockIngredient);

    IngredientResponse response = ingredientService.createIngredient(request);

    assertNotNull(response);
    assertEquals("Lime Juice", response.name());
    verify(ingredientRepository).save(any(Ingredient.class));
  }

  @Test
  @DisplayName("updateIngredient should update and return ingredient")
  void testUpdateIngredient_Success() {
    IngredientRequest request = new IngredientRequest("Updated Lime");

    when(ingredientRepository.findById(1L)).thenReturn(Optional.of(mockIngredient));
    when(ingredientRepository.save(any(Ingredient.class))).thenReturn(mockIngredient);

    IngredientResponse response = ingredientService.updateIngredient(1L, request);

    assertNotNull(response);
    verify(ingredientRepository).findById(1L);
    verify(ingredientRepository).save(mockIngredient);
  }

  @Test
  @DisplayName("updateIngredient should throw ResourceNotFoundException when not found")
  void testUpdateIngredient_NotFound() {
    IngredientRequest request = new IngredientRequest("Updated Name");

    when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> ingredientService.updateIngredient(999L, request));
  }

  @Test
  @DisplayName("deleteIngredient should delete ingredient")
  void testDeleteIngredient_Success() {
    when(ingredientRepository.findById(1L)).thenReturn(Optional.of(mockIngredient));

    ingredientService.deleteIngredient(1L);

    verify(ingredientRepository).findById(1L);
    verify(ingredientRepository).delete(mockIngredient);
  }

  @Test
  @DisplayName("deleteIngredient should throw ResourceNotFoundException when not found")
  void testDeleteIngredient_NotFound() {
    when(ingredientRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> ingredientService.deleteIngredient(999L));
  }
}
