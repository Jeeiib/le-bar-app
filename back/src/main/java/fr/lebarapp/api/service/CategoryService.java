package fr.lebarapp.api.service;

import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.dto.CategoryRequest;
import fr.lebarapp.api.dto.CategoryResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.mapper.CategoryMapper;
import fr.lebarapp.api.repository.CategoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByPositionAsc().stream()
            .map(CategoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID: " + id));
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = CategoryMapper.toEntity(request);
        category = categoryRepository.save(category);
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID: " + id));
        CategoryMapper.updateEntity(request, category);
        category = categoryRepository.save(category);
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID: " + id));
        categoryRepository.delete(category);
    }
}
