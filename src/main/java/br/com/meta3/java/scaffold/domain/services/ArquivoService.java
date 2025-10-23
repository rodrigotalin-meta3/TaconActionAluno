package br.com.meta3.java.scaffold.domain.services;

import java.util.List;
import br.com.meta3.java.scaffold.domain.entities.ArquivoSecSmec;
import br.com.meta3.java.scaffold.domain.entities.AlunoRgInfo;

/**
 * Abstraction for operations related to Arquivo (file) domain use-cases.
 *
 * Purpose:
 * - Provide a clean interface for listing files received from SEC/SMEC and
 *   for checking dependents missing RG information.
 * - Decouples controllers and application services from the implementation
 *   (which may use JdbcTemplate, legacy JDBC, or other data sources).
 *
 * Design notes / decisions:
 * - listarArquivosEnviadosSecSmec:
 *     Accepts month and year as strings (legacy code used String parameters).
 *     Returns a list of ArquivoSecSmec domain DTOs which contain already-formatted
 *     date strings to preserve legacy behaviour. Consider migrating to typed dates
 *     (e.g., java.time) in future refactors.
 *
 * - verificaRgAlunos:
 *     Accepts a hyphen-separated or otherwise formatted string of codes (legacy behaviour).
 *     Implementations SHOULD consider accepting a collection of identifiers instead to avoid
 *     SQL concatenation and injection risks. For now the signature mirrors the legacy usage
 *     to simplify migration.
 *
 * TODOs:
 * - (REVIEW) Consider overloading verificaRgAlunos to accept Collection<Long> or List<String>
 *   to allow safe parameter binding (NamedParameterJdbcTemplate) and avoid inlining lists into SQL.
 * - (REVIEW) Decide whether date/time fields in ArquivoSecSmec and AlunoRgInfo should be converted
 *   to LocalDate/LocalDateTime to take advantage of type-safety across the application.
 */
public interface ArquivoService {

    /**
     * Lists files sent by SEC/SMEC for the specified month and year.
     *
     * Legacy behaviour:
     * - codigoTitular == "9999" indicates SEC, otherwise SMEC.
     * - The file name returned is a substring of the original path (legacy used substr with an offset).
     * - The reception date is expected to be formatted as 'dd/MM/yyyy HH24:MI:SS'.
     *
     * @param mes           month in "MM" format (e.g., "01".."12")
     * @param anoBase       year in "yyyy" format (e.g., "2024")
     * @param codigoTitular "9999" for SEC, other values for SMEC
     * @return list of ArquivoSecSmec DTOs (may be empty but never null)
     */
    List<ArquivoSecSmec> listarArquivosEnviadosSecSmec(String mes, String anoBase, String codigoTitular);

    /**
     * Returns a list of dependents (alunos) older than 10 years without an RG for the previous year
     * constrained by the provided codes and titular code.
     *
     * Legacy behaviour:
     * - 'codigos' was passed as a hyphen-separated string (e.g., "1-2-3") and inlined into SQL.
     * - The returned data contains formatted birth date strings 'dd/MM/yyyy'.
     *
     * Implementation guidance:
     * - Implementations should sanitize/validate 'codigos' and avoid direct SQL inlining when possible.
     * - Prefer accepting a collection parameter in future interface versions to support safe binding.
     *
     * @param codigos        hyphen-separated list of dependent codes (legacy format)
     * @param codigoTitular  titular (school) code used for filtering
     * @return list of AlunoRgInfo DTOs (may be empty but never null)
     */
    List<AlunoRgInfo> verificaRgAlunos(String codigos, String codigoTitular);
}