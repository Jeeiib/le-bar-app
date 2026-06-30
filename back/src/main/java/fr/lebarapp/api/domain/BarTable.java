package fr.lebarapp.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Table du bar, identifiée par un code QR.
@Entity
@Table(name = "bar_table")
@Getter
@Setter
@NoArgsConstructor
public class BarTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String label;

    @Column(nullable = false, unique = true, length = 60)
    private String qrSlug;
}
