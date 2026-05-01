package com.yas.storefrontbff.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yas.storefrontbff.viewmodel.AuthenticationInfoVm;
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
    void user_whenPrincipalMissing_thenReturnGuestInfo() {
        ResponseEntity<AuthenticationInfoVm> response = authenticationController.user(null);

        assertNotNull(response.getBody());
        assertFalse(response.getBody().isAuthenticated());
        assertNull(response.getBody().authenticatedUser());
    }

    @Test
    void user_whenPrincipalPresent_thenReturnAuthenticatedInfo() {
        when(principal.getAttribute("preferred_username")).thenReturn("member3");

        ResponseEntity<AuthenticationInfoVm> response = authenticationController.user(principal);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isAuthenticated());
        assertNotNull(response.getBody().authenticatedUser());
        assertTrue("member3".equals(response.getBody().authenticatedUser().username()));
    }
}
