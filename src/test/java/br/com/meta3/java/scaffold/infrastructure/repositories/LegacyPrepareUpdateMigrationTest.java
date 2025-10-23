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
 * Unit tests for LegacyDaoImpl.prepareUpdate delegation behavior.
 *
 * Goals:
 * - Ensure prepareUpdate delegates to prepareInsert (implemented as a direct call)
 *   and that the instance field 'pstmt' is set to the PreparedStatement produced by the Connection.
 * - Verify lifecycle calls to DataSource.getConnection() and Connection.prepareStatement(...)
 *   are performed as part of preparation.
 * - Verify behavior when Connection.prepareStatement throws SQLException: the method wraps/propagates
 *   the legacy-style unchecked exception and does not set the internal 'pstmt' field.
 *
 * Notes:
 * - These tests use reflection to inspect private field 'pstmt' to assert legacy-like stateful behavior.
 * - TODO: (REVIEW) Consider exposing test-only accessors or avoiding reflection in future refactors.
 */
@ExtendWith(MockitoExtension.class)
public class LegacyPrepareUpdateMigrationTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    /**
     * Success path:
     * - prepareUpdate should return the PreparedStatement produced by Connection.prepareStatement
     * - LegacyDaoImpl.pstmt private field must reference the same PreparedStatement
     * - DataSource.getConnection() and Connection.prepareStatement(...) should be invoked
     */
    @Test
    void testPrepareUpdate_delegatesToPrepareInsert_andSetsPstmt() throws Exception {
        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        String sql = "UPDATE DUMMY SET VAL = ?";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenReturn(ps);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        // Act
        PreparedStatement returned = legacyDao.prepareUpdate(sql);

        // Assert - returned object is the same PreparedStatement produced by Connection.prepareStatement
        assertSame(ps, returned, "prepareUpdate should return the PreparedStatement produced by Connection.prepareStatement");

        // Assert - private field 'pstmt' was set to the same PreparedStatement
        Field pstmtField = LegacyDaoImpl.class.getDeclaredField("pstmt");
        pstmtField.setAccessible(true);
        Object fieldValue = pstmtField.get(legacyDao);
        assertSame(ps, fieldValue, "LegacyDaoImpl.pstmt should reference the last prepared statement after prepareUpdate");

        // Verify lifecycle interactions
        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).prepareStatement(sql);

        // Ensure no unexpected interactions
        verifyNoMoreInteractions(dataSource, conn, ps);
    }

    /**
     * Failure path:
     * - When Connection.prepareStatement throws SQLException, prepareUpdate should wrap/propagate
     *   the legacy-style unchecked exception (UnsupportedOperationException) and must not set 'pstmt'.
     */
    @Test
    void testPrepareUpdate_throwsUnsupportedOperationException_onSQLException() throws Exception {
        // Arrange
        Connection conn = mock(Connection.class);
        String sql = "UPDATE DUMMY SET VAL = ?";

        SQLException sqlEx = new SQLException("prepare failed");
        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.prepareStatement(sql)).thenThrow(sqlEx);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        // Act & Assert
        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> legacyDao.prepareUpdate(sql),
                "prepareUpdate should wrap SQLException into UnsupportedOperationException per legacy semantics");

        // Cause should be present and equal to original SQLException (or wrapped)
        assertNotNull(ex.getCause(), "Wrapped exception should contain the original SQLException as cause");

        // Ensure internal 'pstmt' field was not set
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