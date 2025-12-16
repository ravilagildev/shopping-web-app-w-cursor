package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.BudgetSummaryDto;
import com.avilachehab.christmasgifts.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {
    
    private final BudgetService budgetService;
    
    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryDto> getBudgetSummary(
            @RequestParam(defaultValue = "1000.00") BigDecimal totalBudget) {
        return ResponseEntity.ok(budgetService.getBudgetSummary(totalBudget));
    }
}

