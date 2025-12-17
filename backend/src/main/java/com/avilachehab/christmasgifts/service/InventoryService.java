package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.dto.InventorySummaryDto;
import com.avilachehab.christmasgifts.dto.RoasterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final CoffeeService coffeeService;
    private final RoasterService roasterService;
    
    public InventorySummaryDto getInventorySummary() {
        List<CoffeeDto> allCoffees = coffeeService.getAllCoffees();
        List<RoasterDto> allRoasters = roasterService.getAllRoasters();
        
        // Calculate total weight
        BigDecimal totalWeight = allCoffees.stream()
                .map(CoffeeDto::getCurrentWeight)
                .filter(w -> w != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total spent
        BigDecimal totalSpent = allCoffees.stream()
                .map(CoffeeDto::getPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate average price per gram
        BigDecimal averagePricePerGram = BigDecimal.ZERO;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0 && totalSpent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalInitialWeight = allCoffees.stream()
                    .map(CoffeeDto::getInitialWeight)
                    .filter(w -> w != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalInitialWeight.compareTo(BigDecimal.ZERO) > 0) {
                averagePricePerGram = totalSpent.divide(totalInitialWeight, 4, RoundingMode.HALF_UP);
            }
        }
        
        // Find low stock coffees (< 20% remaining)
        List<CoffeeDto> lowStockCoffees = allCoffees.stream()
                .filter(c -> c.getPercentageRemaining() != null 
                    && c.getPercentageRemaining().compareTo(BigDecimal.valueOf(20)) < 0
                    && c.getCurrentWeight().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        
        // Find aging coffees (> 30 days since roast)
        List<CoffeeDto> agingCoffees = allCoffees.stream()
                .filter(c -> c.getDaysSinceRoast() != null 
                    && c.getDaysSinceRoast() > 30
                    && c.getCurrentWeight().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        
        InventorySummaryDto summary = new InventorySummaryDto();
        summary.setTotalWeight(totalWeight);
        summary.setTotalBags(allCoffees.size());
        summary.setAveragePricePerGram(averagePricePerGram);
        summary.setTotalSpent(totalSpent);
        summary.setLowStockCoffees(lowStockCoffees);
        summary.setAgingCoffees(agingCoffees);
        summary.setRoasters(allRoasters);
        
        return summary;
    }
}

