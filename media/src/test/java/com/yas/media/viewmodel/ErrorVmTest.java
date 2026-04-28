package com.yas.media.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void constructorWithoutFieldErrors_whenCalled_thenInitializeEmptyFieldErrors() {
        ErrorVm error = new ErrorVm("400", "Bad request", "Invalid input");

        assertEquals("400", error.statusCode());
        assertEquals("Bad request", error.title());
        assertEquals("Invalid input", error.detail());
        assertNotNull(error.fieldErrors());
        assertTrue(error.fieldErrors().isEmpty());
    }

    @Test
    void constructorWithFieldErrors_whenCalled_thenKeepProvidedErrors() {
        ErrorVm error = new ErrorVm("400", "Bad request", "Invalid input", List.of("fileName is required"));

        assertEquals(1, error.fieldErrors().size());
        assertEquals("fileName is required", error.fieldErrors().getFirst());
    }
}
