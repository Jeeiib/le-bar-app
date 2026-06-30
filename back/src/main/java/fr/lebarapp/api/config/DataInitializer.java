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
import java.util.List;
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

            // Charger cocktails depuis TheCocktailDB
            String[] cocktailNames = {"Margarita", "Mojito", "Daiquiri", "Cosmopolitan", "Negroni",
                "Mai Tai", "Pina Colada", "Whiskey Sour", "Old Fashioned", "Moscow Mule", "Caipirinha", "Manhattan"};
            int createdCount = 0;

            for (String cocktailName : cocktailNames) {
                try {
                    List<ExternalCocktailDto> results = theCocktailDbClient.search(cocktailName);
                    if (!results.isEmpty()) {
                        ExternalCocktailDto externalCocktail = results.get(0);
                        Cocktail cocktail = new Cocktail();
                        cocktail.setName(externalCocktail.name());
                        cocktail.setDescription(externalCocktail.instructions());
                        cocktail.setAvailable(true);

                        // Assigner une catégorie
                        Category category = categoryRepository.findAll().get(createdCount % 2);
                        cocktail.setCategory(category);

                        // Ajouter les ingrédients
                        for (ExternalIngredientDto extIng : externalCocktail.ingredients()) {
                            var ingredient = ingredientRepository.findByNameIgnoreCase(extIng.name())
                                .orElseGet(() -> {
                                    var ing = new fr.lebarapp.api.domain.Ingredient();
                                    ing.setName(extIng.name());
                                    return ingredientRepository.save(ing);
                                });

                            CocktailIngredient ci = new CocktailIngredient();
                            ci.setCocktail(cocktail);
                            ci.setIngredient(ingredient);
                            ci.setMeasure(extIng.measure());
                            cocktail.getIngredients().add(ci);
                        }

                        // Ajouter les tailles/prix
                        CocktailSize sizeS = new CocktailSize();
                        sizeS.setCocktail(cocktail);
                        sizeS.setSize(Size.S);
                        sizeS.setPrice(new BigDecimal("5.50"));
                        cocktail.getSizes().add(sizeS);

                        CocktailSize sizeM = new CocktailSize();
                        sizeM.setCocktail(cocktail);
                        sizeM.setSize(Size.M);
                        sizeM.setPrice(new BigDecimal("7.50"));
                        cocktail.getSizes().add(sizeM);

                        CocktailSize sizeL = new CocktailSize();
                        sizeL.setCocktail(cocktail);
                        sizeL.setSize(Size.L);
                        sizeL.setPrice(new BigDecimal("9.50"));
                        cocktail.getSizes().add(sizeL);

                        cocktail = cocktailRepository.save(cocktail);

                        // Télécharger l'image
                        if (externalCocktail.imageUrl() != null && !externalCocktail.imageUrl().isEmpty()) {
                            try {
                                var imageData = theCocktailDbClient.downloadImage(externalCocktail.imageUrl());
                                CocktailImage image = new CocktailImage();
                                image.setCocktail(cocktail);
                                image.setData(imageData.data());
                                image.setContentType(imageData.contentType());
                                cocktailImageRepository.save(image);
                                logger.info("Image téléchargée pour " + cocktail.getName());
                            } catch (Exception e) {
                                logger.warn("Impossible de télécharger l'image pour " + cocktail.getName() + ": " + e.getMessage());
                            }
                        }

                        createdCount++;
                        logger.info("Cocktail créé: " + cocktail.getName());
                    }
                } catch (Exception e) {
                    logger.warn("Erreur lors de la création du cocktail " + cocktailName + ": " + e.getMessage());
                }
            }

            // Fallback: créer 2 cocktails manuels sans image si l'API n'a pas fourni assez
            if (createdCount < 2) {
                for (int i = createdCount; i < 2; i++) {
                    Cocktail cocktail = new Cocktail();
                    cocktail.setName("Cocktail " + (i + 1));
                    cocktail.setDescription("Cocktail créé par défaut");
                    cocktail.setAvailable(true);

                    Category category = categoryRepository.findAll().get(i % 4);
                    cocktail.setCategory(category);

                    CocktailSize size = new CocktailSize();
                    size.setCocktail(cocktail);
                    size.setSize(Size.M);
                    size.setPrice(new BigDecimal("8.00"));
                    cocktail.getSizes().add(size);

                    cocktailRepository.save(cocktail);
                    logger.info("Cocktail par défaut créé: " + cocktail.getName());
                }
            }

            logger.info("Initialisation des données terminée");
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation des données: " + e.getMessage(), e);
        }
    }
}
