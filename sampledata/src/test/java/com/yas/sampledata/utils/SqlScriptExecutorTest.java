package com.yas.sampledata.utils;

import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

class SqlScriptExecutorTest {

    @Test
    void executeScriptsForSchema_HandleExceptionGracefully() {
        SqlScriptExecutor executor = new SqlScriptExecutor();
        DataSource dataSource = mock(DataSource.class);
        
        // Test với pattern không tồn tại để check việc bắt lỗi
        executor.executeScriptsForSchema(dataSource, "public", "non-existent-path/*.sql");
        
        // Kiểm tra xem không có connection nào được gọi vì không tìm thấy file
        try {
            verify(dataSource, times(0)).getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}