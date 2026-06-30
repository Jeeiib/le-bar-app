package fr.lebarapp.api.mapper;

import static org.junit.jupiter.api.Assertions.*;

import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.dto.IngredientRequest;
import fr.lebarapp.api.dto.IngredientResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IngredientMapper Tests")
class IngredientMapperTest {

  @Test
  @DisplayName("toResponse should convert Ingredient to IngredientResponse")
  void testToResponse() {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Lime Juice");

    IngredientResponse response = IngredientMapper.toResponse(ingredient);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Lime Juice", response.name());
  }

  @Test
  @DisplayName("toEntity should convert IngredientRequest to Ingredient")
  void testToEntity() {
    IngredientRequest request = new IngredientRequest("Lime Juice");

    Ingredient ingredient = IngredientMapper.toEntity(request);

    assertNotNull(ingredient);
    assertEquals("Lime Juice", ingredient.getName());
    assertNull(ingredient.getId());
  }

  @Test
  @DisplayName("updateEntity should update existing Ingredient with IngredientRequest")
  void testUpdateEntity() {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(1L);
    ingredient.setName("Lime Juice");

    IngredientRequest request = new IngredientRequest("Updated Lime");
    IngredientMapper.updateEntity(request, ingredient);

    assertEquals("Updated Lime", ingredient.getName());
    assertEquals(1L, ingredient.getId());
  }
}
