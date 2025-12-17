package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.InventorySummaryDto;
import com.avilachehab.christmasgifts.filter.JwtAuthenticationFilter;
import com.avilachehab.christmasgifts.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getInventorySummary_ShouldReturnSummary() throws Exception {
        // Given
        InventorySummaryDto summary = new InventorySummaryDto();
        summary.setTotalWeight(BigDecimal.valueOf(550));
        summary.setTotalBags(3);
        summary.setTotalSpent(BigDecimal.valueOf(63.50));
        summary.setAveragePricePerGram(BigDecimal.valueOf(0.0605));

        when(inventoryService.getInventorySummary()).thenReturn(summary);

        // When/Then
        mockMvc.perform(get("/api/inventory/summary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalWeight").value(550))
                .andExpect(jsonPath("$.totalBags").value(3))
                .andExpect(jsonPath("$.totalSpent").value(63.50))
                .andExpect(jsonPath("$.averagePricePerGram").value(0.0605));

        verify(inventoryService, times(1)).getInventorySummary();
    }
}

