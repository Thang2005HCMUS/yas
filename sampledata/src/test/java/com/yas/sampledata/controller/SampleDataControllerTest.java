package com.yas.sampledata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.sampledata.config.SecurityConfig; // Thêm import này
import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import; // Sử dụng @Import thay vì @AutoConfigureMockMvc(addFilters = false)
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Thêm để giả lập user
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SampleDataController.class)
@Import(SecurityConfig.class) // QUAN TRỌNG: Import SecurityConfig để cung cấp HttpSecurity bean
class SampleDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SampleDataService sampleDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser // Giả lập user đã đăng nhập để vượt qua lớp bảo mật
    void createSampleData_WhenValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        SampleDataVm requestVm = new SampleDataVm("Start");
        SampleDataVm responseVm = new SampleDataVm("Insert Sample Data successfully!");

        when(sampleDataService.createSampleData())
                .thenReturn(responseVm);

        // When & Then
        mockMvc.perform(post("/storefront/sampledata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestVm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Insert Sample Data successfully!"));
    }
}