filetype
package br.com.meta3.java.scaffold.infrastructure.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegacyDaoImpl.executePreparedQuery behavior:
 *  - returns the PreparedStatement provided by Connection.prepareStatement
 *  - sets the private instance field 'pstmt' to the returned PreparedStatement
 *  - wraps SQLException thrown by prepareStatement into UnsupportedOperationException
 *
 * Notes on testing decisions:
 * - LegacyDaoImpl holds JDBC resources as private fields (conn, pstmt, rs). We access 'pstmt' via reflection
 *   to assert it was assigned as legacy code expects. Reflection is used only in tests to validate legacy-like state.
 * - The connect(null) path uses the injected DataSource to obtain a Connection. We mock DataSource and Connection
 *   to control behavior deterministically.
 *
 * TODO: (REVIEW) If LegacyDaoImpl exposes accessor methods for testing in the future, replace reflection with direct calls.
 */
@ExtendWith(MockitoExtension.class)
public class LegacyDaoImplExecutePreparedQueryTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    /**
     * Verifies that executePreparedQuery returns a PreparedStatement and that the instance field 'pstmt'
     * inside LegacyDaoImpl is set to that PreparedStatement.
     */
    @Test
    void testExecutePreparedQuery_returnsPreparedStatementAndSetsPstmt() throws Exception {
        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        String sql = "SELECT 1 FROM DUAL";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(ps);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        // Act
        PreparedStatement returned = legacyDao.executePreparedQuery(sql);

        // Assert - returned object is the mock
        assertSame(ps, returned, "executePreparedQuery should return the PreparedStatement produced by Connection.prepareStatement");

        // Assert - private field 'pstmt' was set to the same PreparedStatement
        Field pstmtField = LegacyDaoImpl.class.getDeclaredField("pstmt");
        pstmtField.setAccessible(true);
        Object fieldValue = pstmtField.get(legacyDao);
        assertSame(ps, fieldValue, "LegacyDaoImpl.pstmt should reference the last prepared statement");

        // Verify interactions
        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).prepareStatement(sql);
        // No unexpected interactions
        verifyNoMoreInteractions(dataSource, conn, ps);
    }

    /**
     * Verifies that when Connection.prepareStatement throws SQLException, executePreparedQuery
     * wraps it into UnsupportedOperationException and does not set the 'pstmt' field.
     */
    @Test
    void testExecutePreparedQuery_throwsUnsupportedOperationExceptionOnSQLException() throws Exception {
        // Arrange
        Connection conn = mock(Connection.class);
        String sql = "SELECT 1 FROM DUAL";
        SQLException sqlEx = new SQLException("prepare failed");

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenThrow(sqlEx);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        // Act & Assert
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> legacyDao.executePreparedQuery(sql),
                "executePreparedQuery should wrap SQLException into UnsupportedOperationException");

        // The message should match the legacy message for easier diagnosis
        assertTrue(ex.getMessage().contains("Erro ao preparar a query"), "Wrapped exception message should indicate prepare failure");
        // The cause should be the original SQLException
        assertNotNull(ex.getCause(), "Wrapped exception should contain the original SQLException as cause");
        assertEquals(sqlEx, ex.getCause(), "Cause of the wrapped exception should be the original SQLException");

        // Ensure 'pstmt' field was not set (remains null)
        Field pstmtField = LegacyDaoImpl.class.getDeclaredField("pstmt");
        pstmtField.setAccessible(true);
        Object fieldValue = pstmtField.get(legacyDao);
        assertNull(fieldValue, "LegacyDaoImpl.pstmt should remain null when prepareStatement fails");

        // Verify interactions
        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).prepareStatement(sql);
        verifyNoMoreInteractions(dataSource, conn);
    }
}