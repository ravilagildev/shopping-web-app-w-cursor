package com.avilachehab.christmasgifts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventorySummaryDto {
    private BigDecimal totalWeight; // total grams of coffee
    private Integer totalBags;
    private BigDecimal averagePricePerGram;
    private BigDecimal totalSpent;
    private List<CoffeeDto> lowStockCoffees; // coffees with < 20% remaining
    private List<CoffeeDto> agingCoffees; // coffees older than 30 days since roast
    private List<RoasterDto> roasters;
}

