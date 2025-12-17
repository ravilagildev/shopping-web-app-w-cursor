package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.model.Coffee;
import com.avilachehab.christmasgifts.model.Roaster;
import com.avilachehab.christmasgifts.model.RoastLevel;
import com.avilachehab.christmasgifts.repository.CoffeeRepository;
import com.avilachehab.christmasgifts.repository.RoasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoffeeServiceTest {

    @Mock
    private CoffeeRepository coffeeRepository;

    @Mock
    private RoasterRepository roasterRepository;

    @InjectMocks
    private CoffeeService coffeeService;

    private Roaster testRoaster;
    private Coffee testCoffee;
    private CoffeeDto testCoffeeDto;

    @BeforeEach
    void setUp() {
        testRoaster = new Roaster();
        testRoaster.setId(1L);
        testRoaster.setName("Blue Bottle");

        testCoffee = new Coffee();
        testCoffee.setId(1L);
        testCoffee.setCoffeeName("Ethiopian Yirgacheffe");
        testCoffee.setRoastDate(LocalDate.now().minusDays(5));
        testCoffee.setPurchaseDate(LocalDate.now().minusDays(3));
        testCoffee.setInitialWeight(BigDecimal.valueOf(250));
        testCoffee.setCurrentWeight(BigDecimal.valueOf(200));
        testCoffee.setOrigin("Ethiopia, Yirgacheffe");
        testCoffee.setRoastLevel(RoastLevel.LIGHT);
        testCoffee.setProcessingMethod("Washed");
        testCoffee.setPrice(BigDecimal.valueOf(18.50));
        testCoffee.setNotes("Fruity and bright");
        testCoffee.setRoaster(testRoaster);

        testCoffeeDto = new CoffeeDto();
        testCoffeeDto.setCoffeeName("Ethiopian Yirgacheffe");
        testCoffeeDto.setRoastDate(LocalDate.now().minusDays(5));
        testCoffeeDto.setPurchaseDate(LocalDate.now().minusDays(3));
        testCoffeeDto.setInitialWeight(BigDecimal.valueOf(250));
        testCoffeeDto.setCurrentWeight(BigDecimal.valueOf(200));
        testCoffeeDto.setOrigin("Ethiopia, Yirgacheffe");
        testCoffeeDto.setRoastLevel(RoastLevel.LIGHT);
        testCoffeeDto.setProcessingMethod("Washed");
        testCoffeeDto.setPrice(BigDecimal.valueOf(18.50));
        testCoffeeDto.setNotes("Fruity and bright");
        testCoffeeDto.setRoasterId(1L);
    }

    @Test
    void getAllCoffees_ShouldReturnAllCoffees() {
        // Given
        Coffee coffee2 = new Coffee();
        coffee2.setId(2L);
        coffee2.setCoffeeName("Colombian");
        coffee2.setRoaster(testRoaster);

        when(coffeeRepository.findAll()).thenReturn(Arrays.asList(testCoffee, coffee2));

        // When
        List<CoffeeDto> result = coffeeService.getAllCoffees();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Ethiopian Yirgacheffe", result.get(0).getCoffeeName());
        verify(coffeeRepository, times(1)).findAll();
    }

    @Test
    void getCoffeeById_WhenExists_ShouldReturnCoffee() {
        // Given
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // When
        CoffeeDto result = coffeeService.getCoffeeById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ethiopian Yirgacheffe", result.getCoffeeName());
        assertEquals(RoastLevel.LIGHT, result.getRoastLevel());
        verify(coffeeRepository, times(1)).findById(1L);
    }

    @Test
    void getCoffeeById_WhenNotExists_ShouldThrowException() {
        // Given
        when(coffeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coffeeService.getCoffeeById(999L);
        });

        assertEquals("Coffee not found with id: 999", exception.getMessage());
    }

    @Test
    void getCoffeesByRoasterId_ShouldReturnCoffeesForRoaster() {
        // Given
        when(coffeeRepository.findByRoasterId(1L)).thenReturn(List.of(testCoffee));

        // When
        List<CoffeeDto> result = coffeeService.getCoffeesByRoasterId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Ethiopian Yirgacheffe", result.get(0).getCoffeeName());
        verify(coffeeRepository, times(1)).findByRoasterId(1L);
    }

    @Test
    void createCoffee_ShouldCreateAndReturnCoffee() {
        // Given
        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));
        when(coffeeRepository.save(any(Coffee.class))).thenReturn(testCoffee);

        // When
        CoffeeDto result = coffeeService.createCoffee(testCoffeeDto);

        // Then
        assertNotNull(result);
        verify(roasterRepository, times(1)).findById(1L);
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    void createCoffee_WhenCurrentWeightNotProvided_ShouldUseInitialWeight() {
        // Given
        testCoffeeDto.setCurrentWeight(null);
        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));
        when(coffeeRepository.save(any(Coffee.class))).thenAnswer(invocation -> {
            Coffee coffee = invocation.getArgument(0);
            coffee.setId(1L);
            return coffee;
        });

        // When
        CoffeeDto result = coffeeService.createCoffee(testCoffeeDto);

        // Then
        assertNotNull(result);
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    void createCoffee_WhenRoasterNotExists_ShouldThrowException() {
        // Given
        when(roasterRepository.findById(999L)).thenReturn(Optional.empty());
        testCoffeeDto.setRoasterId(999L);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coffeeService.createCoffee(testCoffeeDto);
        });

        assertEquals("Roaster not found with id: 999", exception.getMessage());
        verify(coffeeRepository, never()).save(any(Coffee.class));
    }

    @Test
    void updateCoffee_WhenExists_ShouldUpdateAndReturnCoffee() {
        // Given
        testCoffeeDto.setCoffeeName("Updated Coffee Name");
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));
        when(coffeeRepository.save(any(Coffee.class))).thenReturn(testCoffee);

        // When
        CoffeeDto result = coffeeService.updateCoffee(1L, testCoffeeDto);

        // Then
        assertNotNull(result);
        verify(coffeeRepository, times(1)).findById(1L);
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    void updateCoffee_WhenNotExists_ShouldThrowException() {
        // Given
        when(coffeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coffeeService.updateCoffee(999L, testCoffeeDto);
        });

        assertEquals("Coffee not found with id: 999", exception.getMessage());
        verify(coffeeRepository, never()).save(any(Coffee.class));
    }

    @Test
    void consumeCoffee_WhenEnoughAvailable_ShouldReduceWeight() {
        // Given
        BigDecimal consumeAmount = BigDecimal.valueOf(20);
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));
        when(coffeeRepository.save(any(Coffee.class))).thenAnswer(invocation -> {
            Coffee coffee = invocation.getArgument(0);
            return coffee;
        });

        // When
        CoffeeDto result = coffeeService.consumeCoffee(1L, consumeAmount);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(180), result.getCurrentWeight());
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    void consumeCoffee_WhenNotEnoughAvailable_ShouldThrowException() {
        // Given
        BigDecimal consumeAmount = BigDecimal.valueOf(300); // More than available (200)
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coffeeService.consumeCoffee(1L, consumeAmount);
        });

        assertEquals("Cannot consume more coffee than available", exception.getMessage());
        verify(coffeeRepository, never()).save(any(Coffee.class));
    }

    @Test
    void consumeCoffee_WhenCoffeeNotExists_ShouldThrowException() {
        // Given
        when(coffeeRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coffeeService.consumeCoffee(999L, BigDecimal.valueOf(20));
        });

        assertEquals("Coffee not found with id: 999", exception.getMessage());
    }

    @Test
    void deleteCoffee_WhenExists_ShouldDelete() {
        // Given
        when(coffeeRepository.existsById(1L)).thenReturn(true);

        // When
        coffeeService.deleteCoffee(1L);

        // Then
        verify(coffeeRepository, times(1)).existsById(1L);
        verify(coffeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCoffee_WhenNotExists_ShouldThrowException() {
        // Given
        when(coffeeRepository.existsById(999L)).thenReturn(false);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            coffeeService.deleteCoffee(999L);
        });

        assertEquals("Coffee not found with id: 999", exception.getMessage());
        verify(coffeeRepository, never()).deleteById(any());
    }

    @Test
    void convertToDto_ShouldCalculateDaysSinceRoast() {
        // Given
        LocalDate roastDate = LocalDate.now().minusDays(10);
        testCoffee.setRoastDate(roastDate);
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // When
        CoffeeDto result = coffeeService.getCoffeeById(1L);

        // Then
        assertNotNull(result.getDaysSinceRoast());
        assertEquals(10L, result.getDaysSinceRoast());
    }

    @Test
    void convertToDto_ShouldCalculatePercentageRemaining() {
        // Given
        testCoffee.setInitialWeight(BigDecimal.valueOf(250));
        testCoffee.setCurrentWeight(BigDecimal.valueOf(125));
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // When
        CoffeeDto result = coffeeService.getCoffeeById(1L);

        // Then
        assertNotNull(result.getPercentageRemaining());
        assertEquals(0, BigDecimal.valueOf(50.0).compareTo(result.getPercentageRemaining().setScale(1, java.math.RoundingMode.HALF_UP)));
    }
}

