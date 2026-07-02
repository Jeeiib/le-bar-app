package fr.lebarapp.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Lien entre un cocktail et un ingrédient, avec la mesure propre à la recette du bar
// (ex : "5 cl"). Un même ingrédient ne peut figurer qu'une fois par cocktail (contrainte unique).
@Entity
@Table(name = "cocktail_ingredients", uniqueConstraints = @UniqueConstraint(columnNames = {"cocktail_id", "ingredient_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CocktailIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cocktail_id")
    private Cocktail cocktail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    @Column(length = 60)
    private String measure;
}
