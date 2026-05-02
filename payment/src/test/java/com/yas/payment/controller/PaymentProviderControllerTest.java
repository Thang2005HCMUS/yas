// package com.yas.payment.controller;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.yas.payment.service.PaymentProviderService;
// import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
// import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;

// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
// import org.springframework.context.annotation.Bean;
// import org.springframework.test.context.bean.override.mockito.MockitoBean;
// import org.springframework.http.MediaType;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(PaymentProviderController.class)
// @AutoConfigureMockMvc(addFilters = false)
// class PaymentProviderControllerTest {
//     @Autowired
//     private MockMvc mockMvc;

//     @MockitoBean
//     private PaymentProviderService paymentProviderService;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @Test
//     void create_shouldReturnCreated() throws Exception {
//         CreatePaymentVm vm = new CreatePaymentVm();
//         vm.setId("paypal");
//         vm.setName("Paypal");

//         when(paymentProviderService.create(any())).thenReturn(new PaymentProviderVm("paypal", "Paypal", null, 0, null, null));

//         mockMvc.perform(post("/backoffice/payment-providers")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(vm)))
//                 .andExpect(status().isCreated());
//     }
//     @TestConfiguration
// static class TestSecurityConfig {
//     @Bean
//     public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
//         return http.csrf(csrf -> csrf.disable())
//                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//                    .build();
//     }
// }
// }