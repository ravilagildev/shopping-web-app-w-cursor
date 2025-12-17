package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.dto.InventorySummaryDto;
import com.avilachehab.christmasgifts.dto.RoasterDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private CoffeeService coffeeService;

    @Mock
    private RoasterService roasterService;

    @InjectMocks
    private InventoryService inventoryService;

    private CoffeeDto coffee1;
    private CoffeeDto coffee2;
    private CoffeeDto coffee3;
    private RoasterDto roaster1;

    @BeforeEach
    void setUp() {
        coffee1 = new CoffeeDto();
        coffee1.setId(1L);
        coffee1.setCoffeeName("Ethiopian Yirgacheffe");
        coffee1.setInitialWeight(BigDecimal.valueOf(250));
        coffee1.setCurrentWeight(BigDecimal.valueOf(200));
        coffee1.setPrice(BigDecimal.valueOf(18.50));
        coffee1.setRoastDate(LocalDate.now().minusDays(5));
        coffee1.setDaysSinceRoast(5L);
        coffee1.setPercentageRemaining(BigDecimal.valueOf(80));

        coffee2 = new CoffeeDto();
        coffee2.setId(2L);
        coffee2.setCoffeeName("Colombian");
        coffee2.setInitialWeight(BigDecimal.valueOf(500));
        coffee2.setCurrentWeight(BigDecimal.valueOf(50)); // Low stock (< 20%)
        coffee2.setPrice(BigDecimal.valueOf(25.00));
        coffee2.setRoastDate(LocalDate.now().minusDays(35)); // Aging (> 30 days)
        coffee2.setDaysSinceRoast(35L);
        coffee2.setPercentageRemaining(BigDecimal.valueOf(10));

        coffee3 = new CoffeeDto();
        coffee3.setId(3L);
        coffee3.setCoffeeName("Kenyan");
        coffee3.setInitialWeight(BigDecimal.valueOf(300));
        coffee3.setCurrentWeight(BigDecimal.valueOf(300));
        coffee3.setPrice(BigDecimal.valueOf(20.00));
        coffee3.setRoastDate(LocalDate.now().minusDays(2));
        coffee3.setDaysSinceRoast(2L);
        coffee3.setPercentageRemaining(BigDecimal.valueOf(100));

        roaster1 = new RoasterDto();
        roaster1.setId(1L);
        roaster1.setName("Blue Bottle");
    }

    @Test
    void getInventorySummary_ShouldCalculateTotalWeight() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(550), result.getTotalWeight()); // 200 + 50 + 300
        verify(coffeeService, times(1)).getAllCoffees();
    }

    @Test
    void getInventorySummary_ShouldCalculateTotalBags() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertEquals(3, result.getTotalBags());
    }

    @Test
    void getInventorySummary_ShouldCalculateTotalSpent() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertEquals(BigDecimal.valueOf(63.50), result.getTotalSpent()); // 18.50 + 25.00 + 20.00
    }

    @Test
    void getInventorySummary_ShouldCalculateAveragePricePerGram() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertNotNull(result.getAveragePricePerGram());
        // Total spent: 63.50, Total initial weight: 1050g (250 + 500 + 300)
        // Average: 63.50 / 1050 = 0.060476... which rounds to 0.0605 with 4 decimal places
        BigDecimal expected = BigDecimal.valueOf(63.50).divide(BigDecimal.valueOf(1050), 4, java.math.RoundingMode.HALF_UP);
        assertEquals(0, expected.compareTo(result.getAveragePricePerGram()));
    }

    @Test
    void getInventorySummary_ShouldIdentifyLowStockCoffees() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertNotNull(result.getLowStockCoffees());
        assertEquals(1, result.getLowStockCoffees().size());
        assertEquals("Colombian", result.getLowStockCoffees().get(0).getCoffeeName());
    }

    @Test
    void getInventorySummary_ShouldIdentifyAgingCoffees() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertNotNull(result.getAgingCoffees());
        assertEquals(1, result.getAgingCoffees().size());
        assertEquals("Colombian", result.getAgingCoffees().get(0).getCoffeeName());
    }

    @Test
    void getInventorySummary_ShouldExcludeEmptyCoffeesFromLowStock() {
        // Given
        CoffeeDto emptyCoffee = new CoffeeDto();
        emptyCoffee.setId(4L);
        emptyCoffee.setCoffeeName("Empty Coffee");
        emptyCoffee.setCurrentWeight(BigDecimal.ZERO);
        emptyCoffee.setPercentageRemaining(BigDecimal.valueOf(5)); // < 20% but weight is 0

        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3, emptyCoffee));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertEquals(1, result.getLowStockCoffees().size()); // Only coffee2, not emptyCoffee
    }

    @Test
    void getInventorySummary_ShouldHandleNullPrices() {
        // Given
        coffee1.setPrice(null);
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertEquals(BigDecimal.valueOf(45.00), result.getTotalSpent()); // Only coffee2 and coffee3
    }

    @Test
    void getInventorySummary_ShouldReturnAllRoasters() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertNotNull(result.getRoasters());
        assertEquals(1, result.getRoasters().size());
        assertEquals("Blue Bottle", result.getRoasters().get(0).getName());
    }

    @Test
    void getInventorySummary_WhenNoCoffees_ShouldReturnZeroValues() {
        // Given
        when(coffeeService.getAllCoffees()).thenReturn(List.of());
        when(roasterService.getAllRoasters()).thenReturn(List.of());

        // When
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Then
        assertEquals(BigDecimal.ZERO, result.getTotalWeight());
        assertEquals(0, result.getTotalBags());
        assertEquals(BigDecimal.ZERO, result.getTotalSpent());
        assertEquals(BigDecimal.ZERO, result.getAveragePricePerGram());
        assertTrue(result.getLowStockCoffees().isEmpty());
        assertTrue(result.getAgingCoffees().isEmpty());
    }
}

