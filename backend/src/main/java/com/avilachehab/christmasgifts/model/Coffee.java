package com.avilachehab.christmasgifts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coffees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coffee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Coffee name is required")
    @Column(nullable = false)
    private String coffeeName;
    
    @NotNull(message = "Roast date is required")
    @Column(nullable = false)
    private LocalDate roastDate;
    
    @NotNull(message = "Purchase date is required")
    @Column(nullable = false)
    private LocalDate purchaseDate;
    
    @NotNull(message = "Initial weight is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial weight must be positive")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal initialWeight; // in grams
    
    @NotNull(message = "Current weight is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Current weight must be non-negative")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal currentWeight; // in grams
    
    @Column
    private String origin; // e.g., "Ethiopia, Yirgacheffe"
    
    @Enumerated(EnumType.STRING)
    @Column
    private RoastLevel roastLevel;
    
    @Column
    private String processingMethod; // Washed, Natural, Honey, etc.
    
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 2000)
    private String notes; // Tasting notes, rating, etc.
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roaster_id", nullable = false)
    private Roaster roaster;
}

