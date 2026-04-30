package com.yas.sampledata.service;

import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SampleDataServiceTest {

    private SampleDataService sampleDataService;
    private DataSource productDataSource;
    private DataSource mediaDataSource;

    @BeforeEach
    void setUp() {
        productDataSource = mock(DataSource.class);
        mediaDataSource = mock(DataSource.class);
        sampleDataService = new SampleDataService(productDataSource, mediaDataSource);
    }

    @Test
    void createSampleData_ShouldReturnSuccessMessage() {
        // Execute
        SampleDataVm result = sampleDataService.createSampleData();

        // Verify
        assertEquals("Insert Sample Data successfully!", result.message());
        // Lưu ý: Vì SqlScriptExecutor được khởi tạo bằng 'new', 
        // unit test này chủ yếu cover luồng trả về message.
    }
}