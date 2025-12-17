package com.avilachehab.christmasgifts.controller;

import com.avilachehab.christmasgifts.dto.RoasterDto;
import com.avilachehab.christmasgifts.filter.JwtAuthenticationFilter;
import com.avilachehab.christmasgifts.service.RoasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RoasterController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoasterService roasterService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllRoasters_ShouldReturnListOfRoasters() throws Exception {
        // Given
        RoasterDto roaster1 = new RoasterDto();
        roaster1.setId(1L);
        roaster1.setName("Blue Bottle");
        roaster1.setTotalSpent(BigDecimal.valueOf(50.00));

        RoasterDto roaster2 = new RoasterDto();
        roaster2.setId(2L);
        roaster2.setName("Stumptown");
        roaster2.setTotalSpent(BigDecimal.valueOf(30.00));

        when(roasterService.getAllRoasters()).thenReturn(Arrays.asList(roaster1, roaster2));

        // When/Then
        mockMvc.perform(get("/api/roasters"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Blue Bottle"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Stumptown"));

        verify(roasterService, times(1)).getAllRoasters();
    }

    @Test
    void getRoasterById_ShouldReturnRoaster() throws Exception {
        // Given
        RoasterDto roaster = new RoasterDto();
        roaster.setId(1L);
        roaster.setName("Blue Bottle");
        roaster.setLocation("Oakland, CA");

        when(roasterService.getRoasterById(1L)).thenReturn(roaster);

        // When/Then
        mockMvc.perform(get("/api/roasters/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Blue Bottle"))
                .andExpect(jsonPath("$.location").value("Oakland, CA"));

        verify(roasterService, times(1)).getRoasterById(1L);
    }

    @Test
    void createRoaster_ShouldReturnCreatedRoaster() throws Exception {
        // Given
        RoasterDto inputDto = new RoasterDto();
        inputDto.setName("New Roaster");
        inputDto.setLocation("Portland, OR");

        RoasterDto savedDto = new RoasterDto();
        savedDto.setId(1L);
        savedDto.setName("New Roaster");
        savedDto.setLocation("Portland, OR");

        when(roasterService.createRoaster(any(RoasterDto.class))).thenReturn(savedDto);

        // When/Then
        mockMvc.perform(post("/api/roasters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Roaster"));

        verify(roasterService, times(1)).createRoaster(any(RoasterDto.class));
    }

    @Test
    void updateRoaster_ShouldReturnUpdatedRoaster() throws Exception {
        // Given
        RoasterDto inputDto = new RoasterDto();
        inputDto.setName("Updated Roaster");

        RoasterDto updatedDto = new RoasterDto();
        updatedDto.setId(1L);
        updatedDto.setName("Updated Roaster");

        when(roasterService.updateRoaster(eq(1L), any(RoasterDto.class))).thenReturn(updatedDto);

        // When/Then
        mockMvc.perform(put("/api/roasters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Roaster"));

        verify(roasterService, times(1)).updateRoaster(eq(1L), any(RoasterDto.class));
    }

    @Test
    void deleteRoaster_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(roasterService).deleteRoaster(1L);

        // When/Then
        mockMvc.perform(delete("/api/roasters/1"))
                .andExpect(status().isNoContent());

        verify(roasterService, times(1)).deleteRoaster(1L);
    }
}

