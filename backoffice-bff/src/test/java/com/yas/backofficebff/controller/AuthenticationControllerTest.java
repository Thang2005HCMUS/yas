package com.yas.backofficebff.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.yas.backofficebff.viewmodel.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private OAuth2User principal;

    private final AuthenticationController authenticationController = new AuthenticationController();

    @Test
    void user_whenPrincipalProvided_thenReturnAuthenticatedUser() {
        when(principal.getAttribute("preferred_username")).thenReturn("jason");

        ResponseEntity<AuthenticatedUser> response = authenticationController.user(principal);

        assertNotNull(response.getBody());
        assertEquals("jason", response.getBody().username());
    }
}
