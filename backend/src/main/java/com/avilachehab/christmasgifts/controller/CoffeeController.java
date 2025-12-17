package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.service.CoffeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coffees")
@RequiredArgsConstructor
public class CoffeeController {
    
    private final CoffeeService coffeeService;
    
    @GetMapping
    public ResponseEntity<List<CoffeeDto>> getAllCoffees() {
        return ResponseEntity.ok(coffeeService.getAllCoffees());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CoffeeDto> getCoffeeById(@PathVariable Long id) {
        return ResponseEntity.ok(coffeeService.getCoffeeById(id));
    }
    
    @GetMapping("/roaster/{roasterId}")
    public ResponseEntity<List<CoffeeDto>> getCoffeesByRoasterId(@PathVariable Long roasterId) {
        return ResponseEntity.ok(coffeeService.getCoffeesByRoasterId(roasterId));
    }
    
    @PostMapping
    public ResponseEntity<CoffeeDto> createCoffee(@Valid @RequestBody CoffeeDto coffeeDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coffeeService.createCoffee(coffeeDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CoffeeDto> updateCoffee(@PathVariable Long id, 
                                                   @Valid @RequestBody CoffeeDto coffeeDto) {
        return ResponseEntity.ok(coffeeService.updateCoffee(id, coffeeDto));
    }
    
    @PostMapping("/{id}/consume")
    public ResponseEntity<CoffeeDto> consumeCoffee(@PathVariable Long id, 
                                                    @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(coffeeService.consumeCoffee(id, amount));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoffee(@PathVariable Long id) {
        coffeeService.deleteCoffee(id);
        return ResponseEntity.noContent().build();
    }
}

