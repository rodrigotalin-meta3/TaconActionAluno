filetype
package br.com.meta3.java.scaffold.infrastructure.repositories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegacyDaoImpl.prepareInsert behavior:
 *  - returns the PreparedStatement provided by Connection.prepareStatement
 *  - sets the private instance field 'pstmt' to the returned PreparedStatement
 *  - wraps SQLException thrown by prepareStatement into UnsupportedOperationException with legacy message
 *
 * Notes on testing decisions:
 * - We reuse the approach from existing tests that access private fields via reflection to assert
 *   legacy-like stateful behavior (pstmt assignment). Reflection is used only in tests for validation.
 * - This test mocks DataSource and Connection to avoid real JDBC operations and to deterministically simulate success/failure.
 *
 * TODO: (REVIEW) Consider exposing test-only accessors or avoiding reflection by providing package-private getters
 *                 in LegacyDaoImpl for testing purposes in future refactors.
 */
@ExtendWith(MockitoExtension.class)
public class LegacyDaoImplPrepareInsertTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    /**
     * Success path:
     * - prepareInsert should return the PreparedStatement produced by Connection.prepareStatement
     * - LegacyDaoImpl.pstmt private field must reference the same PreparedStatement
     * - DataSource.getConnection() and Connection.prepareStatement(...) should be invoked
     */
    @Test
    void testPrepareInsert_returnsPreparedStatementAndSetsPstmt() throws Exception {
        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        String sql = "INSERT INTO DUMMY (ID) VALUES (?)";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(ps);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        // Act
        PreparedStatement returned = legacyDao.prepareInsert(sql);

        // Assert - returned object is the same PreparedStatement produced by Connection.prepareStatement
        assertSame(ps, returned, "prepareInsert should return the PreparedStatement produced by Connection.prepareStatement");

        // Assert - private field 'pstmt' was set to the same PreparedStatement
        Field pstmtField = LegacyDaoImpl.class.getDeclaredField("pstmt");
        pstmtField.setAccessible(true);
        Object fieldValue = pstmtField.get(legacyDao);
        assertSame(ps, fieldValue, "LegacyDaoImpl.pstmt should reference the last prepared statement after prepareInsert");

        // Verify lifecycle interactions
        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).prepareStatement(sql);

        // Ensure no unexpected interactions
        verifyNoMoreInteractions(dataSource, conn, ps);
    }

    /**
     * Failure path:
     * - When Connection.prepareStatement throws SQLException, prepareInsert should wrap it into UnsupportedOperationException
     *   with a message containing "Erro ao preparar o insert" and the cause should be the original SQLException.
     * - The private 'pstmt' field must remain null when preparation fails.
     */
    @Test
    void testPrepareInsert_wrapsSQLExceptionIntoUnsupportedOperationException() throws Exception {
        // Arrange
        Connection conn = mock(Connection.class);
        String sql = "INSERT INTO DUMMY (ID) VALUES (?)";
        SQLException sqlEx = new SQLException("prepare failed");

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenThrow(sqlEx);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        // Act & Assert
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> legacyDao.prepareInsert(sql),
                "prepareInsert should wrap SQLException into UnsupportedOperationException");

        // Message should indicate prepare insert failure (legacy-style message)
        assertTrue(ex.getMessage().contains("Erro ao preparar o insert"),
                "Wrapped exception message should indicate prepare insert failure");

        // The cause should be the original SQLException
        assertNotNull(ex.getCause(), "Wrapped exception should contain the original SQLException as cause");
        assertEquals(sqlEx, ex.getCause(), "Cause of the wrapped exception should be the original SQLException");

        // Ensure 'pstmt' field was not set (remains null)
        Field pstmtField = LegacyDaoImpl.class.getDeclaredField("pstmt");
        pstmtField.setAccessible(true);
        Object fieldValue = pstmtField.get(legacyDao);
        assertNull(fieldValue, "LegacyDaoImpl.pstmt should remain null when prepareInsert fails");

        // Verify interactions
        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).prepareStatement(sql);
        verifyNoMoreInteractions(dataSource, conn);
    }
}