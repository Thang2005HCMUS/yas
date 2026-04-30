package com.yas.sampledata.service;

import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
        // Thực thi
        SampleDataVm result = sampleDataService.createSampleData();

        // Kiểm tra
        assertEquals("Insert Sample Data successfully!", result.message());
        // Vì SqlScriptExecutor được khởi tạo bằng 'new' trong code chính, 
        // chúng ta khó mock sâu hơn nếu không dùng PowerMock, 
        // nhưng logic service vẫn được bao phủ.
    }
}