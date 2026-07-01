package fr.lebarapp.api.external;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Traduction anglais -> français, utilisée uniquement à la création/import d'un cocktail.
 * Le résultat est ensuite stocké en français en base : le runtime client ne dépend
 * d'aucune API externe.
 *
 * Chaîne de repli : DeepL (si clé fournie, meilleure qualité) -> MyMemory (gratuit, sans clé)
 * -> texte d'origine. Un cache mémoire évite de retraduire un même texte.
 */
@Component
public class TranslationClient {

    private static final Logger logger = LoggerFactory.getLogger(TranslationClient.class);

    private final RestClient deepl;
    private final RestClient myMemory;
    private final String deeplKey;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TranslationClient(
        RestClient.Builder restClientBuilder,
        @Value("${app.deepl.key:}") String deeplKey) {
        this.deepl = restClientBuilder.clone().baseUrl("https://api-free.deepl.com").build();
        this.myMemory = restClientBuilder.clone().baseUrl("https://api.mymemory.translated.net").build();
        this.deeplKey = deeplKey;
    }

    public String translate(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        String cached = cache.get(text);
        if (cached != null) {
            return cached;
        }
        String result = tryDeepl(text);
        if (result == null) {
            result = tryMyMemory(text);
        }
        if (result == null) {
            return text; // repli VO, non mis en cache pour pouvoir réessayer plus tard
        }
        cache.put(text, result);
        return result;
    }

    private String tryDeepl(String text) {
        if (deeplKey == null || deeplKey.isBlank()) {
            return null;
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("text", text);
            form.add("source_lang", "EN");
            form.add("target_lang", "FR");

            @SuppressWarnings("unchecked")
            Map<String, Object> response = deepl.post()
                .uri("/v2/translate")
                .header("Authorization", "DeepL-Auth-Key " + deeplKey)
                .body(form)
                .retrieve()
                .body(Map.class);

            if (response != null && response.get("translations") instanceof List<?> translations
                && !translations.isEmpty()
                && translations.get(0) instanceof Map<?, ?> first
                && first.get("text") instanceof String t && !t.isBlank()) {
                return t;
            }
        } catch (Exception e) {
            logger.warn("DeepL indisponible pour \"{}\" : {}", text, e.getMessage());
        }
        return null;
    }

    private String tryMyMemory(String text) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = myMemory.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/get")
                    .queryParam("q", text)
                    .queryParam("langpair", "en|fr")
                    .build())
                .retrieve()
                .body(Map.class);

            if (response != null && response.get("responseData") instanceof Map<?, ?> data
                && data.get("translatedText") instanceof String result && !result.isBlank()
                && !result.contains("MYMEMORY WARNING") && !result.contains("QUERY LENGTH LIMIT")) {
                return unescapeHtml(result);
            }
        } catch (Exception e) {
            logger.warn("MyMemory indisponible pour \"{}\" : {}", text, e.getMessage());
        }
        return null;
    }

    // MyMemory échappe les apostrophes/guillemets en entités HTML.
    private String unescapeHtml(String s) {
        return s.replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">");
    }
}
