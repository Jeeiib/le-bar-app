package fr.lebarapp.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Variante de taille et prix pour un cocktail.
@Entity
@Table(name = "cocktail_sizes", uniqueConstraints = @UniqueConstraint(columnNames = {"cocktail_id", "size"}))
@Getter
@Setter
@NoArgsConstructor
public class CocktailSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cocktail_id")
    private Cocktail cocktail;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Size size;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal price;
}
