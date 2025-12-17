package com.avilachehab.christmasgifts.service;

import com.avilachehab.christmasgifts.dto.RoasterDto;
import com.avilachehab.christmasgifts.model.Coffee;
import com.avilachehab.christmasgifts.model.Roaster;
import com.avilachehab.christmasgifts.repository.RoasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void getAllRoasters_ShouldReturnAllRoasters() {
        // Given
        Roaster roaster2 = new Roaster();
        roaster2.setId(2L);
        roaster2.setName("Stumptown");
        roaster2.setCoffees(new ArrayList<>());

        when(roasterRepository.findAll()).thenReturn(Arrays.asList(testRoaster, roaster2));

        // When
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Blue Bottle", result.get(0).getName());
        assertEquals("Stumptown", result.get(1).getName());
        verify(roasterRepository, times(1)).findAll();
    }

    @Test
    void getRoasterById_WhenExists_ShouldReturnRoaster() {
        // Given
        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));

        // When
        RoasterDto result = roasterService.getRoasterById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Blue Bottle", result.getName());
        assertEquals("Oakland, CA", result.getLocation());
        verify(roasterRepository, times(1)).findById(1L);
    }

    @Test
    void getRoasterById_WhenNotExists_ShouldThrowException() {
        // Given
        when(roasterRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roasterService.getRoasterById(999L);
        });

        assertEquals("Roaster not found with id: 999", exception.getMessage());
        verify(roasterRepository, times(1)).findById(999L);
    }

    @Test
    void createRoaster_ShouldCreateAndReturnRoaster() {
        // Given
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

        // When
        RoasterDto result = roasterService.createRoaster(dto);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Roaster", result.getName());
        assertEquals("Portland, OR", result.getLocation());
        verify(roasterRepository, times(1)).save(any(Roaster.class));
    }

    @Test
    void updateRoaster_WhenExists_ShouldUpdateAndReturnRoaster() {
        // Given
        RoasterDto dto = new RoasterDto();
        dto.setName("Updated Roaster");
        dto.setLocation("Seattle, WA");

        when(roasterRepository.findById(1L)).thenReturn(Optional.of(testRoaster));
        when(roasterRepository.save(any(Roaster.class))).thenReturn(testRoaster);

        // When
        RoasterDto result = roasterService.updateRoaster(1L, dto);

        // Then
        assertNotNull(result);
        verify(roasterRepository, times(1)).findById(1L);
        verify(roasterRepository, times(1)).save(any(Roaster.class));
    }

    @Test
    void updateRoaster_WhenNotExists_ShouldThrowException() {
        // Given
        RoasterDto dto = new RoasterDto();
        dto.setName("Updated Roaster");

        when(roasterRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roasterService.updateRoaster(999L, dto);
        });

        assertEquals("Roaster not found with id: 999", exception.getMessage());
        verify(roasterRepository, times(1)).findById(999L);
        verify(roasterRepository, never()).save(any(Roaster.class));
    }

    @Test
    void deleteRoaster_WhenExists_ShouldDelete() {
        // Given
        when(roasterRepository.existsById(1L)).thenReturn(true);

        // When
        roasterService.deleteRoaster(1L);

        // Then
        verify(roasterRepository, times(1)).existsById(1L);
        verify(roasterRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRoaster_WhenNotExists_ShouldThrowException() {
        // Given
        when(roasterRepository.existsById(999L)).thenReturn(false);

        // When/Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roasterService.deleteRoaster(999L);
        });

        assertEquals("Roaster not found with id: 999", exception.getMessage());
        verify(roasterRepository, times(1)).existsById(999L);
        verify(roasterRepository, never()).deleteById(any());
    }

    @Test
    void convertToDto_ShouldCalculateTotalSpent() {
        // Given
        Coffee coffee1 = new Coffee();
        coffee1.setPrice(BigDecimal.valueOf(18.50));
        coffee1.setRoaster(testRoaster);

        Coffee coffee2 = new Coffee();
        coffee2.setPrice(BigDecimal.valueOf(22.00));
        coffee2.setRoaster(testRoaster);

        testRoaster.setCoffees(Arrays.asList(coffee1, coffee2));
        when(roasterRepository.findAll()).thenReturn(List.of(testRoaster));

        // When
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(40.50), result.get(0).getTotalSpent());
        assertEquals(2, result.get(0).getCoffeeCount());
    }

    @Test
    void convertToDto_ShouldHandleNullPrices() {
        // Given
        Coffee coffee1 = new Coffee();
        coffee1.setPrice(BigDecimal.valueOf(18.50));
        coffee1.setRoaster(testRoaster);

        Coffee coffee2 = new Coffee();
        coffee2.setPrice(null); // Null price
        coffee2.setRoaster(testRoaster);

        testRoaster.setCoffees(Arrays.asList(coffee1, coffee2));
        when(roasterRepository.findAll()).thenReturn(List.of(testRoaster));

        // When
        List<RoasterDto> result = roasterService.getAllRoasters();

        // Then
        assertEquals(1, result.size());
        assertEquals(BigDecimal.valueOf(18.50), result.get(0).getTotalSpent());
        assertEquals(2, result.get(0).getCoffeeCount());
    }
}

