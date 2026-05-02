package com.yas.backofficebff.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuthenticatedUserTest {

    @Test
    void username_whenRecordCreated_thenExposeValue() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser("member3");

        assertEquals("member3", authenticatedUser.username());
    }
}
