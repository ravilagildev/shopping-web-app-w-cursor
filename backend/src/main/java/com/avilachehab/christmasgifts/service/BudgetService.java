package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.BudgetSummaryDto;
import com.avilachehab.christmasgifts.dto.PersonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    
    private final PersonService personService;
    
    public BudgetSummaryDto getBudgetSummary(BigDecimal totalBudget) {
        List<PersonDto> persons = personService.getAllPersons();
        
        BigDecimal totalSpent = persons.stream()
                .map(PersonDto::getTotalSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal remaining = totalBudget.subtract(totalSpent);
        
        BudgetSummaryDto summary = new BudgetSummaryDto();
        summary.setTotalBudget(totalBudget);
        summary.setTotalSpent(totalSpent);
        summary.setRemaining(remaining);
        summary.setPersons(persons);
        
        return summary;
    }
}

