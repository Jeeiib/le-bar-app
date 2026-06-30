package fr.lebarapp.api.web;

import fr.lebarapp.api.dto.CocktailRequest;
import fr.lebarapp.api.dto.CocktailResponse;
import fr.lebarapp.api.service.CocktailService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cocktails")
public class CocktailController {

    private final CocktailService cocktailService;

    public CocktailController(CocktailService cocktailService) {
        this.cocktailService = cocktailService;
    }

    @GetMapping
    public ResponseEntity<List<CocktailResponse>> getAllCocktails(
        @RequestParam(required = false) Long categoryId) {
        List<CocktailResponse> cocktails;
        if (categoryId != null) {
            cocktails = cocktailService.getCocktailsByCategory(categoryId);
        } else {
            cocktails = cocktailService.getAllCocktails();
        }
        return ResponseEntity.ok(cocktails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CocktailResponse> getCocktailById(@PathVariable Long id) {
        CocktailResponse cocktail = cocktailService.getCocktailById(id);
        return ResponseEntity.ok(cocktail);
    }

    @PostMapping
    public ResponseEntity<CocktailResponse> createCocktail(@Valid @RequestBody CocktailRequest request) {
        CocktailResponse cocktail = cocktailService.createCocktail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cocktail);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CocktailResponse> updateCocktail(
        @PathVariable Long id,
        @Valid @RequestBody CocktailRequest request) {
        CocktailResponse cocktail = cocktailService.updateCocktail(id, request);
        return ResponseEntity.ok(cocktail);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCocktail(@PathVariable Long id) {
        cocktailService.deleteCocktail(id);
        return ResponseEntity.noContent().build();
    }
}
