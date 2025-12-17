package com.avilachehab.christmasgifts.dto;

import com.avilachehab.christmasgifts.model.RoastLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeDto {
    private Long id;
    private String coffeeName;
    private LocalDate roastDate;
    private LocalDate purchaseDate;
    private BigDecimal initialWeight;
    private BigDecimal currentWeight;
    private String origin;
    private RoastLevel roastLevel;
    private String processingMethod;
    private BigDecimal price;
    private String notes;
    private Long roasterId;
    private String roasterName;
    private Long daysSinceRoast;
    private BigDecimal percentageRemaining;
}

