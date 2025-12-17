package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.dto.RoasterDto;
import com.avilachehab.christmasgifts.model.Coffee;
import com.avilachehab.christmasgifts.model.Roaster;
import com.avilachehab.christmasgifts.repository.RoasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoasterService {
    
    private final RoasterRepository roasterRepository;
    
    public List<RoasterDto> getAllRoasters() {
        return roasterRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public RoasterDto getRoasterById(Long id) {
        Roaster roaster = roasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roaster not found with id: " + id));
        return convertToDto(roaster);
    }
    
    @Transactional
    public RoasterDto createRoaster(RoasterDto roasterDto) {
        Roaster roaster = new Roaster();
        roaster.setName(roasterDto.getName());
        roaster.setLocation(roasterDto.getLocation());
        roaster.setWebsite(roasterDto.getWebsite());
        roaster.setNotes(roasterDto.getNotes());
        Roaster saved = roasterRepository.save(roaster);
        return convertToDto(saved);
    }
    
    @Transactional
    public RoasterDto updateRoaster(Long id, RoasterDto roasterDto) {
        Roaster roaster = roasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Roaster not found with id: " + id));
        roaster.setName(roasterDto.getName());
        roaster.setLocation(roasterDto.getLocation());
        roaster.setWebsite(roasterDto.getWebsite());
        roaster.setNotes(roasterDto.getNotes());
        Roaster saved = roasterRepository.save(roaster);
        return convertToDto(saved);
    }
    
    @Transactional
    public void deleteRoaster(Long id) {
        if (!roasterRepository.existsById(id)) {
            throw new RuntimeException("Roaster not found with id: " + id);
        }
        roasterRepository.deleteById(id);
    }
    
    private RoasterDto convertToDto(Roaster roaster) {
        RoasterDto dto = new RoasterDto();
        dto.setId(roaster.getId());
        dto.setName(roaster.getName());
        dto.setLocation(roaster.getLocation());
        dto.setWebsite(roaster.getWebsite());
        dto.setNotes(roaster.getNotes());
        
        BigDecimal totalSpent = roaster.getCoffees().stream()
                .filter(c -> c.getPrice() != null)
                .map(Coffee::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalSpent(totalSpent);
        dto.setCoffeeCount(roaster.getCoffees().size());
        
        List<CoffeeDto> coffeeDtos = roaster.getCoffees().stream()
                .map(coffee -> {
                    CoffeeDto coffeeDto = new CoffeeDto();
                    coffeeDto.setId(coffee.getId());
                    coffeeDto.setCoffeeName(coffee.getCoffeeName());
                    coffeeDto.setRoastDate(coffee.getRoastDate());
                    coffeeDto.setPurchaseDate(coffee.getPurchaseDate());
                    coffeeDto.setInitialWeight(coffee.getInitialWeight());
                    coffeeDto.setCurrentWeight(coffee.getCurrentWeight());
                    coffeeDto.setOrigin(coffee.getOrigin());
                    coffeeDto.setRoastLevel(coffee.getRoastLevel());
                    coffeeDto.setProcessingMethod(coffee.getProcessingMethod());
                    coffeeDto.setPrice(coffee.getPrice());
                    coffeeDto.setNotes(coffee.getNotes());
                    coffeeDto.setRoasterId(roaster.getId());
                    coffeeDto.setRoasterName(roaster.getName());
                    
                    // Calculate days since roast
                    if (coffee.getRoastDate() != null) {
                        coffeeDto.setDaysSinceRoast(
                            java.time.temporal.ChronoUnit.DAYS.between(
                                coffee.getRoastDate(), 
                                java.time.LocalDate.now()
                            )
                        );
                    }
                    
                    // Calculate percentage remaining
                    if (coffee.getInitialWeight() != null && coffee.getCurrentWeight() != null 
                        && coffee.getInitialWeight().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal percentage = coffee.getCurrentWeight()
                            .divide(coffee.getInitialWeight(), 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                        coffeeDto.setPercentageRemaining(percentage);
                    }
                    
                    return coffeeDto;
                })
                .collect(Collectors.toList());
        dto.setCoffees(coffeeDtos);
        
        return dto;
    }
}

