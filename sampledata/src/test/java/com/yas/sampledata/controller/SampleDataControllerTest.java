package com.yas.sampledata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleDataController.class)
@AutoConfigureMockMvc(addFilters = false) // Bỏ qua security layer để focus vào logic controller
class SampleDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SampleDataService sampleDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSampleData_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        SampleDataVm requestVm = new SampleDataVm("Start");
        SampleDataVm responseVm = new SampleDataVm("Insert Sample Data successfully!");
        when(sampleDataService.createSampleData()).thenReturn(responseVm);

        // When & Then
        mockMvc.perform(post("/storefront/sampledata")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestVm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Insert Sample Data successfully!"));
    }
}