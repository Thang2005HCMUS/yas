package com.yas.storefrontbff.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ViewModelTest {

    @Test
    void cartItemFromCartDetailVm_whenCalled_thenMapFields() {
        CartDetailVm cartDetailVm = new CartDetailVm(10L, 20L, 3);

        CartItemVm cartItemVm = CartItemVm.fromCartDetailVm(cartDetailVm);

        assertEquals(20L, cartItemVm.productId());
        assertEquals(3, cartItemVm.quantity());
    }

    @Test
    void records_whenCreated_thenExposeValues() {
        AuthenticatedUserVm authenticatedUserVm = new AuthenticatedUserVm("user-a");
        AuthenticationInfoVm authenticationInfoVm = new AuthenticationInfoVm(true, authenticatedUserVm);
        GuestUserVm guestUserVm = new GuestUserVm("id-1", "a@b.com", "pwd");
        TokenResponseVm tokenResponseVm = new TokenResponseVm("access", "refresh");
        CartDetailVm cartDetailVm = new CartDetailVm(1L, 2L, 4);
        CartGetDetailVm cartGetDetailVm = new CartGetDetailVm(7L, "customer-x", List.of(cartDetailVm));

        assertEquals("user-a", authenticatedUserVm.username());
        assertTrue(authenticationInfoVm.isAuthenticated());
        assertNotNull(authenticationInfoVm.authenticatedUser());
        assertEquals("id-1", guestUserVm.userId());
        assertEquals("a@b.com", guestUserVm.email());
        assertEquals("pwd", guestUserVm.password());
        assertEquals("access", tokenResponseVm.accessToken());
        assertEquals("refresh", tokenResponseVm.refreshToken());
        assertEquals(1, cartGetDetailVm.cartDetails().size());
        assertEquals(4, cartGetDetailVm.cartDetails().getFirst().quantity());
    }

    @Test
    void serviceUrlConfig_whenCreated_thenExposeServiceMap() {
        Map<String, String> services = Map.of("cart", "http://localhost:8081");
        var serviceUrlConfig = new com.yas.storefrontbff.config.ServiceUrlConfig(services);

        assertFalse(serviceUrlConfig.services().isEmpty());
        assertEquals("http://localhost:8081", serviceUrlConfig.services().get("cart"));
    }
}
