package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.CoffeeDto;
import com.avilachehab.christmasgifts.filter.JwtAuthenticationFilter;
import com.avilachehab.christmasgifts.model.RoastLevel;
import com.avilachehab.christmasgifts.service.CoffeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CoffeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class CoffeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoffeeService coffeeService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCoffees_ShouldReturnListOfCoffees() throws Exception {
        // Given
        CoffeeDto coffee1 = new CoffeeDto();
        coffee1.setId(1L);
        coffee1.setCoffeeName("Ethiopian Yirgacheffe");
        coffee1.setCurrentWeight(BigDecimal.valueOf(200));

        CoffeeDto coffee2 = new CoffeeDto();
        coffee2.setId(2L);
        coffee2.setCoffeeName("Colombian");
        coffee2.setCurrentWeight(BigDecimal.valueOf(150));

        when(coffeeService.getAllCoffees()).thenReturn(Arrays.asList(coffee1, coffee2));

        // When/Then
        mockMvc.perform(get("/api/coffees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].coffeeName").value("Ethiopian Yirgacheffe"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(coffeeService, times(1)).getAllCoffees();
    }

    @Test
    void getCoffeeById_ShouldReturnCoffee() throws Exception {
        // Given
        CoffeeDto coffee = new CoffeeDto();
        coffee.setId(1L);
        coffee.setCoffeeName("Ethiopian Yirgacheffe");
        coffee.setRoastLevel(RoastLevel.LIGHT);

        when(coffeeService.getCoffeeById(1L)).thenReturn(coffee);

        // When/Then
        mockMvc.perform(get("/api/coffees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.coffeeName").value("Ethiopian Yirgacheffe"))
                .andExpect(jsonPath("$.roastLevel").value("LIGHT"));

        verify(coffeeService, times(1)).getCoffeeById(1L);
    }

    @Test
    void getCoffeesByRoasterId_ShouldReturnCoffeesForRoaster() throws Exception {
        // Given
        CoffeeDto coffee = new CoffeeDto();
        coffee.setId(1L);
        coffee.setCoffeeName("Ethiopian Yirgacheffe");
        coffee.setRoasterId(1L);

        when(coffeeService.getCoffeesByRoasterId(1L)).thenReturn(List.of(coffee));

        // When/Then
        mockMvc.perform(get("/api/coffees/roaster/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].roasterId").value(1));

        verify(coffeeService, times(1)).getCoffeesByRoasterId(1L);
    }

    @Test
    void createCoffee_ShouldReturnCreatedCoffee() throws Exception {
        // Given
        CoffeeDto inputDto = new CoffeeDto();
        inputDto.setCoffeeName("New Coffee");
        inputDto.setRoastDate(LocalDate.now());
        inputDto.setPurchaseDate(LocalDate.now());
        inputDto.setInitialWeight(BigDecimal.valueOf(250));
        inputDto.setRoasterId(1L);

        CoffeeDto savedDto = new CoffeeDto();
        savedDto.setId(1L);
        savedDto.setCoffeeName("New Coffee");
        savedDto.setRoasterId(1L);

        when(coffeeService.createCoffee(any(CoffeeDto.class))).thenReturn(savedDto);

        // When/Then
        mockMvc.perform(post("/api/coffees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.coffeeName").value("New Coffee"));

        verify(coffeeService, times(1)).createCoffee(any(CoffeeDto.class));
    }

    @Test
    void updateCoffee_ShouldReturnUpdatedCoffee() throws Exception {
        // Given
        CoffeeDto inputDto = new CoffeeDto();
        inputDto.setCoffeeName("Updated Coffee");
        inputDto.setRoastDate(LocalDate.now());
        inputDto.setPurchaseDate(LocalDate.now());
        inputDto.setInitialWeight(BigDecimal.valueOf(250));
        inputDto.setCurrentWeight(BigDecimal.valueOf(200));
        inputDto.setRoasterId(1L);

        CoffeeDto updatedDto = new CoffeeDto();
        updatedDto.setId(1L);
        updatedDto.setCoffeeName("Updated Coffee");

        when(coffeeService.updateCoffee(eq(1L), any(CoffeeDto.class))).thenReturn(updatedDto);

        // When/Then
        mockMvc.perform(put("/api/coffees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.coffeeName").value("Updated Coffee"));

        verify(coffeeService, times(1)).updateCoffee(eq(1L), any(CoffeeDto.class));
    }

    @Test
    void consumeCoffee_ShouldReturnUpdatedCoffee() throws Exception {
        // Given
        CoffeeDto updatedDto = new CoffeeDto();
        updatedDto.setId(1L);
        updatedDto.setCurrentWeight(BigDecimal.valueOf(180));

        when(coffeeService.consumeCoffee(eq(1L), eq(BigDecimal.valueOf(20)))).thenReturn(updatedDto);

        // When/Then
        mockMvc.perform(post("/api/coffees/1/consume")
                        .param("amount", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentWeight").value(180));

        verify(coffeeService, times(1)).consumeCoffee(eq(1L), eq(BigDecimal.valueOf(20)));
    }

    @Test
    void deleteCoffee_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(coffeeService).deleteCoffee(1L);

        // When/Then
        mockMvc.perform(delete("/api/coffees/1"))
                .andExpect(status().isNoContent());

        verify(coffeeService, times(1)).deleteCoffee(1L);
    }
}

