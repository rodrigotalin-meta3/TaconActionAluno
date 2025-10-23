filetype
package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.services.LegacyDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LegacyDaoAdapter.executarQuery
 *
 * Verifies:
 *  - successful execution stores the ResultSet in the adapter (getLastResultSet)
 *  - runtime exceptions from underlying LegacyDao are wrapped into UnsupportedOperationException
 *    with the legacy message "Erro ao realizar transação com o banco de dados!"
 *
 * Notes on testing decisions:
 * - LegacyDao.executeQuery declares SQLException, but LegacyDaoImpl converts SQLExceptions into
 *   unchecked UnsupportedOperationException. To validate adapter's exception wrapping behavior we
 *   simulate a runtime exception (UnsupportedOperationException) as the underlying error.
 *   // TODO: (REVIEW) If future implementations of LegacyDao propagate checked SQLExceptions here,
 *   // we should add a separate test to assert behavior when a checked SQLException is thrown.
 */
@ExtendWith(MockitoExtension.class)
public class LegacyDaoAdapterExecutarQueryTest {

    @Mock
    private LegacyDao legacyDao;

    /**
     * Success path: ensure the ResultSet returned by legacyDao.executeQuery(...) is returned
     * by the adapter and stored in getLastResultSet().
     */
    @Test
    void testExecutarQuery_storesResultSet() throws Exception {
        LegacyDaoAdapter adapter = new LegacyDaoAdapter(legacyDao);

        ResultSet mockRs = mock(ResultSet.class);
        when(legacyDao.executeQuery(anyString())).thenReturn(mockRs);

        String sql = "SELECT * FROM DUMMY";
        ResultSet returned = adapter.executarQuery(sql);

        // returned ResultSet should be the same as mocked
        assertSame(mockRs, returned, "executarQuery should return the ResultSet produced by LegacyDao");

        // adapter should store the last ResultSet for legacy compatibility
        assertSame(mockRs, adapter.getLastResultSet(), "getLastResultSet should reference the ResultSet returned by executarQuery");

        verify(legacyDao, times(1)).executeQuery(sql);
        verifyNoMoreInteractions(legacyDao);
    }

    /**
     * Error path: when the underlying LegacyDao throws a runtime exception (as LegacyDaoImpl does
     * by wrapping SQLExceptions), the adapter must wrap it into UnsupportedOperationException
     * with the legacy message.
     *
     * Also ensures adapter does not retain a ResultSet reference after failure.
     */
    @Test
    void testExecutarQuery_wrapsRuntimeExceptionWithLegacyMessage() throws Exception {
        LegacyDaoAdapter adapter = new LegacyDaoAdapter(legacyDao);

        // Simulate underlying unchecked exception (LegacyDaoImpl often throws UnsupportedOperationException)
        RuntimeException underlying = new UnsupportedOperationException("driver failure");
        when(legacyDao.executeQuery(anyString())).thenThrow(underlying);

        String sql = "SELECT BAD FROM NONE";

        UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class,
                () -> adapter.executarQuery(sql),
                "executarQuery should wrap runtime exceptions into UnsupportedOperationException");

        // Message must match the legacy text used by the adapter
        assertEquals("Erro ao realizar transação com o banco de dados!", ex.getMessage());

        // The original cause should be preserved for diagnostics
        assertNotNull(ex.getCause(), "Wrapped exception should contain original cause");
        assertSame(underlying, ex.getCause(), "Cause should be the original runtime exception thrown by LegacyDao");

        // Ensure adapter did not set the last ResultSet on failure
        assertNull(adapter.getLastResultSet(), "Adapter should not retain a ResultSet reference after a failed execution");

        verify(legacyDao, times(1)).executeQuery(sql);
        verifyNoMoreInteractions(legacyDao);

        // TODO: (REVIEW) Consider adding a test for checked SQLException propagation if LegacyDao implementations change to throw SQLException directly.
    }
}