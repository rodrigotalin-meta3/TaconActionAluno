package br.com.meta3.java.scaffold.application.services;

import br.com.meta3.java.scaffold.domain.entities.ArquivoSecSmec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArquivoServiceImpl.
 *
 * Strategy:
 * - Mock JdbcTemplate and intercept the RowMapper provided by the service method call.
 * - Provide a mocked ResultSet with expected column values and invoke RowMapper.mapRow to simulate
 *   JdbcTemplate behavior. This verifies that the SQL result columns (nomearquivo, datarecebimento,
 *   quantidadealunos) are correctly mapped into ArquivoSecSmec fields.
 *
 * NOTE:
 * - We avoid asserting the full SQL string in the service to keep tests resilient to minor SQL formatting changes.
 * - We do assert the computed "like" pattern and that the date parameters are java.sql.Date instances
 *   to validate parameter construction logic.
 */
// TODO: (REVIEW) If future refactors convert ArquivoSecSmec.dateRecebimento to typed dates, update tests to assert typed values.
@ExtendWith(MockitoExtension.class)
public class ArquivoServiceImplTest {

    @Test
    void testListarArquivosEnviadosSecSmec_mappings() throws Exception {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

        ArquivoServiceImpl service = new ArquivoServiceImpl(jdbcTemplate);

        // Prepare a mocked ResultSet to be used by the RowMapper
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.getString("nomearquivo")).thenReturn("arquivo_exemplo.txt");
        when(mockRs.getString("datarecebimento")).thenReturn("15/09/2025 08:30:00");
        // getInt returns primitive int, stub with Integer (auto-unboxed)
        when(mockRs.getInt("quantidadealunos")).thenReturn(123);

        // Intercept the call to jdbcTemplate.query and invoke the provided RowMapper with our mocked ResultSet.
        // Return a list containing the single mapped ArquivoSecSmec.
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenAnswer(invocation -> {
                    RowMapper<ArquivoSecSmec> mapper = invocation.getArgument(2);
                    List<ArquivoSecSmec> list = new ArrayList<>();
                    // Simulate a single row mapped by RowMapper
                    list.add(mapper.mapRow(mockRs, 1));
                    return list;
                });

        // Call the service method under test
        String mes = "09";
        String anoBase = "2025";
        String codigoTitular = "9999"; // should select 'sec' branch with deslocamento=29

        List<ArquivoSecSmec> result = service.listarArquivosEnviadosSecSmec(mes, anoBase, codigoTitular);

        // Verify JdbcTemplate.query was invoked exactly once and capture the parameters array
        ArgumentCaptor<Object[]> paramsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, times(1)).query(anyString(), paramsCaptor.capture(), any(RowMapper.class));

        Object[] capturedParams = paramsCaptor.getValue();
        assertNotNull(capturedParams, "Captured parameters should not be null");
        assertEquals(3, capturedParams.length, "Expected three query parameters (likePattern, startDate, endDate)");

        // Assert like pattern construction (note escaping of backslashes)
        String expectedLike = "\\\\setps.com.br\\ftp\\sec\\2025%";
        assertEquals(expectedLike, capturedParams[0], "The LIKE pattern parameter should match expected legacy path pattern");

        // Assert date parameters are java.sql.Date and correspond to first day of the given month/year
        assertTrue(capturedParams[1] instanceof Date, "Second parameter should be java.sql.Date (start of month)");
        assertTrue(capturedParams[2] instanceof Date, "Third parameter should be java.sql.Date (start of month for last_day truncation)");

        // Verify mapping result content from mocked ResultSet
        assertNotNull(result, "Result list should not be null");
        assertEquals(1, result.size(), "Expected exactly one mapped ArquivoSecSmec item");

        ArquivoSecSmec item = result.get(0);
        assertEquals("arquivo_exemplo.txt", item.getNomeArquivo(), "nomearquivo should be mapped to nomeArquivo");
        assertEquals("15/09/2025 08:30:00", item.getDataRecebimento(), "datarecebimento should be mapped to dataRecebimento");
        assertEquals(123, item.getQuantidadeAlunos(), "quantidadealunos should be mapped to quantidadeAlunos");

        // Additional verification: ensure no unexpected interactions with JdbcTemplate
        verifyNoMoreInteractions(jdbcTemplate);
    }
}