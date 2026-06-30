package fr.lebarapp.api.service;

import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.mapper.CocktailMapper;
import fr.lebarapp.api.repository.CategoryRepository;
import fr.lebarapp.api.repository.CocktailRepository;
import fr.lebarapp.api.repository.IngredientRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CocktailService {

    private final CocktailRepository cocktailRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;

    public CocktailService(
        CocktailRepository cocktailRepository,
        CategoryRepository categoryRepository,
        IngredientRepository ingredientRepository) {
        this.cocktailRepository = cocktailRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public List<CocktailResponse> getAllCocktails() {
        return cocktailRepository.findAll().stream()
            .map(CocktailMapper::toResponse)
            .collect(Collectors.toList());
    }

    public List<CocktailResponse> getCocktailsByCategory(Long categoryId) {
        return cocktailRepository.findByCategoryId(categoryId).stream()
            .map(CocktailMapper::toResponse)
            .collect(Collectors.toList());
    }

    public CocktailResponse getCocktailById(Long id) {
        Cocktail cocktail = cocktailRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cocktail introuvable avec l'ID: " + id));
        return CocktailMapper.toResponse(cocktail);
    }

    @Transactional
    public CocktailResponse createCocktail(CocktailRequest request) {
        Cocktail cocktail = CocktailMapper.toEntity(request);

        // Résoudre la catégorie
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID: " + request.categoryId()));
        cocktail.setCategory(category);

        // Résoudre les ingrédients
        Set<Ingredient> ingredients = new HashSet<>();
        if (request.ingredientIds() != null && !request.ingredientIds().isEmpty()) {
            ingredients = ingredientRepository.findAllById(request.ingredientIds())
                .stream()
                .collect(Collectors.toSet());
        }
        cocktail.setIngredients(ingredients);

        // Ajouter les tailles/prix
        CocktailMapper.updateSizes(cocktail, request.sizes());

        cocktail = cocktailRepository.save(cocktail);
        return CocktailMapper.toResponse(cocktail);
    }

    @Transactional
    public CocktailResponse updateCocktail(Long id, CocktailRequest request) {
        Cocktail cocktail = cocktailRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cocktail introuvable avec l'ID: " + id));

        CocktailMapper.updateEntity(request, cocktail);

        // Mettre à jour la catégorie
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'ID: " + request.categoryId()));
        cocktail.setCategory(category);

        // Mettre à jour les ingrédients
        Set<Ingredient> ingredients = new HashSet<>();
        if (request.ingredientIds() != null && !request.ingredientIds().isEmpty()) {
            ingredients = ingredientRepository.findAllById(request.ingredientIds())
                .stream()
                .collect(Collectors.toSet());
        }
        cocktail.setIngredients(ingredients);

        // Mettre à jour les tailles/prix (orphanRemoval gère la suppression)
        CocktailMapper.updateSizes(cocktail, request.sizes());

        cocktail = cocktailRepository.save(cocktail);
        return CocktailMapper.toResponse(cocktail);
    }

    @Transactional
    public void deleteCocktail(Long id) {
        Cocktail cocktail = cocktailRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cocktail introuvable avec l'ID: " + id));
        cocktailRepository.delete(cocktail);
    }
}
