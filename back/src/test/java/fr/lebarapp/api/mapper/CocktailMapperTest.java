package fr.lebarapp.api.mapper;

import static org.junit.jupiter.api.Assertions.*;

import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.domain.CocktailIngredient;
import fr.lebarapp.api.domain.Size;
import fr.lebarapp.api.dto.CocktailIngredientRequest;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.dto.SizePriceRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CocktailMapper Tests")
class CocktailMapperTest {

  private Cocktail mockCocktail;
  private Category mockCategory;

  @BeforeEach
  void setUp() {
    mockCategory = new Category();
    mockCategory.setId(1L);
    mockCategory.setName("Spirits");

    mockCocktail = new Cocktail();
    mockCocktail.setId(1L);
    mockCocktail.setName("Mojito");
    mockCocktail.setDescription("A classic mojito");
    mockCocktail.setAvailable(true);
    mockCocktail.setCategory(mockCategory);
    mockCocktail.setIngredients(new ArrayList<>());
  }

  @Test
  @DisplayName("toResponse should convert Cocktail to CocktailResponse")
  void testToResponse() {
    CocktailResponse response = CocktailMapper.toResponse(mockCocktail);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Mojito", response.name());
    assertEquals("A classic mojito", response.description());
    assertEquals(1L, response.categoryId());
    assertNotNull(response.imageUrl());
  }

  @Test
  @DisplayName("toEntity should convert CocktailRequest to Cocktail")
  void testToEntity() {
    CocktailRequest request = new CocktailRequest(
        "Mojito",
        "A classic",
        true,
        1L,
        List.of(),
        "",
        List.of()
    );

    Cocktail cocktail = CocktailMapper.toEntity(request);

    assertNotNull(cocktail);
    assertEquals("Mojito", cocktail.getName());
    assertEquals("A classic", cocktail.getDescription());
    assertTrue(cocktail.isAvailable());
  }

  @Test
  @DisplayName("updateEntity should update existing Cocktail")
  void testUpdateEntity() {
    CocktailRequest request = new CocktailRequest(
        "Updated Mojito",
        "Updated desc",
        false,
        1L,
        List.of(),
        "",
        List.of()
    );

    CocktailMapper.updateEntity(request, mockCocktail);

    assertEquals("Updated Mojito", mockCocktail.getName());
    assertEquals("Updated desc", mockCocktail.getDescription());
    assertFalse(mockCocktail.isAvailable());
  }

  @Test
  @DisplayName("updateSizes should update cocktail sizes")
  void testUpdateSizes() {
    List<SizePriceRequest> sizes = List.of(
        new SizePriceRequest(Size.S, new BigDecimal("5.00")),
        new SizePriceRequest(Size.M, new BigDecimal("7.00"))
    );

    CocktailMapper.updateSizes(mockCocktail, sizes);

    assertNotNull(mockCocktail.getSizes());
    assertEquals(2, mockCocktail.getSizes().size());
  }

  @Test
  @DisplayName("toResponse should include ingredients with measure")
  void testToResponseWithIngredients() {
    fr.lebarapp.api.domain.Ingredient ing = new fr.lebarapp.api.domain.Ingredient();
    ing.setId(2L);
    ing.setName("Rum");

    CocktailIngredient ci = new CocktailIngredient();
    ci.setIngredient(ing);
    ci.setMeasure("45ml");
    mockCocktail.getIngredients().add(ci);

    CocktailResponse response = CocktailMapper.toResponse(mockCocktail);

    assertNotNull(response.ingredients());
    assertEquals(1, response.ingredients().size());
    assertEquals("Rum", response.ingredients().get(0).name());
    assertEquals("45ml", response.ingredients().get(0).measure());
  }
}
