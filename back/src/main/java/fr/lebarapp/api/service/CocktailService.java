package fr.lebarapp.api.service;

import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.CocktailIngredient;
import fr.lebarapp.api.domain.CocktailImage;
import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.dto.CocktailIngredientRequest;
import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.external.TheCocktailDbClient;
import fr.lebarapp.api.mapper.CocktailMapper;
import fr.lebarapp.api.repository.CategoryRepository;
import fr.lebarapp.api.repository.CocktailImageRepository;
import fr.lebarapp.api.repository.CocktailRepository;
import fr.lebarapp.api.repository.IngredientRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CocktailService {

    private static final Logger logger = LoggerFactory.getLogger(CocktailService.class);

    private final CocktailRepository cocktailRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final CocktailImageRepository cocktailImageRepository;
    private final TheCocktailDbClient theCocktailDbClient;

    public CocktailService(
        CocktailRepository cocktailRepository,
        CategoryRepository categoryRepository,
        IngredientRepository ingredientRepository,
        CocktailImageRepository cocktailImageRepository,
        TheCocktailDbClient theCocktailDbClient) {
        this.cocktailRepository = cocktailRepository;
        this.categoryRepository = categoryRepository;
        this.ingredientRepository = ingredientRepository;
        this.cocktailImageRepository = cocktailImageRepository;
        this.theCocktailDbClient = theCocktailDbClient;
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

        // Résoudre et ajouter les ingrédients
        if (request.ingredients() != null && !request.ingredients().isEmpty()) {
            for (CocktailIngredientRequest ingredientReq : request.ingredients()) {
                Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(ingredientReq.name())
                    .orElseGet(() -> {
                        Ingredient newIng = new Ingredient();
                        newIng.setName(ingredientReq.name());
                        return ingredientRepository.save(newIng);
                    });

                CocktailIngredient ci = new CocktailIngredient();
                ci.setCocktail(cocktail);
                ci.setIngredient(ingredient);
                ci.setMeasure(ingredientReq.measure());
                cocktail.getIngredients().add(ci);
            }
        }

        // Ajouter les tailles/prix
        CocktailMapper.updateSizes(cocktail, request.sizes());

        cocktail = cocktailRepository.save(cocktail);

        // Télécharger l'image si fournie
        if (request.imageSourceUrl() != null && !request.imageSourceUrl().isEmpty()) {
            downloadAndSaveImage(cocktail, request.imageSourceUrl());
        }

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
        cocktail.getIngredients().clear();
        if (request.ingredients() != null && !request.ingredients().isEmpty()) {
            for (CocktailIngredientRequest ingredientReq : request.ingredients()) {
                Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(ingredientReq.name())
                    .orElseGet(() -> {
                        Ingredient newIng = new Ingredient();
                        newIng.setName(ingredientReq.name());
                        return ingredientRepository.save(newIng);
                    });

                CocktailIngredient ci = new CocktailIngredient();
                ci.setCocktail(cocktail);
                ci.setIngredient(ingredient);
                ci.setMeasure(ingredientReq.measure());
                cocktail.getIngredients().add(ci);
            }
        }

        // Mettre à jour les tailles/prix (orphanRemoval gère la suppression)
        CocktailMapper.updateSizes(cocktail, request.sizes());

        cocktail = cocktailRepository.save(cocktail);

        // Mettre à jour l'image si fournie
        if (request.imageSourceUrl() != null && !request.imageSourceUrl().isEmpty()) {
            downloadAndSaveImage(cocktail, request.imageSourceUrl());
        }

        return CocktailMapper.toResponse(cocktail);
    }

    @Transactional
    public void deleteCocktail(Long id) {
        Cocktail cocktail = cocktailRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cocktail introuvable avec l'ID: " + id));
        cocktailRepository.delete(cocktail);
    }

    private void downloadAndSaveImage(Cocktail cocktail, String imageUrl) {
        try {
            var imageData = theCocktailDbClient.downloadImage(imageUrl);
            CocktailImage image = cocktail.getImage();
            if (image == null) {
                image = new CocktailImage();
                image.setCocktail(cocktail);
                cocktail.setImage(image);
            }
            image.setData(imageData.data());
            image.setContentType(imageData.contentType());
            cocktailImageRepository.save(image);
        } catch (Exception e) {
            logger.warn("Impossible de télécharger l'image pour le cocktail " + cocktail.getId() + ": " + e.getMessage());
        }
    }
}
