package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.dto.InventorySummaryDto;
import com.avilachehab.christmasgifts.dto.RoasterDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @DisplayName("Should calculate total weight correctly")
    void getInventorySummary_withCoffees_calculatesTotalWeight() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(InventorySummaryDto::getTotalWeight)
                .isEqualTo(BigDecimal.valueOf(550)); // 200 + 50 + 300
        verify(coffeeService, times(1)).getAllCoffees();
    }

    @Test
    @DisplayName("Should calculate total bags correctly")
    void getInventorySummary_withCoffees_calculatesTotalBags() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getTotalBags()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should calculate total spent correctly")
    void getInventorySummary_withCoffees_calculatesTotalSpent() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(63.50)); // 18.50 + 25.00 + 20.00
    }

    @Test
    @DisplayName("Should calculate average price per gram correctly")
    void getInventorySummary_withCoffees_calculatesAveragePricePerGram() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        // Total spent: 63.50, Total initial weight: 1050g (250 + 500 + 300)
        // Average: 63.50 / 1050 = 0.060476... which rounds to 0.0605 with 4 decimal places
        BigDecimal expected = BigDecimal.valueOf(63.50)
                .divide(BigDecimal.valueOf(1050), 4, java.math.RoundingMode.HALF_UP);
        assertThat(result.getAveragePricePerGram()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Should identify low stock coffees correctly")
    void getInventorySummary_withLowStockCoffees_identifiesLowStock() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getLowStockCoffees())
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(CoffeeDto::getCoffeeName)
                .isEqualTo("Colombian");
    }

    @Test
    @DisplayName("Should identify aging coffees correctly")
    void getInventorySummary_withAgingCoffees_identifiesAgingCoffees() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getAgingCoffees())
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(CoffeeDto::getCoffeeName)
                .isEqualTo("Colombian");
    }

    @Test
    @DisplayName("Should exclude empty coffees from low stock list")
    void getInventorySummary_withEmptyCoffee_excludesFromLowStock() {
        // Arrange
        CoffeeDto emptyCoffee = new CoffeeDto();
        emptyCoffee.setId(4L);
        emptyCoffee.setCoffeeName("Empty Coffee");
        emptyCoffee.setCurrentWeight(BigDecimal.ZERO);
        emptyCoffee.setPercentageRemaining(BigDecimal.valueOf(5)); // < 20% but weight is 0

        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3, emptyCoffee));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getLowStockCoffees()).hasSize(1); // Only coffee2, not emptyCoffee
    }

    @Test
    @DisplayName("Should handle null prices when calculating totals")
    void getInventorySummary_withNullPrices_calculatesCorrectTotal() {
        // Arrange
        coffee1.setPrice(null);
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(45.00)); // Only coffee2 and coffee3
    }

    @Test
    @DisplayName("Should return all roasters in summary")
    void getInventorySummary_withRoasters_returnsAllRoasters() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2, coffee3));
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getRoasters())
                .isNotNull()
                .hasSize(1)
                .first()
                .extracting(RoasterDto::getName)
                .isEqualTo("Blue Bottle");
    }

    @Test
    @DisplayName("Should return zero values when no coffees exist")
    void getInventorySummary_noCoffees_returnsZeroValues() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(Collections.emptyList());
        when(roasterService.getAllRoasters()).thenReturn(Collections.emptyList());

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result)
                .isNotNull()
                .satisfies(summary -> {
                    assertThat(summary.getTotalWeight()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(summary.getTotalBags()).isZero();
                    assertThat(summary.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(summary.getAveragePricePerGram()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(summary.getLowStockCoffees()).isEmpty();
                    assertThat(summary.getAgingCoffees()).isEmpty();
                });
    }

    @Test
    @DisplayName("Should handle multiple roasters in summary")
    void getInventorySummary_multipleRoasters_returnsAllRoasters() {
        // Arrange
        RoasterDto roaster2 = new RoasterDto();
        roaster2.setId(2L);
        roaster2.setName("Stumptown");

        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2));
        when(roasterService.getAllRoasters()).thenReturn(Arrays.asList(roaster1, roaster2));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getRoasters())
                .hasSize(2)
                .extracting(RoasterDto::getName)
                .containsExactly("Blue Bottle", "Stumptown");
    }

    @Test
    @DisplayName("Should not include fresh coffees in aging list")
    void getInventorySummary_freshCoffees_excludesFromAging() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(List.of(coffee1, coffee3)); // Only fresh coffees
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getAgingCoffees()).isEmpty();
    }

    @Test
    @DisplayName("Should not include high stock coffees in low stock list")
    void getInventorySummary_highStockCoffees_excludesFromLowStock() {
        // Arrange
        when(coffeeService.getAllCoffees()).thenReturn(List.of(coffee1, coffee3)); // Only high stock coffees
        when(roasterService.getAllRoasters()).thenReturn(List.of(roaster1));

        // Act
        InventorySummaryDto result = inventoryService.getInventorySummary();

        // Assert
        assertThat(result.getLowStockCoffees()).isEmpty();
    }
}
