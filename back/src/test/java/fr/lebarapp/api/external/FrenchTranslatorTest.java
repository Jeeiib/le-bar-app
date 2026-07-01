package fr.lebarapp.api.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

// translateMeasure n'utilise pas l'API de traduction : on peut tester avec un client null.
class FrenchTranslatorTest {

    private final FrenchTranslator translator = new FrenchTranslator(null);

    @Test
    void convertitLesOncesEnCentilitres() {
        assertEquals("4.5 cl", translator.translateMeasure("1 1/2 oz"));
        assertEquals("1.5 cl", translator.translateMeasure("1/2 oz"));
        assertEquals("3 cl", translator.translateMeasure("1 oz"));
        assertEquals("24 cl", translator.translateMeasure("8 oz"));
    }

    @Test
    void convertitLesPlagesDOnces() {
        assertEquals("6 à 9 cl", translator.translateMeasure("2-3 oz"));
    }

    @Test
    void gereLesOncesSuiviesDUnMot() {
        assertEquals("7.5 cl mélangé", translator.translateMeasure("2 1/2 oz Blended"));
    }

    @Test
    void normaliseLaCasseDesCentilitres() {
        assertEquals("4.5 cl", translator.translateMeasure("4.5 cL"));
    }

    @Test
    void traduitLesUnites() {
        assertEquals("2 traits", translator.translateMeasure("2 dashes"));
        assertEquals("1 morceau", translator.translateMeasure("1 cube"));
        assertEquals("Trait", translator.translateMeasure("dash"));
        assertEquals("2 c. à café", translator.translateMeasure("2 tsp"));
        assertEquals("½ tranche", translator.translateMeasure("1/2 slice"));
    }

    @Test
    void traduitLesJusDeFruit() {
        assertEquals("Jus d'1", translator.translateMeasure("Juice of 1"));
        assertEquals("Jus de ½", translator.translateMeasure("Juice of 1/2"));
    }

    @Test
    void traduitLesUnitesRares() {
        assertEquals("½ poignée", translator.translateMeasure("1/2 handful"));
        assertEquals("8 gouttes", translator.translateMeasure("8 drops"));
        assertEquals("1/8 L jamaïcain", translator.translateMeasure("1/8 L Jamaican"));
        assertEquals("1 pincée", translator.translateMeasure("1 pinch"));
        assertEquals("Selon le goût", translator.translateMeasure("To taste"));
    }

    @Test
    void traduitLesPlagesDeComptageEtConnecteurs() {
        assertEquals("2 à 4", translator.translateMeasure("2-4"));
        assertEquals("2 ou 3", translator.translateMeasure("2 or 3"));
    }

    @Test
    void renvoieValeurDOrigineSiVideOuNull() {
        assertNull(translator.translateMeasure(null));
        assertEquals("", translator.translateMeasure(""));
    }
}
