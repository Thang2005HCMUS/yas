package com.yas.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.CapturePaymentResponseVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentResponseVm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Spring Boot 4.0.2 Controller Unit Test
 */
@WebMvcTest(
        controllers = PaymentController.class,
        excludeAutoConfiguration = {
                OAuth2ResourceServerAutoConfiguration.class
        }
)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("POST /init -> should init payment success")
    void initPayment_success() throws Exception {

        InitPaymentRequestVm request = InitPaymentRequestVm.builder()
                .paymentMethod("PAYPAL")
                .checkoutId("CHK001")
                .totalPrice(new BigDecimal("100.00"))
                .build();

        InitPaymentResponseVm response = InitPaymentResponseVm.builder()
                .status("CREATED")
                .paymentId("PAY001")
                .redirectUrl("https://paypal.com/checkout")
                .build();

        when(paymentService.initPayment(any(InitPaymentRequestVm.class)))
                .thenReturn(response);

        mockMvc.perform(post("/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.paymentId").value("PAY001"))
                .andExpect(jsonPath("$.redirectUrl").value("https://paypal.com/checkout"));

        verify(paymentService).initPayment(any(InitPaymentRequestVm.class));
    }

    @Test
    @DisplayName("POST /capture -> should capture payment success")
    void capturePayment_success() throws Exception {

        CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
                .paymentMethod("PAYPAL")
                .token("TOKEN123")
                .build();

        CapturePaymentResponseVm response = CapturePaymentResponseVm.builder()
                .orderId(1L)
                .checkoutId("CHK001")
                .amount(new BigDecimal("100.00"))
                .paymentFee(new BigDecimal("2.00"))
                .gatewayTransactionId("TXN001")
                .failureMessage(null)
                .build();

        when(paymentService.capturePayment(any(CapturePaymentRequestVm.class)))
                .thenReturn(response);

        mockMvc.perform(post("/capture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.checkoutId").value("CHK001"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.paymentFee").value(2.00))
                .andExpect(jsonPath("$.gatewayTransactionId").value("TXN001"));

        verify(paymentService).capturePayment(any(CapturePaymentRequestVm.class));
    }

    @Test
    @DisplayName("GET /cancel -> should return cancelled message")
    void cancelPayment_success() throws Exception {

        mockMvc.perform(get("/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment cancelled"));
    }
}