package fr.lebarapp.api.mapper;

import static org.junit.jupiter.api.Assertions.*;

import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.dto.CategoryRequest;
import fr.lebarapp.api.dto.CategoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CategoryMapper Tests")
class CategoryMapperTest {

  @Test
  @DisplayName("toResponse should convert Category to CategoryResponse")
  void testToResponse() {
    Category category = new Category();
    category.setId(1L);
    category.setName("Spirits");
    category.setPosition(1);

    CategoryResponse response = CategoryMapper.toResponse(category);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Spirits", response.name());
    assertEquals(1, response.position());
  }

  @Test
  @DisplayName("toEntity should convert CategoryRequest to Category")
  void testToEntity() {
    CategoryRequest request = new CategoryRequest("Spirits", 1);

    Category category = CategoryMapper.toEntity(request);

    assertNotNull(category);
    assertEquals("Spirits", category.getName());
    assertEquals(1, category.getPosition());
    assertNull(category.getId());
  }

  @Test
  @DisplayName("updateEntity should update existing Category with CategoryRequest")
  void testUpdateEntity() {
    Category category = new Category();
    category.setId(1L);
    category.setName("Spirits");
    category.setPosition(1);

    CategoryRequest request = new CategoryRequest("Updated Spirits", 2);
    CategoryMapper.updateEntity(request, category);

    assertEquals("Updated Spirits", category.getName());
    assertEquals(2, category.getPosition());
    assertEquals(1L, category.getId());
  }
}
