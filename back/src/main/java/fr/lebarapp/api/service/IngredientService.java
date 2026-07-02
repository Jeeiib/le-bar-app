package fr.lebarapp.api.service;

import fr.lebarapp.api.domain.Ingredient;
import fr.lebarapp.api.dto.IngredientRequest;
import fr.lebarapp.api.dto.IngredientResponse;
import fr.lebarapp.api.error.ResourceNotFoundException;
import fr.lebarapp.api.mapper.IngredientMapper;
import fr.lebarapp.api.repository.IngredientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Logique métier des ingrédients : CRUD, avec suppression bloquée si l'ingrédient est encore
// utilisé dans des cocktails.
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll().stream()
            .map(IngredientMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable avec l'ID: " + id));
        return IngredientMapper.toResponse(ingredient);
    }

    @Transactional
    public IngredientResponse createIngredient(IngredientRequest request) {
        Ingredient ingredient = IngredientMapper.toEntity(request);
        ingredient = ingredientRepository.save(ingredient);
        return IngredientMapper.toResponse(ingredient);
    }

    @Transactional
    public IngredientResponse updateIngredient(Long id, IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable avec l'ID: " + id));
        IngredientMapper.updateEntity(request, ingredient);
        ingredient = ingredientRepository.save(ingredient);
        return IngredientMapper.toResponse(ingredient);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ingrédient introuvable avec l'ID: " + id));
        try {
            ingredientRepository.delete(ingredient);
            ingredientRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // L'ingrédient est référencé par des cocktails : on bloque proprement
            throw new fr.lebarapp.api.error.BusinessException(
                "Impossible de supprimer cet ingrédient : il figure dans des cocktails.");
        }
    }
}
