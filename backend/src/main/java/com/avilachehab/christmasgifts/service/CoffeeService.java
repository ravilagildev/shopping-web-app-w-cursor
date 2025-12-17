package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.model.Coffee;
import com.avilachehab.christmasgifts.model.Roaster;
import com.avilachehab.christmasgifts.repository.CoffeeRepository;
import com.avilachehab.christmasgifts.repository.RoasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoffeeService {
    
    private final CoffeeRepository coffeeRepository;
    private final RoasterRepository roasterRepository;
    
    public List<CoffeeDto> getAllCoffees() {
        return coffeeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public CoffeeDto getCoffeeById(Long id) {
        Coffee coffee = coffeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coffee not found with id: " + id));
        return convertToDto(coffee);
    }
    
    public List<CoffeeDto> getCoffeesByRoasterId(Long roasterId) {
        return coffeeRepository.findByRoasterId(roasterId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public CoffeeDto createCoffee(CoffeeDto coffeeDto) {
        Roaster roaster = roasterRepository.findById(coffeeDto.getRoasterId())
                .orElseThrow(() -> new RuntimeException("Roaster not found with id: " + coffeeDto.getRoasterId()));
        
        Coffee coffee = new Coffee();
        coffee.setCoffeeName(coffeeDto.getCoffeeName());
        coffee.setRoastDate(coffeeDto.getRoastDate());
        coffee.setPurchaseDate(coffeeDto.getPurchaseDate());
        coffee.setInitialWeight(coffeeDto.getInitialWeight());
        coffee.setCurrentWeight(coffeeDto.getCurrentWeight() != null ? coffeeDto.getCurrentWeight() : coffeeDto.getInitialWeight());
        coffee.setOrigin(coffeeDto.getOrigin());
        coffee.setRoastLevel(coffeeDto.getRoastLevel());
        coffee.setProcessingMethod(coffeeDto.getProcessingMethod());
        coffee.setPrice(coffeeDto.getPrice());
        coffee.setNotes(coffeeDto.getNotes());
        coffee.setRoaster(roaster);
        
        Coffee saved = coffeeRepository.save(coffee);
        return convertToDto(saved);
    }
    
    @Transactional
    public CoffeeDto updateCoffee(Long id, CoffeeDto coffeeDto) {
        Coffee coffee = coffeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coffee not found with id: " + id));
        
        coffee.setCoffeeName(coffeeDto.getCoffeeName());
        coffee.setRoastDate(coffeeDto.getRoastDate());
        coffee.setPurchaseDate(coffeeDto.getPurchaseDate());
        coffee.setInitialWeight(coffeeDto.getInitialWeight());
        coffee.setCurrentWeight(coffeeDto.getCurrentWeight());
        coffee.setOrigin(coffeeDto.getOrigin());
        coffee.setRoastLevel(coffeeDto.getRoastLevel());
        coffee.setProcessingMethod(coffeeDto.getProcessingMethod());
        coffee.setPrice(coffeeDto.getPrice());
        coffee.setNotes(coffeeDto.getNotes());
        
        if (!coffee.getRoaster().getId().equals(coffeeDto.getRoasterId())) {
            Roaster roaster = roasterRepository.findById(coffeeDto.getRoasterId())
                    .orElseThrow(() -> new RuntimeException("Roaster not found with id: " + coffeeDto.getRoasterId()));
            coffee.setRoaster(roaster);
        }
        
        Coffee saved = coffeeRepository.save(coffee);
        return convertToDto(saved);
    }
    
    @Transactional
    public CoffeeDto consumeCoffee(Long id, BigDecimal amount) {
        Coffee coffee = coffeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coffee not found with id: " + id));
        
        BigDecimal newWeight = coffee.getCurrentWeight().subtract(amount);
        if (newWeight.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Cannot consume more coffee than available");
        }
        coffee.setCurrentWeight(newWeight);
        
        Coffee saved = coffeeRepository.save(coffee);
        return convertToDto(saved);
    }
    
    @Transactional
    public void deleteCoffee(Long id) {
        if (!coffeeRepository.existsById(id)) {
            throw new RuntimeException("Coffee not found with id: " + id);
        }
        coffeeRepository.deleteById(id);
    }
    
    private CoffeeDto convertToDto(Coffee coffee) {
        CoffeeDto dto = new CoffeeDto();
        dto.setId(coffee.getId());
        dto.setCoffeeName(coffee.getCoffeeName());
        dto.setRoastDate(coffee.getRoastDate());
        dto.setPurchaseDate(coffee.getPurchaseDate());
        dto.setInitialWeight(coffee.getInitialWeight());
        dto.setCurrentWeight(coffee.getCurrentWeight());
        dto.setOrigin(coffee.getOrigin());
        dto.setRoastLevel(coffee.getRoastLevel());
        dto.setProcessingMethod(coffee.getProcessingMethod());
        dto.setPrice(coffee.getPrice());
        dto.setNotes(coffee.getNotes());
        dto.setRoasterId(coffee.getRoaster().getId());
        dto.setRoasterName(coffee.getRoaster().getName());
        
        // Calculate days since roast
        if (coffee.getRoastDate() != null) {
            dto.setDaysSinceRoast(
                java.time.temporal.ChronoUnit.DAYS.between(
                    coffee.getRoastDate(), 
                    LocalDate.now()
                )
            );
        }
        
        // Calculate percentage remaining
        if (coffee.getInitialWeight() != null && coffee.getCurrentWeight() != null 
            && coffee.getInitialWeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percentage = coffee.getCurrentWeight()
                .divide(coffee.getInitialWeight(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
            dto.setPercentageRemaining(percentage);
        }
        
        return dto;
    }
}

