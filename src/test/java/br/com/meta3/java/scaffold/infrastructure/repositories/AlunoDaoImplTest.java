package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.entities.Aluno;
import br.com.meta3.java.scaffold.domain.services.LegacyDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AlunoDaoImpl.inserirAluno.
 *
 * Goals:
 * - Mock LegacyDao.prepareInsert to return a PreparedStatement and verify it is used.
 * - Verify that string parameters are trimmed before binding.
 * - Verify LocalDate -> java.sql.Date conversion is performed when binding parameter 6.
 * - Verify null dataNascimento results in setNull on parameter 6.
 *
 * Notes:
 * - We mock LegacyDao to avoid real JDBC/DataSource interactions.
 * - We verify trimmed string values because AlunoDaoImpl uses safeString(...) which trims values.
 *
 * TODO: (REVIEW) Consider additional tests for SQLException flows (prepareInsert / executeUpdate throwing)
 *                and for executeUpdate returning 0 (should map to legacy failure code 0).
 */
@ExtendWith(MockitoExtension.class)
public class AlunoDaoImplTest {

    @Mock
    private LegacyDao legacyDao;

    @Mock
    private PreparedStatement pstmt;

    @Captor
    private ArgumentCaptor<java.sql.Date> sqlDateCaptor;

    /**
     * Verifies successful insertion path:
     * - prepareInsert is used to obtain a PreparedStatement
     * - parameters are bound (strings trimmed)
     * - LocalDate dataNascimento is converted to java.sql.Date and bound
     * - executeUpdate returning >0 maps to legacy success code 1
     */
    @Test
    void testInserirAluno_success_bindingsAndDateConversion() throws Exception {
        // Arrange
        when(legacyDao.prepareInsert(anyString())).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenReturn(1);

        AlunoDaoImpl dao = new AlunoDaoImpl(legacyDao);

        Aluno aluno = new Aluno();
        aluno.setCodigoSetps("  12345  ");
        aluno.setNomeAluno(" João da Silva ");
        aluno.setMatricula("  MT-001  ");
        aluno.setNomeMae(" Maria ");
        aluno.setNomePai(" José ");
        // Use a LocalDate to exercise the LocalDate -> java.sql.Date branch
        LocalDate birth = LocalDate.of(2010, 5, 20);
        aluno.setDataNascimento(birth);
        aluno.setEmail("  teste@example.com  ");

        String ipCliente = "192.168.0.1";
        String tipo = "INCLUSAO";

        // Act
        int result = dao.inserirAluno(aluno, ipCliente, tipo);

        // Assert
        assertEquals(1, result, "Expected legacy-success code 1 when executeUpdate returns >0");

        // Verify connection lifecycle delegated to LegacyDao
        verify(legacyDao, times(1)).connect("oracle");
        verify(legacyDao, times(1)).disconnect();

        // Verify prepareInsert was called to obtain the PreparedStatement
        verify(legacyDao, times(1)).prepareInsert(anyString());

        // Verify parameter bindings on PreparedStatement (trimmed values)
        verify(pstmt).setString(1, "12345"); // cod_dependente trimmed
        verify(pstmt).setString(2, "João da Silva"); // nome_dependente trimmed
        verify(pstmt).setString(3, "MT-001"); // mt_aluno trimmed
        verify(pstmt).setString(4, "Maria"); // nome_mae trimmed
        verify(pstmt).setString(5, "José"); // nome_pai trimmed

        // Verify date conversion: capture the java.sql.Date passed for parameter index 6
        verify(pstmt).setDate(eq(6), sqlDateCaptor.capture());
        java.sql.Date captured = sqlDateCaptor.getValue();
        assertEquals(java.sql.Date.valueOf(birth), captured, "Expected LocalDate to be converted to corresponding java.sql.Date");

        // Verify remaining bindings
        verify(pstmt).setString(7, "teste@example.com"); // email trimmed
        verify(pstmt).setString(8, ipCliente);
        verify(pstmt).setString(9, tipo);

        // Verify executeUpdate invoked
        verify(pstmt, times(1)).executeUpdate();

        // Ensure no unexpected interactions (keeps test strict)
        verifyNoMoreInteractions(legacyDao, pstmt);
    }

    /**
     * Verifies insertion path when dataNascimento is null:
     * - prepareInsert is used
     * - parameter 6 is bound via setNull(...) for DATE type
     */
    @Test
    void testInserirAluno_nullDate_bindsNull() throws Exception {
        // Arrange
        when(legacyDao.prepareInsert(anyString())).thenReturn(pstmt);
        when(pstmt.executeUpdate()).thenReturn(1);

        AlunoDaoImpl dao = new AlunoDaoImpl(legacyDao);

        Aluno aluno = new Aluno();
        aluno.setCodigoSetps("42");
        aluno.setNomeAluno(" Test User ");
        aluno.setMatricula(null);
        aluno.setNomeMae(null);
        aluno.setNomePai(null);
        // dataNascimento is left null to exercise setNull branch
        aluno.setDataNascimento((LocalDate) null);
        aluno.setEmail(" email@x.com ");

        String ipCliente = "10.0.0.1";
        String tipo = "INCLUSAO";

        // Act
        int result = dao.inserirAluno(aluno, ipCliente, tipo);

        // Assert
        assertEquals(1, result, "Expected legacy-success code 1 when executeUpdate returns >0");

        // Verify lifecycle and prepareInsert usage
        verify(legacyDao, times(1)).connect("oracle");
        verify(legacyDao, times(1)).disconnect();
        verify(legacyDao, times(1)).prepareInsert(anyString());

        // Parameter 1: cod_dependente trimmed
        verify(pstmt).setString(1, "42");
        // Parameter 2: nome_dependente trimmed
        verify(pstmt).setString(2, "Test User");
        // Parameters 3-5: nulls should be set as null via setString (code uses safeString -> null) so setString with null is allowed
        verify(pstmt).setString(3, null);
        verify(pstmt).setString(4, null);
        verify(pstmt).setString(5, null);

        // Verify that date was bound as NULL for SQL DATE type
        verify(pstmt).setNull(6, java.sql.Types.DATE);

        // Email trimmed binding
        verify(pstmt).setString(7, "email@x.com");
        verify(pstmt).setString(8, ipCliente);
        verify(pstmt).setString(9, tipo);

        verify(pstmt, times(1)).executeUpdate();

        verifyNoMoreInteractions(legacyDao, pstmt);
    }
}