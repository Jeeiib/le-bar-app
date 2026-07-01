package fr.lebarapp.api.external;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Mise en français des données TheCocktailDB (anglais).
 * - Ingrédients et recette : traduits via l'API ({@link TranslationClient}).
 * - Mesures : normalisées en interne (les API de traduction ne convertissent pas les unités) :
 *   onces -> cl (1 oz ≈ 3 cl), fractions en ½/¼/¾, plages "X à Y", traduction des mots d'unité.
 */
@Component
public class FrenchTranslator {

    // Traduction des mots d'unité / connecteurs courants (clé en minuscules).
    private static final Map<String, String> WORDS = Map.ofEntries(
        Map.entry("tsp", "c. à café"),
        Map.entry("tspn", "c. à café"),
        Map.entry("tblsp", "c. à soupe"),
        Map.entry("tbsp", "c. à soupe"),
        Map.entry("tbs", "c. à soupe"),
        Map.entry("dash", "trait"),
        Map.entry("dashes", "traits"),
        Map.entry("shot", "dose"),
        Map.entry("shots", "doses"),
        Map.entry("splash", "giclée"),
        Map.entry("cup", "tasse"),
        Map.entry("cups", "tasses"),
        Map.entry("part", "part"),
        Map.entry("parts", "parts"),
        Map.entry("cube", "morceau"),
        Map.entry("cubes", "morceaux"),
        Map.entry("slice", "tranche"),
        Map.entry("slices", "tranches"),
        Map.entry("leaves", "feuilles"),
        Map.entry("leaf", "feuille"),
        Map.entry("wedge", "quartier"),
        Map.entry("wedges", "quartiers"),
        Map.entry("twist", "zeste"),
        Map.entry("whole", "entier"),
        Map.entry("fresh", "frais"),
        Map.entry("chilled", "frais"),
        Map.entry("blended", "mélangé"),
        Map.entry("blend", "mélangé"),
        Map.entry("juice", "jus"),
        Map.entry("of", "de"),
        Map.entry("or", "ou"),
        Map.entry("fill", "compléter"),
        Map.entry("top", "compléter"),
        Map.entry("with", "avec"),
        Map.entry("garnish", "décoration"),
        Map.entry("handful", "poignée"),
        Map.entry("handfuls", "poignées"),
        Map.entry("drop", "goutte"),
        Map.entry("drops", "gouttes"),
        Map.entry("pinch", "pincée"),
        Map.entry("pinches", "pincées"),
        Map.entry("sprig", "brin"),
        Map.entry("sprigs", "brins"),
        Map.entry("scoop", "boule"),
        Map.entry("scoops", "boules"),
        Map.entry("glass", "verre"),
        Map.entry("glasses", "verres"),
        Map.entry("can", "canette"),
        Map.entry("bottle", "bouteille"),
        Map.entry("bunch", "bouquet"),
        Map.entry("stick", "bâton"),
        Map.entry("sticks", "bâtons"),
        Map.entry("jamaican", "jamaïcain"),
        Map.entry("crushed", "pilé"),
        Map.entry("ripe", "mûr"),
        Map.entry("peeled", "pelé"),
        Map.entry("large", "grand"),
        Map.entry("small", "petit"),
        Map.entry("medium", "moyen")
    );

    private static final double CL_PER_OZ = 3.0;
    // Capture le nombre (entier, décimal, fraction, mixte ou plage) précédant "oz".
    private static final Pattern OZ_PATTERN =
        Pattern.compile("(?i)([0-9][0-9 /.\\-]*?)\\s*oz\\b");

    private final TranslationClient translationClient;

    public FrenchTranslator(TranslationClient translationClient) {
        this.translationClient = translationClient;
    }

    // Nom d'ingrédient : traduit via l'API, première lettre en majuscule.
    public String translateIngredient(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        return capitalize(translationClient.translate(name.trim()));
    }

    /**
     * Normalise une mesure :
     * "1 1/2 oz" -> "4.5 cl", "2-3 oz" -> "6 à 9 cl", "Juice of 1/2" -> "Jus de ½",
     * "2 dashes" -> "2 traits". Renvoie la valeur d'origine si rien ne matche.
     */
    public String translateMeasure(String measure) {
        if (measure == null || measure.trim().isEmpty()) {
            return measure;
        }
        String m = measure.trim();

        // "Juice of 1" -> "Jus d'1" (mais pas "Juice of 1/2") ; "Juice of X" -> "Jus de X"
        m = m.replaceAll("(?i)\\bjuice of 1(?![/0-9])", "Jus d'1");
        m = m.replaceAll("(?i)\\bjuice of\\b", "Jus de");
        m = m.replaceAll("(?i)\\bto taste\\b", "selon le goût");

        // Onces -> cl (gère fractions, mixtes et plages)
        m = convertOuncesToCl(m);

        // Normaliser la casse de cl/ml
        m = m.replaceAll("(?i)\\bcl\\b", "cl").replaceAll("(?i)\\bml\\b", "ml");

        // Traduire les mots restants + jolies fractions + plages "X à Y"
        return capitalize(translateWords(m));
    }

    private String convertOuncesToCl(String text) {
        Matcher matcher = OZ_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String cl = ouncesToCl(matcher.group(1).trim());
            String replacement = (cl != null) ? cl + " cl" : matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // Convertit une expression d'onces en cl. Gère les plages "2-3" -> "6 à 9".
    private String ouncesToCl(String amount) {
        if (amount.matches(".*\\d\\s*-\\s*\\d.*")) {
            String[] parts = amount.split("\\s*-\\s*");
            Double a = parseAmount(parts[0]);
            Double b = parseAmount(parts[1]);
            return (a != null && b != null) ? format(a * CL_PER_OZ) + " à " + format(b * CL_PER_OZ) : null;
        }
        Double a = parseAmount(amount);
        return (a != null) ? format(a * CL_PER_OZ) : null;
    }

    private String translateWords(String text) {
        String[] tokens = text.split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String token : tokens) {
            String key = token.toLowerCase().replaceAll("[.,]$", "");
            String fr = WORDS.get(key);
            if (fr != null) {
                out.append(fr);
            } else if (token.matches("\\d+-\\d+")) {
                out.append(token.replace("-", " à ")); // plage de comptage : "2-4" -> "2 à 4"
            } else {
                out.append(prettyFraction(token));
            }
            out.append(' ');
        }
        return out.toString().trim();
    }

    private String prettyFraction(String token) {
        return token.replace("1/2", "½").replace("1/4", "¼").replace("3/4", "¾");
    }

    private String format(double cl) {
        double rounded = Math.round(cl * 10.0) / 10.0;
        return (rounded == Math.floor(rounded)) ? String.valueOf((int) rounded) : String.valueOf(rounded);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // Parse "1", "1.5", "1/2" ou "1 1/2" en nombre décimal.
    private Double parseAmount(String raw) {
        try {
            raw = raw.trim();
            if (raw.contains(" ")) {
                String[] parts = raw.split("\\s+");
                return Double.parseDouble(parts[0]) + parseFraction(parts[1]);
            }
            if (raw.contains("/")) {
                return parseFraction(raw);
            }
            return Double.parseDouble(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private double parseFraction(String frac) {
        String[] parts = frac.split("/");
        return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
    }
}
