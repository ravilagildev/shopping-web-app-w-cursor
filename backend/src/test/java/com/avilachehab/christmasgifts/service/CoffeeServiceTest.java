package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.model.Coffee;
import com.avilachehab.christmasgifts.model.Roaster;
import com.avilachehab.christmasgifts.model.RoastLevel;
import com.avilachehab.christmasgifts.repository.CoffeeRepository;
import com.avilachehab.christmasgifts.repository.RoasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @DisplayName("Should return all coffees when getAllCoffees is called")
    void getAllCoffees_validRequest_returnsAllCoffees() {
        // Arrange
        Coffee coffee2 = new Coffee();
        coffee2.setId(2L);
        coffee2.setCoffeeName("Colombian");
        coffee2.setRoaster(testRoaster);

        when(coffeeRepository.findAll()).thenReturn(Arrays.asList(testCoffee, coffee2));

        // Act
        List<CoffeeDto> result = coffeeService.getAllCoffees();

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(CoffeeDto::getCoffeeName)
                .containsExactly("Ethiopian Yirgacheffe", "Colombian");
        verify(coffeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no coffees exist")
    void getAllCoffees_noCoffees_returnsEmptyList() {
        // Arrange
        when(coffeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CoffeeDto> result = coffeeService.getAllCoffees();

        // Assert
        assertThat(result).isEmpty();
        verify(coffeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return coffee when valid ID provided")
    void getCoffeeById_validId_returnsCoffee() {
        // Arrange
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // Act
        CoffeeDto result = coffeeService.getCoffeeById(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(CoffeeDto::getId, CoffeeDto::getCoffeeName, CoffeeDto::getRoastLevel)
                .containsExactly(1L, "Ethiopian Yirgacheffe", RoastLevel.LIGHT);
        verify(coffeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when coffee ID not found")
    void getCoffeeById_nonExistentId_throwsException() {
        // Arrange
        when(coffeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coffeeService.getCoffeeById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coffee not found with id: 999");
    }

    @Test
    @DisplayName("Should return coffees for specific roaster ID")
    void getCoffeesByRoasterId_validRoasterId_returnsCoffees() {
        // Arrange
        when(coffeeRepository.findByRoasterId(1L)).thenReturn(List.of(testCoffee));

        // Act
        List<CoffeeDto> result = coffeeService.getCoffeesByRoasterId(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(CoffeeDto::getCoffeeName)
                .isEqualTo("Ethiopian Yirgacheffe");
        verify(coffeeRepository, times(1)).findByRoasterId(1L);
    }

    @Test
    @DisplayName("Should return empty list when roaster has no coffees")
    void getCoffeesByRoasterId_noCoffe_returnsEmptyList() {
        // Arrange
        when(coffeeRepository.findByRoasterId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<CoffeeDto> result = coffeeService.getCoffeesByRoasterId(1L);

        // Assert
        assertThat(result).isEmpty();
        verify(coffeeRepository, times(1)).findByRoasterId(1L);
    }

    @Test
    @DisplayName("Should create and return coffee successfully")
    void createCoffee_validDto_createsAndReturnsCoffee() {
        // Arrange
        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));
        when(coffeeRepository.save(any(Coffee.class))).thenReturn(testCoffee);

        // Act
        CoffeeDto result = coffeeService.createCoffee(testCoffeeDto);

        // Assert
        assertThat(result).isNotNull();
        verify(roasterRepository, times(1)).findById(1L);
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should use initial weight when current weight not provided")
    void createCoffee_nullCurrentWeight_usesInitialWeight() {
        // Arrange
        testCoffeeDto.setCurrentWeight(null);
        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));
        when(coffeeRepository.save(any(Coffee.class))).thenAnswer(invocation -> {
            Coffee coffee = invocation.getArgument(0);
            coffee.setId(1L);
            return coffee;
        });

        // Act
        CoffeeDto result = coffeeService.createCoffee(testCoffeeDto);

        // Assert
        assertThat(result).isNotNull();
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should throw exception when roaster not found during creation")
    void createCoffee_nonExistentRoaster_throwsException() {
        // Arrange
        when(roasterRepository.findById(999L)).thenReturn(Optional.empty());
        testCoffeeDto.setRoasterId(999L);

        // Act & Assert
        assertThatThrownBy(() -> coffeeService.createCoffee(testCoffeeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Roaster not found with id: 999");
        verify(coffeeRepository, never()).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should update existing coffee successfully")
    void updateCoffee_validIdAndDto_updatesAndReturnsCoffee() {
        // Arrange
        testCoffeeDto.setCoffeeName("Updated Coffee Name");
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));
        when(coffeeRepository.save(any(Coffee.class))).thenReturn(testCoffee);

        // Act
        CoffeeDto result = coffeeService.updateCoffee(1L, testCoffeeDto);

        // Assert
        assertThat(result).isNotNull();
        verify(coffeeRepository, times(1)).findById(1L);
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent coffee")
    void updateCoffee_nonExistentId_throwsException() {
        // Arrange
        when(coffeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coffeeService.updateCoffee(999L, testCoffeeDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coffee not found with id: 999");
        verify(coffeeRepository, never()).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should reduce weight when consuming coffee")
    void consumeCoffee_validAmount_reducesWeight() {
        // Arrange
        BigDecimal consumeAmount = BigDecimal.valueOf(20);
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));
        when(coffeeRepository.save(any(Coffee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CoffeeDto result = coffeeService.consumeCoffee(1L, consumeAmount);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(CoffeeDto::getCurrentWeight)
                .isEqualTo(BigDecimal.valueOf(180));
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should throw exception when consuming more than available")
    void consumeCoffee_excessiveAmount_throwsException() {
        // Arrange
        BigDecimal consumeAmount = BigDecimal.valueOf(300); // More than available (200)
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // Act & Assert
        assertThatThrownBy(() -> coffeeService.consumeCoffee(1L, consumeAmount))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot consume more coffee than available");
        verify(coffeeRepository, never()).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should throw exception when consuming from non-existent coffee")
    void consumeCoffee_nonExistentId_throwsException() {
        // Arrange
        when(coffeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> coffeeService.consumeCoffee(999L, BigDecimal.valueOf(20)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coffee not found with id: 999");
    }

    @Test
    @DisplayName("Should handle consuming exactly all remaining coffee")
    void consumeCoffee_exactAmount_depletesCoffee() {
        // Arrange
        BigDecimal consumeAmount = BigDecimal.valueOf(200); // Exactly the current weight
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));
        when(coffeeRepository.save(any(Coffee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CoffeeDto result = coffeeService.consumeCoffee(1L, consumeAmount);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(CoffeeDto::getCurrentWeight)
                .isEqualTo(BigDecimal.ZERO);
        verify(coffeeRepository, times(1)).save(any(Coffee.class));
    }

    @Test
    @DisplayName("Should delete existing coffee successfully")
    void deleteCoffee_validId_deletesCoffee() {
        // Arrange
        when(coffeeRepository.existsById(1L)).thenReturn(true);

        // Act
        coffeeService.deleteCoffee(1L);

        // Assert
        verify(coffeeRepository, times(1)).existsById(1L);
        verify(coffeeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent coffee")
    void deleteCoffee_nonExistentId_throwsException() {
        // Arrange
        when(coffeeRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> coffeeService.deleteCoffee(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Coffee not found with id: 999");
        verify(coffeeRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should correctly calculate days since roast")
    void convertToDto_withRoastDate_calculatesDaysSinceRoast() {
        // Arrange
        LocalDate roastDate = LocalDate.now().minusDays(10);
        testCoffee.setRoastDate(roastDate);
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // Act
        CoffeeDto result = coffeeService.getCoffeeById(1L);

        // Assert
        assertThat(result.getDaysSinceRoast()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Should correctly calculate percentage remaining")
    void convertToDto_withWeights_calculatesPercentageRemaining() {
        // Arrange
        testCoffee.setInitialWeight(BigDecimal.valueOf(250));
        testCoffee.setCurrentWeight(BigDecimal.valueOf(125));
        when(coffeeRepository.findById(1L)).thenReturn(Optional.of(testCoffee));

        // Act
        CoffeeDto result = coffeeService.getCoffeeById(1L);

        // Assert
        assertThat(result.getPercentageRemaining())
                .isNotNull()
                .isEqualByComparingTo(BigDecimal.valueOf(50.0).setScale(1, java.math.RoundingMode.HALF_UP));
    }
}
