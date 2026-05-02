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

import java.net.URI;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    // @Test
    // void updateCheckoutStatus_shouldExecuteHeaderLambda() {
    //     when(config.order()).thenReturn("http://order");
        
    //     RestClient.RequestBodyUriSpec bodySpec = mock(RestClient.RequestBodyUriSpec.class);
    //     RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    //     when(restClient.put()).thenReturn(bodySpec);
    //     when(bodySpec.uri(any(URI.class))).thenReturn(bodySpec);
    //     when(bodySpec.headers(any())).thenReturn(bodySpec);
    //     when(bodySpec.body(any(CheckoutStatusVm.class))).thenReturn(bodySpec);
    //     when(bodySpec.retrieve()).thenReturn(responseSpec);
    //     when(responseSpec.body(Long.class)).thenReturn(1L);

    //     CapturedPayment payment = CapturedPayment.builder()
    //             .checkoutId("C1").paymentStatus(PaymentStatus.COMPLETED).build();

    //     orderService.updateCheckoutStatus(payment);

    //     // Kiểm tra xem lambda headers có được gọi không
    //     ArgumentCaptor<Consumer> headerCaptor = ArgumentCaptor.forClass(Consumer.class);
    //     verify(bodySpec).headers(headerCaptor.capture());
        
    //     // Thực thi lambda thủ công để phủ code
    //     org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
    //     headerCaptor.getValue().accept(headers);
    //     assertThat(headers.getFirst("Authorization")).contains("Bearer mock-token");
    // }
}