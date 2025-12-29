package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.RoasterDto;
import com.avilachehab.christmasgifts.model.Coffee;
import com.avilachehab.christmasgifts.model.Roaster;
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
import java.util.ArrayList;
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
class RoasterServiceTest {

    @Mock
    private RoasterRepository roasterRepository;

    @InjectMocks
    private RoasterService roasterService;

    private Roaster testRoaster;
    private Coffee testCoffee;

    @BeforeEach
    void setUp() {
        testRoaster = new Roaster();
        testRoaster.setId(1L);
        testRoaster.setName("Blue Bottle");
        testRoaster.setLocation("Oakland, CA");
        testRoaster.setWebsite("https://bluebottlecoffee.com");
        testRoaster.setNotes("Great roaster");
        testRoaster.setCoffees(new ArrayList<>());

        testCoffee = new Coffee();
        testCoffee.setId(1L);
        testCoffee.setCoffeeName("Ethiopian Yirgacheffe");
        testCoffee.setRoastDate(LocalDate.now().minusDays(5));
        testCoffee.setPurchaseDate(LocalDate.now().minusDays(3));
        testCoffee.setInitialWeight(BigDecimal.valueOf(250));
        testCoffee.setCurrentWeight(BigDecimal.valueOf(200));
        testCoffee.setPrice(BigDecimal.valueOf(18.50));
        testCoffee.setRoaster(testRoaster);
    }

    @Test
    @DisplayName("Should return all roasters when getAllRoasters is called")
    void getAllRoasters_validRequest_returnsAllRoasters() {
        // Arrange
        Roaster roaster2 = new Roaster();
        roaster2.setId(2L);
        roaster2.setName("Stumptown");
        roaster2.setCoffees(new ArrayList<>());

        when(roasterRepository.findAll()).thenReturn(Arrays.asList(testRoaster, roaster2));

        // Act
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Assert
        assertThat(result)
                .isNotNull()
                .hasSize(2)
                .extracting(RoasterDto::getName)
                .containsExactly("Blue Bottle", "Stumptown");
        verify(roasterRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no roasters exist")
    void getAllRoasters_noRoasters_returnsEmptyList() {
        // Arrange
        when(roasterRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Assert
        assertThat(result).isEmpty();
        verify(roasterRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return roaster when valid ID provided")
    void getRoasterById_validId_returnsRoaster() {
        // Arrange
        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));

        // Act
        RoasterDto result = roasterService.getRoasterById(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(RoasterDto::getId, RoasterDto::getName, RoasterDto::getLocation)
                .containsExactly(1L, "Blue Bottle", "Oakland, CA");
        verify(roasterRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when roaster ID not found")
    void getRoasterById_nonExistentId_throwsException() {
        // Arrange
        when(roasterRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roasterService.getRoasterById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Roaster not found with id: 999");
        verify(roasterRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should create and return roaster successfully")
    void createRoaster_validDto_createsAndReturnsRoaster() {
        // Arrange
        RoasterDto dto = new RoasterDto();
        dto.setName("New Roaster");
        dto.setLocation("Portland, OR");
        dto.setWebsite("https://newroaster.com");
        dto.setNotes("Test notes");

        Roaster savedRoaster = new Roaster();
        savedRoaster.setId(2L);
        savedRoaster.setName("New Roaster");
        savedRoaster.setLocation("Portland, OR");
        savedRoaster.setWebsite("https://newroaster.com");
        savedRoaster.setNotes("Test notes");
        savedRoaster.setCoffees(new ArrayList<>());

        when(roasterRepository.save(any(Roaster.class))).thenReturn(savedRoaster);

        // Act
        RoasterDto result = roasterService.createRoaster(dto);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(RoasterDto::getId, RoasterDto::getName, RoasterDto::getLocation)
                .containsExactly(2L, "New Roaster", "Portland, OR");
        verify(roasterRepository, times(1)).save(any(Roaster.class));
    }

    @Test
    @DisplayName("Should update existing roaster successfully")
    void updateRoaster_validIdAndDto_updatesAndReturnsRoaster() {
        // Arrange
        RoasterDto dto = new RoasterDto();
        dto.setName("Updated Roaster");
        dto.setLocation("Seattle, WA");

        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));
        when(roasterRepository.save(any(Roaster.class))).thenReturn(testRoaster);

        // Act
        RoasterDto result = roasterService.updateRoaster(1L, dto);

        // Assert
        assertThat(result).isNotNull();
        verify(roasterRepository, times(1)).findById(1L);
        verify(roasterRepository, times(1)).save(any(Roaster.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent roaster")
    void updateRoaster_nonExistentId_throwsException() {
        // Arrange
        RoasterDto dto = new RoasterDto();
        dto.setName("Updated Roaster");

        when(roasterRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roasterService.updateRoaster(999L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Roaster not found with id: 999");
        verify(roasterRepository, times(1)).findById(999L);
        verify(roasterRepository, never()).save(any(Roaster.class));
    }

    @Test
    @DisplayName("Should delete existing roaster successfully")
    void deleteRoaster_validId_deletesRoaster() {
        // Arrange
        when(roasterRepository.existsById(1L)).thenReturn(true);

        // Act
        roasterService.deleteRoaster(1L);

        // Assert
        verify(roasterRepository, times(1)).existsById(1L);
        verify(roasterRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent roaster")
    void deleteRoaster_nonExistentId_throwsException() {
        // Arrange
        when(roasterRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> roasterService.deleteRoaster(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Roaster not found with id: 999");
        verify(roasterRepository, times(1)).existsById(999L);
        verify(roasterRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should correctly calculate total spent across coffees")
    void convertToDto_withCoffees_calculatesTotalSpent() {
        // Arrange
        Coffee coffee1 = new Coffee();
        coffee1.setPrice(BigDecimal.valueOf(18.50));
        coffee1.setRoaster(testRoaster);

        Coffee coffee2 = new Coffee();
        coffee2.setPrice(BigDecimal.valueOf(22.00));
        coffee2.setRoaster(testRoaster);

        testRoaster.setCoffees(Arrays.asList(coffee1, coffee2));
        when(roasterRepository.findAll()).thenReturn(List.of(testRoaster));

        // Act
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(roaster -> {
                    assertThat(roaster.getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(40.50));
                    assertThat(roaster.getCoffeeCount()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Should handle null prices when calculating total spent")
    void convertToDto_withNullPrices_calculatesCorrectTotal() {
        // Arrange
        Coffee coffee1 = new Coffee();
        coffee1.setPrice(BigDecimal.valueOf(18.50));
        coffee1.setRoaster(testRoaster);

        Coffee coffee2 = new Coffee();
        coffee2.setPrice(null); // Null price
        coffee2.setRoaster(testRoaster);

        testRoaster.setCoffees(Arrays.asList(coffee1, coffee2));
        when(roasterRepository.findAll()).thenReturn(List.of(testRoaster));

        // Act
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(roaster -> {
                    assertThat(roaster.getTotalSpent()).isEqualByComparingTo(BigDecimal.valueOf(18.50));
                    assertThat(roaster.getCoffeeCount()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Should return zero total spent when roaster has no coffees")
    void convertToDto_noCoffees_returnsZeroTotalSpent() {
        // Arrange
        testRoaster.setCoffees(Collections.emptyList());
        when(roasterRepository.findAll()).thenReturn(List.of(testRoaster));

        // Act
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Assert
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(roaster -> {
                    assertThat(roaster.getTotalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
                    assertThat(roaster.getCoffeeCount()).isZero();
                });
    }
}
