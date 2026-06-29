package fr.lebarapp.api.external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TheCocktailDbClient {

    private static final Logger logger = LoggerFactory.getLogger(TheCocktailDbClient.class);
    private static final String BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1";

    private final RestClient restClient;

    public TheCocktailDbClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(BASE_URL).build();
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
                String instructions = (String) drink.get("strInstructions");
                String imageUrl = (String) drink.get("strDrinkThumb");

                List<ExternalIngredientDto> ingredients = new ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                    String ingredientKey = "strIngredient" + i;
                    String measureKey = "strMeasure" + i;
                    String ingredientName = (String) drink.get(ingredientKey);
                    if (ingredientName != null && !ingredientName.trim().isEmpty()) {
                        String measure = (String) drink.get(measureKey);
                        ingredients.add(new ExternalIngredientDto(ingredientName, measure));
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
