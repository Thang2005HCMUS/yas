package com.yas.sampledata.utils;

import com.yas.sampledata.viewmodel.ErrorVm;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UtilsAndVmTest {

    @Test
    void testErrorVm() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Detail");
        assertEquals("400", errorVm.statusCode());
        assertEquals(0, errorVm.fieldErrors().size());

        ErrorVm errorWithFields = new ErrorVm("400", "Title", "Detail", List.of("error1"));
        assertEquals(1, errorWithFields.fieldErrors().size());
    }

    @Test
    void testSampleDataVm() {
        SampleDataVm vm = new SampleDataVm("test message");
        assertEquals("test message", vm.message());
    }

    @Test
    void testMessagesUtils() {
        // Test trường hợp không tìm thấy key trong resource bundle
        String message = MessagesUtils.getMessage("non.existent.key", "param1");
        assertNotNull(message);
        assertEquals("non.existent.key", message);
    }
}