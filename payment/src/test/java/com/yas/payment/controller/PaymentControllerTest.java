package com.yas.payment.controller;

import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.CapturePaymentResponseVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentResponseVm;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false) // Tắt bộ lọc Security để tập trung test logic Controller
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void initPayment_ShouldReturnOk() throws Exception {
        // Given
        InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .totalPrice(BigDecimal.valueOf(100))
                .checkoutId("check-123")
                .build();
        
        InitPaymentResponseVm response = InitPaymentResponseVm.builder()
                .status("CREATED")
                .paymentId("pay-001")
                .redirectUrl("http://paypal.com/approve")
                .build();

        when(paymentService.initPayment(any(InitPaymentRequestVm.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/init")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void capturePayment_ShouldReturnResponse() throws Exception {
        // Given
        CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .token("token-abc")
                .build();

        CapturePaymentResponseVm response = CapturePaymentResponseVm.builder()
                .paymentStatus(PaymentStatus.COMPLETED)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(paymentService.capturePayment(any(CapturePaymentRequestVm.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/capture")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void cancelPayment_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment cancelled"));
    }
}