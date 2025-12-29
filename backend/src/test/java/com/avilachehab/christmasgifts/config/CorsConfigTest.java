package com.avilachehab.christmasgifts.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.CorsFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000,https://test.example.com",
    "app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS",
    "app.cors.allowed-headers=*",
    "app.cors.exposed-headers=Authorization,Content-Type"
})
@DisplayName("CORS Configuration Tests")
class CorsConfigTest {

    @Autowired
    private CorsFilter corsFilter;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should create CORS filter bean")
    void corsFilter_beanCreation_shouldCreateBean() {
        assertThat(corsFilter).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost:5173",
        "http://localhost:3000",
        "https://test.example.com"
    })
    @DisplayName("Should allow all configured origins")
    void corsFilter_allowedOrigins_shouldAllowEachConfiguredOrigin(String origin) throws Exception {
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", origin)
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", origin));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "DELETE", "OPTIONS"})
    @DisplayName("Should allow all configured HTTP methods")
    void corsFilter_allowedMethods_shouldAllowEachConfiguredMethod(String method) throws Exception {
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", method))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    @DisplayName("Should expose configured headers with correct values")
    void corsFilter_exposedHeaders_shouldMatchConfiguration() throws Exception {
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Expose-Headers"))
                .andExpect(header().string("Access-Control-Expose-Headers", "Authorization, Content-Type"));
    }

    @Test
    @DisplayName("Should allow credentials")
    void corsFilter_credentials_shouldAllowCredentials() throws Exception {
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "https://test.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Should reject unauthorized origins")
    void corsFilter_unauthorizedOrigin_shouldNotSetAllowOriginHeader() throws Exception {
        mockMvc.perform(options("/api/roasters")
                        .header("Origin", "http://malicious-site.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should include CORS headers on actual GET requests")
    void corsFilter_actualGetRequest_shouldIncludeCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/roasters")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("Should apply CORS to different API endpoints")
    void corsFilter_differentEndpoints_shouldApplyCorsToAllPaths() throws Exception {
        mockMvc.perform(options("/api/coffees")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    @DisplayName("Should handle requests without Origin header gracefully")
    void corsFilter_missingOrigin_shouldHandleGracefully() throws Exception {
        mockMvc.perform(get("/api/roasters"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
