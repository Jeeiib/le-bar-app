package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.domain.Size;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.dto.SizePriceRequest;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.mapper.CocktailMapper;
import fr.lebarapp.api.repository.CategoryRepository;
import fr.lebarapp.api.repository.CocktailRepository;
import fr.lebarapp.api.repository.IngredientRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CocktailService Tests")
class CocktailServiceTest {

  @Mock private CocktailRepository cocktailRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private IngredientRepository ingredientRepository;

  @InjectMocks private CocktailService cocktailService;

  private Cocktail mockCocktail;
  private Category mockCategory;
  private Ingredient mockIngredient;

  @BeforeEach
  void setUp() {
    mockCategory = new Category();
    mockCategory.setId(1L);
    mockCategory.setName("Spirits");

    mockIngredient = new Ingredient();
    mockIngredient.setId(1L);
    mockIngredient.setName("Lime Juice");

    mockCocktail = new Cocktail();
    mockCocktail.setId(1L);
    mockCocktail.setName("Mojito");
    mockCocktail.setCategory(mockCategory);
    mockCocktail.setIngredients(new HashSet<>(List.of(mockIngredient)));
  }

  @Test
  @DisplayName("getAllCocktails should return all cocktails")
  void testGetAllCocktails_Success() {
    Cocktail cocktail2 = new Cocktail();
    cocktail2.setId(2L);
    cocktail2.setName("Margarita");
    cocktail2.setCategory(mockCategory);
    cocktail2.setIngredients(new HashSet<>());

    List<Cocktail> cocktails = List.of(mockCocktail, cocktail2);

    when(cocktailRepository.findAll()).thenReturn(cocktails);

    List<CocktailResponse> response = cocktailService.getAllCocktails();

    assertNotNull(response);
    assertEquals(2, response.size());
    verify(cocktailRepository).findAll();
  }

  @Test
  @DisplayName("getCocktailsByCategory should return cocktails for category")
  void testGetCocktailsByCategory_Success() {
    List<Cocktail> cocktails = List.of(mockCocktail);

    when(cocktailRepository.findByCategoryId(1L)).thenReturn(cocktails);

    List<CocktailResponse> response = cocktailService.getCocktailsByCategory(1L);

    assertNotNull(response);
    assertEquals(1, response.size());
    verify(cocktailRepository).findByCategoryId(1L);
  }

  @Test
  @DisplayName("getCocktailById should return cocktail when found")
  void testGetCocktailById_Success() {
    when(cocktailRepository.findById(1L)).thenReturn(Optional.of(mockCocktail));

    CocktailResponse response = cocktailService.getCocktailById(1L);

    assertNotNull(response);
    assertEquals("Mojito", response.name());
    verify(cocktailRepository).findById(1L);
  }

  @Test
  @DisplayName("getCocktailById should throw ResourceNotFoundException when not found")
  void testGetCocktailById_NotFound() {
    when(cocktailRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> cocktailService.getCocktailById(999L));
  }

  @Test
  @DisplayName("createCocktail should resolve category and save cocktail")
  void testCreateCocktail_Success() {
    List<SizePriceRequest> sizes =
        List.of(new SizePriceRequest(Size.S, new BigDecimal("5.00")));
    CocktailRequest request =
        new CocktailRequest("Mojito", "A classic mojito", "img.jpg", true, 1L, new HashSet<>(List.of(1L)), sizes);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
    when(ingredientRepository.findAllById(anyCollection())).thenReturn(List.of(mockIngredient));
    when(cocktailRepository.save(any(Cocktail.class))).thenReturn(mockCocktail);

    CocktailResponse response = cocktailService.createCocktail(request);

    assertNotNull(response);
    assertEquals("Mojito", response.name());
    verify(categoryRepository).findById(1L);
    verify(cocktailRepository).save(any(Cocktail.class));
  }

  @Test
  @DisplayName("createCocktail should throw ResourceNotFoundException when category not found")
  void testCreateCocktail_CategoryNotFound() {
    List<SizePriceRequest> sizes =
        List.of(new SizePriceRequest(Size.S, new BigDecimal("5.00")));
    CocktailRequest request =
        new CocktailRequest("Mojito", "A classic mojito", "img.jpg", true, 999L, new HashSet<>(List.of(1L)), sizes);

    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> cocktailService.createCocktail(request));
  }

  @Test
  @DisplayName("createCocktail should handle null or empty ingredient list")
  void testCreateCocktail_NoIngredients() {
    List<SizePriceRequest> sizes =
        List.of(new SizePriceRequest(Size.S, new BigDecimal("5.00")));
    CocktailRequest request = new CocktailRequest("Mojito", "A classic mojito", "img.jpg", true, 1L, null, sizes);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
    when(cocktailRepository.save(any(Cocktail.class))).thenReturn(mockCocktail);

    CocktailResponse response = cocktailService.createCocktail(request);

    assertNotNull(response);
    verify(categoryRepository).findById(1L);
    verify(cocktailRepository).save(any(Cocktail.class));
  }

  @Test
  @DisplayName("updateCocktail should update and resolve category")
  void testUpdateCocktail_Success() {
    List<SizePriceRequest> sizes =
        List.of(new SizePriceRequest(Size.S, new BigDecimal("5.00")));
    CocktailRequest request =
        new CocktailRequest("Updated Mojito", "Updated description", "img.jpg", true, 1L, new HashSet<>(List.of(1L)), sizes);

    when(cocktailRepository.findById(1L)).thenReturn(Optional.of(mockCocktail));
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
    when(ingredientRepository.findAllById(anyCollection())).thenReturn(List.of(mockIngredient));
    when(cocktailRepository.save(any(Cocktail.class))).thenReturn(mockCocktail);

    CocktailResponse response = cocktailService.updateCocktail(1L, request);

    assertNotNull(response);
    verify(cocktailRepository).findById(1L);
    verify(categoryRepository).findById(1L);
    verify(cocktailRepository).save(mockCocktail);
  }

  @Test
  @DisplayName("updateCocktail should throw ResourceNotFoundException when cocktail not found")
  void testUpdateCocktail_CocktailNotFound() {
    List<SizePriceRequest> sizes =
        List.of(new SizePriceRequest(Size.S, new BigDecimal("5.00")));
    CocktailRequest request =
        new CocktailRequest("Mojito", "Description", "img.jpg", true, 1L, new HashSet<>(List.of(1L)), sizes);

    when(cocktailRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> cocktailService.updateCocktail(999L, request));
  }

  @Test
  @DisplayName("updateCocktail should throw ResourceNotFoundException when category not found")
  void testUpdateCocktail_CategoryNotFound() {
    List<SizePriceRequest> sizes =
        List.of(new SizePriceRequest(Size.S, new BigDecimal("5.00")));
    CocktailRequest request =
        new CocktailRequest("Mojito", "Description", "img.jpg", true, 999L, new HashSet<>(List.of(1L)), sizes);

    when(cocktailRepository.findById(1L)).thenReturn(Optional.of(mockCocktail));
    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> cocktailService.updateCocktail(1L, request));
  }

  @Test
  @DisplayName("deleteCocktail should delete cocktail")
  void testDeleteCocktail_Success() {
    when(cocktailRepository.findById(1L)).thenReturn(Optional.of(mockCocktail));

    cocktailService.deleteCocktail(1L);

    verify(cocktailRepository).findById(1L);
    verify(cocktailRepository).delete(mockCocktail);
  }

  @Test
  @DisplayName("deleteCocktail should throw ResourceNotFoundException when not found")
  void testDeleteCocktail_NotFound() {
    when(cocktailRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> cocktailService.deleteCocktail(999L));
  }
}
