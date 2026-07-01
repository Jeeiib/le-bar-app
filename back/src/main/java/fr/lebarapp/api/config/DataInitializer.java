package fr.lebarapp.api.config;

import fr.lebarapp.api.domain.BarTable;
import fr.lebarapp.api.domain.Category;
import fr.lebarapp.api.domain.Cocktail;
import fr.lebarapp.api.domain.CocktailIngredient;
import fr.lebarapp.api.domain.CocktailImage;
import fr.lebarapp.api.domain.CocktailSize;
import fr.lebarapp.api.domain.Size;
import fr.lebarapp.api.domain.User;
import fr.lebarapp.api.domain.UserRole;
import fr.lebarapp.api.external.ExternalCocktailDto;
import fr.lebarapp.api.external.ExternalIngredientDto;
import fr.lebarapp.api.external.TheCocktailDbClient;
import fr.lebarapp.api.repository.BarTableRepository;
import fr.lebarapp.api.repository.CategoryRepository;
import fr.lebarapp.api.repository.CocktailImageRepository;
import fr.lebarapp.api.repository.CocktailRepository;
import fr.lebarapp.api.repository.IngredientRepository;
import fr.lebarapp.api.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    private final UserRepository userRepository;
    private final BarTableRepository barTableRepository;
    private final CategoryRepository categoryRepository;
    private final CocktailRepository cocktailRepository;
    private final IngredientRepository ingredientRepository;
    private final CocktailImageRepository cocktailImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final TheCocktailDbClient theCocktailDbClient;

    public DataInitializer(
        UserRepository userRepository,
        BarTableRepository barTableRepository,
        CategoryRepository categoryRepository,
        CocktailRepository cocktailRepository,
        IngredientRepository ingredientRepository,
        CocktailImageRepository cocktailImageRepository,
        PasswordEncoder passwordEncoder,
        TheCocktailDbClient theCocktailDbClient) {
        this.userRepository = userRepository;
        this.barTableRepository = barTableRepository;
        this.categoryRepository = categoryRepository;
        this.cocktailRepository = cocktailRepository;
        this.ingredientRepository = ingredientRepository;
        this.cocktailImageRepository = cocktailImageRepository;
        this.passwordEncoder = passwordEncoder;
        this.theCocktailDbClient = theCocktailDbClient;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled || userRepository.count() > 0) {
            return;
        }

        logger.info("Initialisation des données de seed...");

        try {
            // Créer barmaker
            User barmaker = new User();
            barmaker.setEmail("barmaker@lebarapp.fr");
            barmaker.setName("Barmaker");
            barmaker.setPasswordHash(passwordEncoder.encode("barmaker123"));
            barmaker.setRole(UserRole.BARMAKER);
            userRepository.save(barmaker);
            logger.info("Barmaker créé");

            // Créer 12 bar_table
            for (int i = 1; i <= 12; i++) {
                BarTable table = new BarTable();
                table.setLabel("Table " + i);
                table.setQrSlug("table-" + i);
                barTableRepository.save(table);
            }
            logger.info("12 tables de bar créées");

            // Créer 4 catégories
            String[] categoryNames = {"Signatures", "Classiques", "Sans alcool", "Softs"};
            for (String name : categoryNames) {
                Category cat = new Category();
                cat.setName(name);
                categoryRepository.save(cat);
            }
            logger.info("4 catégories créées");

            // Index des catégories par nom (évite de dépendre de l'ordre)
            Map<String, Category> catByName = new HashMap<>();
            for (Category c : categoryRepository.findAll()) {
                catByName.put(c.getName(), c);
            }

            // Cocktails chargés depuis TheCocktailDB (avec image).
            // Format : nom TheCocktailDB, catégorie, prix taille S (M = S+1.5, L = S+3).
            Object[][] apiCocktails = {
                {"Margarita", "Signatures", 6.5},
                {"Mojito", "Signatures", 6.0},
                {"Pina Colada", "Signatures", 7.0},
                {"Mai Tai", "Signatures", 7.5},
                {"Cosmopolitan", "Signatures", 7.0},
                {"Caipirinha", "Signatures", 6.5},
                {"Negroni", "Classiques", 7.5},
                {"Old Fashioned", "Classiques", 8.0},
                {"Manhattan", "Classiques", 8.0},
                {"Daiquiri", "Classiques", 6.0},
                {"Whiskey Sour", "Classiques", 6.5},
                {"Moscow Mule", "Classiques", 6.5},
                {"Bora Bora", "Sans alcool", 4.5},
                {"Cranberry Punch", "Sans alcool", 4.5},
                {"Fruit Cooler", "Sans alcool", 4.0},
            };

            for (Object[] spec : apiCocktails) {
                String cocktailName = (String) spec[0];
                String categoryName = (String) spec[1];
                double priceS = (double) spec[2];
                try {
                    List<ExternalCocktailDto> results = theCocktailDbClient.search(cocktailName);
                    if (results.isEmpty()) {
                        logger.warn("Aucun résultat TheCocktailDB pour " + cocktailName);
                        continue;
                    }
                    ExternalCocktailDto ext = results.get(0);
                    Cocktail cocktail = new Cocktail();
                    cocktail.setName(ext.name());
                    cocktail.setDescription(ext.instructions());
                    cocktail.setAvailable(true);
                    cocktail.setCategory(catByName.get(categoryName));

                    for (ExternalIngredientDto extIng : ext.ingredients()) {
                        addIngredient(cocktail, extIng.name(), extIng.measure());
                    }
                    addSizes(cocktail, priceS);
                    cocktail = cocktailRepository.save(cocktail);

                    if (ext.imageUrl() != null && !ext.imageUrl().isEmpty()) {
                        try {
                            var imageData = theCocktailDbClient.downloadImage(ext.imageUrl());
                            CocktailImage image = new CocktailImage();
                            image.setCocktail(cocktail);
                            image.setData(imageData.data());
                            image.setContentType(imageData.contentType());
                            cocktailImageRepository.save(image);
                        } catch (Exception e) {
                            logger.warn("Image indisponible pour " + cocktail.getName());
                        }
                    }
                    logger.info("Cocktail créé: " + cocktail.getName());
                } catch (Exception e) {
                    logger.warn("Erreur création " + cocktailName + ": " + e.getMessage());
                }
            }

            // Softs créés à la main (produits du commerce, sans image).
            // Format : nom, prix taille S, ingrédients.
            Object[][] softs = {
                {"Coca-Cola", 3.0, new String[]{"Cola"}},
                {"Limonade maison", 3.0, new String[]{"Citron", "Sucre", "Eau gazeuse"}},
                {"Jus d'orange", 3.5, new String[]{"Jus d'orange"}},
                {"Eau pétillante", 2.5, new String[]{"Eau gazeuse"}},
            };
            Category softsCat = catByName.get("Softs");
            for (Object[] spec : softs) {
                String name = (String) spec[0];
                double priceS = (double) spec[1];
                String[] ings = (String[]) spec[2];
                Cocktail soft = new Cocktail();
                soft.setName(name);
                soft.setAvailable(true);
                soft.setCategory(softsCat);
                for (String ingName : ings) {
                    addIngredient(soft, ingName, null);
                }
                addSizes(soft, priceS);
                cocktailRepository.save(soft);
                logger.info("Soft créé: " + name);
            }

            logger.info("Initialisation des données terminée");
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation des données: " + e.getMessage(), e);
        }
    }

    // Ajoute un ingrédient au cocktail (réutilise l'ingrédient s'il existe déjà).
    private void addIngredient(Cocktail cocktail, String name, String measure) {
        var ingredient = ingredientRepository.findByNameIgnoreCase(name)
            .orElseGet(() -> {
                var ing = new fr.lebarapp.api.domain.Ingredient();
                ing.setName(name);
                return ingredientRepository.save(ing);
            });
        CocktailIngredient ci = new CocktailIngredient();
        ci.setCocktail(cocktail);
        ci.setIngredient(ingredient);
        ci.setMeasure(measure);
        cocktail.getIngredients().add(ci);
    }

    // Ajoute les 3 tailles : S = priceS, M = priceS + 1.5, L = priceS + 3.
    private void addSizes(Cocktail cocktail, double priceS) {
        addSize(cocktail, Size.S, priceS);
        addSize(cocktail, Size.M, priceS + 1.5);
        addSize(cocktail, Size.L, priceS + 3.0);
    }

    private void addSize(Cocktail cocktail, Size size, double price) {
        CocktailSize cs = new CocktailSize();
        cs.setCocktail(cocktail);
        cs.setSize(size);
        cs.setPrice(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP));
        cocktail.getSizes().add(cs);
    }
}
