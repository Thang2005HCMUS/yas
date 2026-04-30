package com.yas.sampledata.utils;

import com.yas.sampledata.viewmodel.ErrorVm;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UtilityTest {

    @Test
    void testMessagesUtils() {
        // Test trường hợp không tìm thấy key trong bundle (sẽ trả về chính key đó)
        String msg = MessagesUtils.getMessage("non.existent.key");
        assertEquals("non.existent.key", msg);
    }

    @Test
    void testViewModels() {
        // Cover ErrorVm
        ErrorVm error = new ErrorVm("400", "Bad Request", "Detail");
        assertEquals("400", error.statusCode());
        assertTrue(error.fieldErrors().isEmpty());

        // Cover SampleDataVm
        SampleDataVm vm = new SampleDataVm("Success");
        assertEquals("Success", vm.message());
    }
}