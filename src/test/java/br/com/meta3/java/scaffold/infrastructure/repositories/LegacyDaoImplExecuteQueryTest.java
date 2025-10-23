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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegacyDaoImpl.executeQuery behavior:
 *  - returns the ResultSet produced by Statement.executeQuery
 *  - sets the private instance fields 'stmt' and 'rs' accordingly
 *  - wraps SQLException into UnsupportedOperationException with legacy message
 *
 * Notes on testing decisions:
 * - Tests access private fields 'stmt' and 'rs' via reflection to assert legacy-like stateful behavior.
 *   This mirrors other tests in the suite that validate internal resource assignments.
 * - We include separate failure scenarios for:
 *     1) Connection.createStatement() throwing SQLException (no stmt/rs assigned)
 *     2) Statement.executeQuery(...) throwing SQLException (stmt assigned, rs not assigned)
 *
 * TODO: (REVIEW) Consider exposing package-private test hooks in LegacyDaoImpl to avoid reflection in future test code.
 */
@ExtendWith(MockitoExtension.class)
public class LegacyDaoImplExecuteQueryTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private Connection conn;

    @Mock
    private Statement stmt;

    @Mock
    private ResultSet rs;

    private static final String LEGACY_ERROR_MESSAGE = "Erro ao realizar transação com o banco de dados!";

    /**
     * Success path:
     * - executeQuery should return the ResultSet produced by Statement.executeQuery
     * - LegacyDaoImpl.stmt and LegacyDaoImpl.rs fields should reference the Statement and ResultSet respectively
     */
    @Test
    void testExecuteQuery_returnsResultSetAndSetsStmtAndRs() throws Exception {
        String sql = "SELECT 1 FROM DUAL";

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(sql)).thenReturn(rs);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        ResultSet returned = legacyDao.executeQuery(sql);

        // Returned ResultSet must be the same object produced by stmt.executeQuery
        assertSame(rs, returned, "executeQuery should return the ResultSet produced by Statement.executeQuery");

        // Verify private fields 'stmt' and 'rs' were set
        Field stmtField = LegacyDaoImpl.class.getDeclaredField("stmt");
        Field rsField = LegacyDaoImpl.class.getDeclaredField("rs");
        stmtField.setAccessible(true);
        rsField.setAccessible(true);

        Object stmtValue = stmtField.get(legacyDao);
        Object rsValue = rsField.get(legacyDao);

        assertSame(stmt, stmtValue, "LegacyDaoImpl.stmt should reference the Statement created for the query");
        assertSame(rs, rsValue, "LegacyDaoImpl.rs should reference the ResultSet returned by executeQuery");

        // Verify JDBC interactions
        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).createStatement();
        verify(stmt, times(1)).executeQuery(sql);

        verifyNoMoreInteractions(dataSource, conn, stmt, rs);
    }

    /**
     * Failure path A: Connection.createStatement() throws SQLException.
     * Expectation:
     * - executeQuery wraps SQLException into UnsupportedOperationException with legacy message
     * - no stmt/rs fields set (both remain null)
     */
    @Test
    void testExecuteQuery_wrapsSQLExceptionOnCreateStatement() throws Exception {
        String sql = "SELECT BAD FROM NONE";
        SQLException sqlEx = new SQLException("createStatement failed");

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenThrow(sqlEx);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> legacyDao.executeQuery(sql),
                "executeQuery should wrap SQLException from createStatement into UnsupportedOperationException");

        assertEquals(LEGACY_ERROR_MESSAGE, ex.getMessage(), "Wrapped exception message must match legacy message");
        assertNotNull(ex.getCause(), "Wrapped exception should contain the original cause");
        assertEquals(sqlEx, ex.getCause(), "Cause should be the original SQLException");

        // Verify private fields 'stmt' and 'rs' are not set (remain null)
        Field stmtField = LegacyDaoImpl.class.getDeclaredField("stmt");
        Field rsField = LegacyDaoImpl.class.getDeclaredField("rs");
        stmtField.setAccessible(true);
        rsField.setAccessible(true);

        Object stmtValue = stmtField.get(legacyDao);
        Object rsValue = rsField.get(legacyDao);

        assertNull(stmtValue, "LegacyDaoImpl.stmt should be null when createStatement fails");
        assertNull(rsValue, "LegacyDaoImpl.rs should be null when createStatement fails");

        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).createStatement();
        verifyNoMoreInteractions(dataSource, conn);
    }

    /**
     * Failure path B: Statement.executeQuery(...) throws SQLException.
     * Expectation:
     * - executeQuery wraps SQLException into UnsupportedOperationException with legacy message
     * - stmt field is set (since createStatement succeeded), rs remains null
     */
    @Test
    void testExecuteQuery_wrapsSQLExceptionOnExecuteQuery() throws Exception {
        String sql = "SELECT WILL_FAIL FROM DUAL";
        SQLException sqlEx = new SQLException("executeQuery failed");

        when(dataSource.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(sql)).thenThrow(sqlEx);

        LegacyDaoImpl legacyDao = new LegacyDaoImpl(dataSource, jdbcTemplate);

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> legacyDao.executeQuery(sql),
                "executeQuery should wrap SQLException from executeQuery into UnsupportedOperationException");

        assertEquals(LEGACY_ERROR_MESSAGE, ex.getMessage(), "Wrapped exception message must match legacy message");
        assertNotNull(ex.getCause(), "Wrapped exception should contain the original cause");
        assertEquals(sqlEx, ex.getCause(), "Cause should be the original SQLException");

        // Verify private fields: stmt should be set, rs should be null
        Field stmtField = LegacyDaoImpl.class.getDeclaredField("stmt");
        Field rsField = LegacyDaoImpl.class.getDeclaredField("rs");
        stmtField.setAccessible(true);
        rsField.setAccessible(true);

        Object stmtValue = stmtField.get(legacyDao);
        Object rsValue = rsField.get(legacyDao);

        assertSame(stmt, stmtValue, "LegacyDaoImpl.stmt should reference the Statement even if executeQuery fails");
        assertNull(rsValue, "LegacyDaoImpl.rs should be null when executeQuery fails");

        verify(dataSource, times(1)).getConnection();
        verify(conn, times(1)).createStatement();
        verify(stmt, times(1)).executeQuery(sql);
        verifyNoMoreInteractions(dataSource, conn, stmt);
    }
}