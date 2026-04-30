package com.yas.sampledata.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SampleDataController.class)
@AutoConfigureMockMvc(addFilters = false)
class SampleDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SampleDataService sampleDataService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSampleData_WhenValidRequest_ShouldReturnSuccess() throws Exception {

        SampleDataVm requestVm = new SampleDataVm("Start");
        SampleDataVm responseVm =
                new SampleDataVm("Insert Sample Data successfully!");

        when(sampleDataService.createSampleData())
                .thenReturn(responseVm);

        mockMvc.perform(post("/storefront/sampledata")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestVm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Insert Sample Data successfully!"));
    }
}