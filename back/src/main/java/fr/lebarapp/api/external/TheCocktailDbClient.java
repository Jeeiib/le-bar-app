package fr.lebarapp.api.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

// Client de l'API publique TheCocktailDB, utilisé côté barmaker pour rechercher un cocktail et
// préremplir sa fiche. L'image est téléchargée puis stockée en base : au runtime, l'application
// ne dépend plus de cette API.
@Component
public class TheCocktailDbClient {

    private static final Logger logger = LoggerFactory.getLogger(TheCocktailDbClient.class);
    private static final String BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1";

    private final RestClient restClient;
    private final FrenchTranslator translator;

    public TheCocktailDbClient(RestClient.Builder restClientBuilder, FrenchTranslator translator) {
        this.restClient = restClientBuilder.baseUrl(BASE_URL).build();
        this.translator = translator;
    }

    public List<ExternalCocktailDto> search(String name) {
        try {
            var response = restClient.get()
                .uri("/search.php?s={name}", name)
                .retrieve()
                .body(Map.class);

            if (response == null || response.get("drinks") == null) {
                return new ArrayList<>();
            }

            List<Map<String, Object>> drinks = (List<Map<String, Object>>) response.get("drinks");
            List<ExternalCocktailDto> result = new ArrayList<>();

            for (Map<String, Object> drink : drinks) {
                String cocktailName = (String) drink.get("strDrink");
                String category = (String) drink.get("strCategory");
                // TheCocktailDB fournit déjà une recette en français (repli sur l'anglais)
                String instructions = (String) drink.get("strInstructionsFR");
                if (instructions == null || instructions.isBlank()) {
                    instructions = (String) drink.get("strInstructions");
                }
                String imageUrl = (String) drink.get("strDrinkThumb");

                List<ExternalIngredientDto> ingredients = new ArrayList<>();
                java.util.Set<String> seenIngredients = new java.util.HashSet<>();
                for (int i = 1; i <= 15; i++) {
                    String ingredientKey = "strIngredient" + i;
                    String measureKey = "strMeasure" + i;
                    String ingredientName = (String) drink.get(ingredientKey);
                    if (ingredientName != null && !ingredientName.trim().isEmpty()) {
                        String measure = (String) drink.get(measureKey);
                        // Normalisation FR (ingrédient + mesure), repli en VO si non couvert
                        String frName = translator.translateIngredient(ingredientName);
                        // Dédoublonnage : deux ingrédients EN peuvent se traduire pareil en FR
                        // (sinon violation de la contrainte unique cocktail+ingrédient en base)
                        if (frName != null && seenIngredients.add(frName.toLowerCase())) {
                            ingredients.add(new ExternalIngredientDto(
                                frName, translator.translateMeasure(measure)));
                        }
                    }
                }

                result.add(new ExternalCocktailDto(cocktailName, category, instructions, imageUrl, ingredients));
            }

            return result;
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de cocktails dans TheCocktailDB: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ImageData downloadImage(String url) throws Exception {
        try {
            byte[] imageData = restClient.get()
                .uri(url)
                .retrieve()
                .body(byte[].class);

            if (imageData == null) {
                throw new Exception("Image vide");
            }

            String contentType = "image/jpeg";
            if (url != null && url.toLowerCase().contains(".png")) {
                contentType = "image/png";
            }

            return new ImageData(imageData, contentType);
        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement de l'image: " + e.getMessage());
            throw e;
        }
    }

    public record ImageData(byte[] data, String contentType) {}
}
