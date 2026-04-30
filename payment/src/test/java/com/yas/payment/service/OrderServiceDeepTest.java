package com.yas.payment.service;

import com.yas.payment.config.ServiceUrlConfig;
import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.viewmodel.CheckoutStatusVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClient;

import java.util.function.Consumer;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpHeaders;

import org.springframework.security.core.context.SecurityContext;

class OrderServiceDeepTest {
    private RestClient restClient;
    private ServiceUrlConfig config;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        config = mock(ServiceUrlConfig.class);
        orderService = new OrderService(restClient, config);

        // Thiết lập SecurityContext để lấy JWT
        Authentication auth = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mock-token");
        when(auth.getPrincipal()).thenReturn(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void updateCheckoutStatus_FullCoverage() throws Throwable {
        RestClient restClient = mock(RestClient.class);
        ServiceUrlConfig config = mock(ServiceUrlConfig.class);
        OrderService service = new OrderService(restClient, config);

        // Mock Security Context[cite: 3]
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("valid-token");
        when(auth.getPrincipal()).thenReturn(jwt);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(config.order()).thenReturn("http://order-service");

        // Mock RestClient Fluent API
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        
        when(restClient.put()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.net.URI.class))).thenReturn(uriSpec);
        when(uriSpec.headers(any())).thenReturn(uriSpec);
        when(uriSpec.body(any())).thenReturn(uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Long.class)).thenReturn(123L);

        CapturedPayment payment = CapturedPayment.builder()
                .checkoutId("CHK-01").paymentStatus(PaymentStatus.COMPLETED).build();

        service.updateCheckoutStatus(payment);

        // Kích hoạt Lambda để phủ code
        verify(uriSpec).headers(argThat(consumer -> {
            HttpHeaders headers = new HttpHeaders();
            ((Consumer<HttpHeaders>) consumer).accept(headers);
            return headers.getFirst(HttpHeaders.AUTHORIZATION).contains("valid-token");
        }));
    }
}