package com.avilachehab.christmasgifts.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000,https://test.example.com",
    "app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
    "app.cors.allowed-headers=*",
    "app.cors.exposed-headers=Authorization,Content-Type"
})
class CorsConfigTest {

    @Autowired
    private CorsFilter corsFilter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void corsFilter_ShouldBeCreated() {
        assertNotNull(corsFilter);
    }

    @Test
    void corsFilter_ShouldAllowConfiguredOrigins() throws Exception {
        // Given
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(corsFilter)
                .build();

        // When/Then - Test CORS preflight request
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void corsFilter_ShouldExposeConfiguredHeaders() throws Exception {
        // Given
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(corsFilter)
                .build();

        // When/Then
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Expose-Headers"));
    }

    @Test
    void corsFilter_ShouldAllowCredentials() throws Exception {
        // Given
        MockMvc mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(corsFilter)
                .build();

        // When/Then
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "https://test.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }
}

