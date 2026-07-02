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

    @Transactional(readOnly = true)
    public List<CocktailResponse> getAllCocktails() {
        return cocktailRepository.findAll().stream()
            .map(CocktailMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CocktailResponse> getCocktailsByCategory(Long categoryId) {
        return cocktailRepository.findByCategoryId(categoryId).stream()
            .map(CocktailMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
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

        // Appliquer les ingrédients et les tailles
        applyIngredients(cocktail, request.ingredients());
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

        // On vide ingrédients ET tailles puis on FLUSHE avant de réinsérer : sinon Hibernate
        // insère les nouvelles lignes avant de supprimer les anciennes et viole les contraintes
        // uniques (cocktail, ingrédient) et (cocktail, taille).
        cocktail.getIngredients().clear();
        cocktail.getSizes().clear();
        cocktailRepository.saveAndFlush(cocktail);

        // Appliquer les ingrédients et les tailles
        applyIngredients(cocktail, request.ingredients());
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
        try {
            cocktailRepository.delete(cocktail);
            cocktailRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Le cocktail est référencé par des commandes : on bloque proprement
            throw new fr.lebarapp.api.error.BusinessException(
                "Impossible de supprimer ce cocktail : il figure dans des commandes.");
        }
    }

    private void applyIngredients(Cocktail cocktail, List<CocktailIngredientRequest> ingredientRequests) {
        if (ingredientRequests != null && !ingredientRequests.isEmpty()) {
            java.util.Set<Long> seen = new java.util.HashSet<>();
            for (CocktailIngredientRequest ingredientReq : ingredientRequests) {
                Ingredient ingredient = ingredientRepository.findByNameIgnoreCase(ingredientReq.name())
                    .orElseGet(() -> {
                        Ingredient newIng = new Ingredient();
                        newIng.setName(ingredientReq.name());
                        return ingredientRepository.save(newIng);
                    });

                // Éviter un même ingrédient deux fois (contrainte unique cocktail+ingrédient)
                if (!seen.add(ingredient.getId())) {
                    continue;
                }

                CocktailIngredient ci = new CocktailIngredient();
                ci.setCocktail(cocktail);
                ci.setIngredient(ingredient);
                ci.setMeasure(ingredientReq.measure());
                cocktail.getIngredients().add(ci);
            }
        }
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
