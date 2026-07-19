package com.smartagri.advisory.controller;

import com.smartagri.advisory.config.WebMvcSecurityConfig;
import com.smartagri.advisory.dto.FarmFieldResponse;
import com.smartagri.advisory.entity.LocationMode;
import com.smartagri.advisory.security.ApiKeyInterceptor;
import com.smartagri.advisory.service.FarmFieldService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FarmFieldController.class)
@Import({ApiKeyInterceptor.class, WebMvcSecurityConfig.class})
@TestPropertySource(properties = "app.security.api-key=test-api-key")
class FarmFieldControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FarmFieldService farmFieldService;

    @Test
    void shouldRejectRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/field"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectDeleteWithoutApiKey() throws Exception {
        mockMvc.perform(delete("/api/field/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldDeleteFieldWithValidApiKey() throws Exception {
        mockMvc.perform(delete("/api/field/5").header("X-API-KEY", "test-api-key"))
                .andExpect(status().isNoContent());
        verify(farmFieldService).deleteById(5L);
    }

    @Test
    void shouldReturnFieldsWithValidApiKey() throws Exception {
        when(farmFieldService.getAll()).thenReturn(List.of(
                new FarmFieldResponse(1L, "Field 1", "Wheat", 2.0, "Sangli",
                        LocationMode.AUTO_GPS, 16.8, 74.5, LocalDate.of(2026, 4, 1), 40)
        ));

        mockMvc.perform(get("/api/field").header("X-API-KEY", "test-api-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fieldName").value("Field 1"))
                .andExpect(jsonPath("$[0].cropAgeDays").value(40));
    }
}
