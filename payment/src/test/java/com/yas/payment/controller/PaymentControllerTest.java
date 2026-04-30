package com.yas.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PaymentService paymentService;

    @Test
    void initPayment() throws Exception {

        var response = InitPaymentResponseVm.builder()
                .status("CREATED")
                .paymentId("PAY001")
                .redirectUrl("https://paypal.com")
                .build();

        given(paymentService.initPayment(any())).willReturn(response);

        var request = InitPaymentRequestVm.builder()
                .paymentMethod("PAYPAL")
                .checkoutId("CHK001")
                .totalPrice(new BigDecimal("100"))
                .build();

        mockMvc.perform(post("/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("PAY001"));

        verify(paymentService).initPayment(any());
    }

    @Test
    void capturePayment() throws Exception {

        var response = CapturePaymentResponseVm.builder()
                .orderId(1L)
                .checkoutId("CHK001")
                .build();

        given(paymentService.capturePayment(any())).willReturn(response);

        var request = CapturePaymentRequestVm.builder()
                .paymentMethod("PAYPAL")
                .token("TOKEN")
                .build();

        mockMvc.perform(post("/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));

        verify(paymentService).capturePayment(any());
    }

    @Test
    void cancelPayment() throws Exception {
        mockMvc.perform(get("/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment cancelled"));
    }
    @TestConfiguration
static class TestConfig {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
}