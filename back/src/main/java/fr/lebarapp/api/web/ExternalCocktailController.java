package fr.lebarapp.api.web;

import fr.lebarapp.api.external.ExternalCocktailDto;
import fr.lebarapp.api.external.TheCocktailDbClient;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external")
public class ExternalCocktailController {

    private final TheCocktailDbClient theCocktailDbClient;

    public ExternalCocktailController(TheCocktailDbClient theCocktailDbClient) {
        this.theCocktailDbClient = theCocktailDbClient;
    }

    @GetMapping("/cocktails")
    public ResponseEntity<List<ExternalCocktailDto>> searchCocktails(@RequestParam String name) {
        List<ExternalCocktailDto> cocktails = theCocktailDbClient.search(name);
        return ResponseEntity.ok(cocktails);
    }
}
