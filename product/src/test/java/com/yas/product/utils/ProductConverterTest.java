package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ProductConverterTest {

    @Test
    void toSlug_whenInputHasSpacesAndSpecialChars_thenNormalize() {
        String slug = ProductConverter.toSlug("  MacBook Pro 16\" 2026  ");

        assertEquals("macbook-pro-16-2026", slug);
    }

    @Test
    void toSlug_whenInputStartsWithSpecialChars_thenRemoveLeadingDash() {
        String slug = ProductConverter.toSlug(" @@@Hello---World ");

        assertEquals("hello-world", slug);
    }
}
