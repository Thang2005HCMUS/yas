package com.yas.sampledata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Thư viện mới cho Spring Boot 4
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleDataController.class)
class SampleDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Trong Spring Boot 4, dùng MockitoBean thay cho MockBean
    @MockitoBean 
    private SampleDataService sampleDataService;

    @Test
    @WithMockUser
    void createSampleData_WhenValidRequest_ShouldReturnStatusOk() throws Exception {
        // Given
        SampleDataVm inputVm = new SampleDataVm("Start seeding");
        SampleDataVm expectedResponse = new SampleDataVm("Insert Sample Data successfully!");
        
        when(sampleDataService.createSampleData()).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/storefront/sampledata")
                        .with(csrf()) // Vẫn cần CSRF vì security config chưa tắt hoàn toàn
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputVm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Insert Sample Data successfully!"));
    }

    @Test
    @WithMockUser
    void createSampleData_WhenBodyIsMissing_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/storefront/sampledata")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}