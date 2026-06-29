package fr.lebarapp.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.dto.CategoryRequest;
import fr.lebarapp.api.dto.CategoryResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.mapper.CategoryMapper;
import fr.lebarapp.api.repository.CategoryRepository;
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
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private CategoryService categoryService;

  private Category mockCategory;

  @BeforeEach
  void setUp() {
    mockCategory = new Category();
    mockCategory.setId(1L);
    mockCategory.setName("Spirits");
    mockCategory.setPosition(1);
  }

  @Test
  @DisplayName("getAllCategories should return all categories ordered by position")
  void testGetAllCategories_Success() {
    Category category2 = new Category();
    category2.setId(2L);
    category2.setName("Liqueurs");
    category2.setPosition(2);

    List<Category> categories = List.of(mockCategory, category2);

    when(categoryRepository.findAllByOrderByPositionAsc()).thenReturn(categories);

    List<CategoryResponse> response = categoryService.getAllCategories();

    assertNotNull(response);
    assertEquals(2, response.size());
    verify(categoryRepository).findAllByOrderByPositionAsc();
  }

  @Test
  @DisplayName("getCategoryById should return category when found")
  void testGetCategoryById_Success() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));

    CategoryResponse response = categoryService.getCategoryById(1L);

    assertNotNull(response);
    assertEquals("Spirits", response.name());
    verify(categoryRepository).findById(1L);
  }

  @Test
  @DisplayName("getCategoryById should throw ResourceNotFoundException when not found")
  void testGetCategoryById_NotFound() {
    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(999L));
  }

  @Test
  @DisplayName("createCategory should save and return new category")
  void testCreateCategory_Success() {
    CategoryRequest request = new CategoryRequest("Spirits", 1);

    when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

    CategoryResponse response = categoryService.createCategory(request);

    assertNotNull(response);
    assertEquals("Spirits", response.name());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  @DisplayName("updateCategory should update and return category")
  void testUpdateCategory_Success() {
    CategoryRequest request = new CategoryRequest("Updated Spirits", 2);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
    when(categoryRepository.save(any(Category.class))).thenReturn(mockCategory);

    CategoryResponse response = categoryService.updateCategory(1L, request);

    assertNotNull(response);
    verify(categoryRepository).findById(1L);
    verify(categoryRepository).save(mockCategory);
  }

  @Test
  @DisplayName("updateCategory should throw ResourceNotFoundException when category not found")
  void testUpdateCategory_NotFound() {
    CategoryRequest request = new CategoryRequest("Updated Name", 2);

    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> categoryService.updateCategory(999L, request));
  }

  @Test
  @DisplayName("deleteCategory should delete category")
  void testDeleteCategory_Success() {
    when(categoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));

    categoryService.deleteCategory(1L);

    verify(categoryRepository).findById(1L);
    verify(categoryRepository).delete(mockCategory);
  }

  @Test
  @DisplayName("deleteCategory should throw ResourceNotFoundException when not found")
  void testDeleteCategory_NotFound() {
    when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
        () -> categoryService.deleteCategory(999L));
  }
}
