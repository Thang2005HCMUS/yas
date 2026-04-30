// src/test/java/com/yas/payment/config/SecurityConfigTest.java
package com.yas.payment.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

    // @Test
    // void jwtAuthenticationConverterForKeycloak_shouldMapRealmRolesToAuthorities() {
    //     SecurityConfig config = new SecurityConfig();

    //     JwtAuthenticationConverter converter =
    //             config.jwtAuthenticationConverterForKeycloak();

    //     Jwt jwt = Jwt.withTokenValue("token")
    //             .header("alg", "none")
    //             .claim("realm_access", Map.of(
    //                     "roles",
    //                     List.of("ADMIN", "USER")
    //             ))
    //             .build();

    //     Collection<GrantedAuthority> authorities =
    //             converter.convert(jwt).getAuthorities();

    //     assertThat(authorities)
    //             .extracting(GrantedAuthority::getAuthority)
    //             .containsExactlyInAnyOrder(
    //                     "ROLE_ADMIN",
    //                     "ROLE_USER"
    //             );
    // }

    @Test
    void jwtAuthenticationConverterForKeycloak_shouldReturnConverter() {
        SecurityConfig config = new SecurityConfig();

        JwtAuthenticationConverter converter =
                config.jwtAuthenticationConverterForKeycloak();

        assertThat(converter).isNotNull();
    }
}