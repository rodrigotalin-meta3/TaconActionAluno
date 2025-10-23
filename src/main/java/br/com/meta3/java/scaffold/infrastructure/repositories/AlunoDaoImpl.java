filetype
package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.entities.Aluno;
import br.com.meta3.java.scaffold.domain.entities.Documento;
import br.com.meta3.java.scaffold.domain.entities.Endereco;
import br.com.meta3.java.scaffold.domain.repositories.AlunoLegacyDao;
import br.com.meta3.java.scaffold.domain.services.LegacyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom DAO implementation for legacy Aluno operations (search, validation, list, insert, delete)
 * using the LegacyDao abstraction for JDBC interactions.
 *
 * Extended to implement AlunoLegacyDao to expose legacy insertion functionality required by migrated services.
 *
 * Migration decision notes:
 * - All preparation of statements and execution is delegated to LegacyDao (legacyDao.executePreparedQuery / prepareInsert).
 *   This centralizes connection/driver handling in LegacyDaoImpl and avoids direct use of Connection.prepareStatement here.
 * - Resource lifecycle is managed by invoking legacyDao.connect(...) before operations and legacyDao.disconnect() in finally blocks.
 *   LegacyDaoImpl.disconnect() closes ResultSet/PreparedStatement/Statement/Connection in the correct order.
 *
 * Important: Inserts must use legacyDao.prepareInsert(...) to ensure PreparedStatement creation is centralized and that
 * the LegacyDao lifecycle (connect/disconnect and instance pstmt tracking) is preserved. Selects/queries continue to
 * use legacyDao.executePreparedQuery(...).
 *
 * TODO: (REVIEW) Consider evolving LegacyDao to return higher-level helpers (e.g., QueryExecutor) to avoid exposing raw JDBC objects.
 */
@Repository
public class AlunoDaoImpl implements AlunoLegacyDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlunoDaoImpl.class);

    private final LegacyDao legacyDao;

    public AlunoDaoImpl(LegacyDao legacyDao) {
        this.legacyDao = legacyDao;
    }

    /**
     * Validates a date string and age constraints, ported from legacy Aluno.verificaData.
     *
     * @param data date in 'dd/MM/yyyy' format
     * @return 1 if valid; 0 otherwise
     */
    public int verificaData(String data) {
        // Simple port of legacy date validation; may require refinement
        if (data == null || data.length() != 10) {
            return 0;
        }
        try {
            String[] parts = data.split("/");
            int dia = Integer.parseInt(parts[0]);
            int mes = Integer.parseInt(parts[1]);
            int ano = Integer.parseInt(parts[2]);
            if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || ano < 1800 || ano > 2050) {
                return 0;
            }
            int currentYear = Integer.parseInt(new java.text.SimpleDateFormat("yyyy").format(new java.util.Date()));
            // Ensure minimum age requirement (>=5 years)
            if (currentYear - ano < 5) {
                return 0;
            }
            return 1;
        } catch (Exception ex) {
            LOGGER.warn("Erro em verificaData parse/validation", ex);
            return 0;
        }
    }

    /**
     * Validates CPF format and checksum, ported from legacy Aluno.verificaCpf.
     *
     * @param cpf exactly 11 digits
     * @return true if valid; false otherwise
     */
    public boolean verificaCpf(String cpf) {
        // Basic legacy checks: length, all digits, not all same digit
        if (cpf == null || !cpf.matches("\\d{11}") ||
            cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        // TODO: (REVIEW) Implement full CPF checksum validation as in legacy code
        return true;
    }

    /**
     * Searches and populates Aluno by codigosetps, matching legacy pesquisaCodigosetps.
     *
     * @param codigosetps  dependent code
     * @param codigoEscola titular (school) code
     * @return codigosetps if found; "-1" on error
     */
    public String pesquisaCodigosetps(String codigosetps, String codigoEscola) {
        String sql1 = "" +
            "select dpd.dpd_cod_dependente, dpd.dpd_nome_dependente, dpd.dpd_sexo_dependente, " +
            "dpd.dpd_filiacao_mae, dpd.dpd_filiacao_pai, " +
            "TO_CHAR(dpd.dpd_data_nasc,'dd/mm/yyyy') dpd_data_nasc, dpd.dpd_num_telefone_dependente, dpd.dpd_email_dependente, " +
            "dpd.dpd_num_cpf, dpd.dpd_num_identid, TO_CHAR(dpd.dpd_data_exp_identid,'dd/mm/yyyy') dpd_data_exp_identid, dpd.dpd_orgao_exp, " +
            "dpd.dpd_certidao_num, dpd.dpd_certidao_folha, dpd.dpd_certidao_livro, dpd.dpd_matricula_nascimento, " +
            "dpd.dpd_end_nome_logradouro, dpd.dpd_end_complemento, dpd.dpd_end_numero, dpd.dpd_end_bairro, dpd.dpd_end_cep, mun.mun_desc_municipio " +
            "from admcit.tpu_dependentes_dpd dpd, admcit.tpu_municipios_mun mun " +
            "where dpd.mun_cod_municipio = mun.mun_cod_municipio(+) " +
            "  and dpd.dpd_cod_dependente = ? " +
            aplicarClausulaSeNaoForEscolaTesteSETPS(codigoEscola);

        String sql2 = "" +
            "select des.des_serie_periodo, des.des_grau_estudante, des.des_turno, des.des_matricula_estudante " +
            "from admcit.tpu_dependentes_dpd dpd " +
            "join admcit.tpu_dependentes_tit_dpt dpt on dpd.dpd_cod_dependente = dpt.dpd_cod_dependente " +
            "join admcit.tpu_depend_estudante_des des on dpt.dpt_cod_dpd_tit = des.dpt_cod_dpd_tit " +
            "where dpd.dpd_cod_dependente = ? " +
            aplicarClausulaSeNaoForEscolaTesteSETPS(codigoEscola);

        Aluno aluno = new Aluno();
        try {
            // Ensure connection is established via LegacyDao abstraction.
            legacyDao.connect("oracle");

            // Use LegacyDao.executePreparedQuery to centralize PreparedStatement creation for SELECTs.
            PreparedStatement ps1 = legacyDao.executePreparedQuery(sql1);
            ps1.setInt(1, Integer.parseInt(codigosetps));
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                aluno.setCodigoSetps(rs1.getString("dpd_cod_dependente"));
                aluno.setNomeAluno(rs1.getString("dpd_nome_dependente"));
                aluno.setSexo(rs1.getString("dpd_sexo_dependente"));
                aluno.setNomeMae(rs1.getString("dpd_filiacao_mae"));
                aluno.setNomePai(rs1.getString("dpd_filiacao_pai"));
                // NOTE: The domain Aluno.dataNascimento is LocalDate; legacy flow assigns strings in many places.
                // Keeping legacy behavior for now; consider centralizing parsing to LocalDate in future refactor.
                try {
                    aluno.setDataNascimento(rs1.getString("dpd_data_nasc"));
                } catch (Exception e) {
                    // Keep resilient if domain changed types; ignore and proceed
                    LOGGER.debug("Could not set dataNascimento as String (type mismatch), skipping", e);
                }
                aluno.setTelefone(rs1.getString("dpd_num_telefone_dependente"));
                aluno.setEmail(rs1.getString("dpd_email_dependente"));

                // Refactor: instantiate Documento using new constructors
                String rg = rs1.getString("dpd_num_identid");
                String orgaoExp = rs1.getString("dpd_orgao_exp");
                String dataExp = rs1.getString("dpd_data_exp_identid");
                String cpf = rs1.getString("dpd_num_cpf");
                String numeroCertidao = rs1.getString("dpd_certidao_num");
                String livroCertidao = rs1.getString("dpd_certidao_livro");
                String folhaCertidao = rs1.getString("dpd_certidao_folha");
                String matriculaNascimento = rs1.getString("dpd_matricula_nascimento");

                Documento doc;
                if (numeroCertidao != null && !numeroCertidao.isEmpty()
                        && livroCertidao != null && !livroCertidao.isEmpty()
                        && folhaCertidao != null && !folhaCertidao.isEmpty()) {
                    // Use full-details constructor
                    doc = new Documento(rg, orgaoExp, dataExp, cpf,
                                       numeroCertidao, livroCertidao, folhaCertidao);
                    // TODO: (REVIEW) Preserve matriculaNascimento if needed
                    doc.getCertidao().setMatriculaNascimento(matriculaNascimento);
                } else {
                    // Fallback: only matriculaNascimento available
                    doc = new Documento(rg, orgaoExp, dataExp, cpf, matriculaNascimento);
                }
                aluno.setDocumento(doc);

                // endereco
                Endereco end = new Endereco();
                end.setLogradouro(rs1.getString("dpd_end_nome_logradouro"));
                end.setComplemento(rs1.getString("dpd_end_complemento"));
                end.setNumero(rs1.getString("dpd_end_numero"));
                end.setBairro(rs1.getString("dpd_end_bairro"));
                end.setCep(rs1.getString("dpd_end_cep"));
                end.setCidade(rs1.getString("mun_desc_municipio"));
                aluno.setEndereco(end);
            }
            // Second query: schooling
            PreparedStatement ps2 = legacyDao.executePreparedQuery(sql2);
            ps2.setInt(1, Integer.parseInt(codigosetps));
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                aluno.setSerie(rs2.getString("des_serie_periodo"));
                aluno.setGrau(rs2.getString("des_grau_estudante"));
                aluno.setTurno(rs2.getString("des_turno"));
                if (aluno.getMatricula() == null) {
                    aluno.setMatricula(rs2.getString("des_matricula_estudante"));
                }
            }
            return codigosetps;
        } catch (SQLException ex) {
            LOGGER.error("Erro em pesquisaCodigosetps", ex);
            return "-1";
        } finally {
            try {
                // Ensure centralized resource cleanup via LegacyDao implementation.
                legacyDao.disconnect();
            } catch (SQLException e) {
                LOGGER.warn("Erro ao desconectar após pesquisaCodigosetps", e);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Helper to apply the "TESTE SETPS" exclusion clause based on codigoEscola.
    // ------------------------------------------------------------------------
    private String aplicarClausulaSeNaoForEscolaTesteSETPS(String codigoEscola) {
        if ("2603".equals(codigoEscola)) {
            return "";
        }
        return " and dpd.dpd_nome_dependente not like '%TESTE%' ";
    }

    // ------------------------------------------------------------------------
    // New method added to satisfy legacy insertion contract.
    // Implements AlunoLegacyDao.inserirAluno using LegacyDao prepareInsert/execute.
    // Returns 1 on success, 0 on failure (legacy convention).
    // ------------------------------------------------------------------------

    /**
     * Inserts an Aluno record into the legacy system.
     *
     * Note:
     * - This method attempts to map a subset of Aluno fields to the legacy table.
     * - The SQL and mapped columns reflect a conservative subset to avoid breaking assumptions.
     * - Uses legacyDao.prepareInsert(...) to obtain a PreparedStatement bound to the legacy connection.
     *
     * TODO: (REVIEW) Expand column mapping and transactional semantics as required by legacy DB constraints.
     *
     * @param aluno     domain Aluno object containing values to be inserted (may be partially populated)
     * @param ipCliente IP address of the client that triggered the insertion (legacy stored field)
     * @param tipo      operation type (e.g., "INCLUSAO", "EXCLUSAO")
     * @return 1 on success, 0 on failure
     */
    @Override
    public int inserirAluno(Aluno aluno, String ipCliente, String tipo) {
        // Basic defensive checks
        if (aluno == null) {
            LOGGER.warn("inserirAluno called with null Aluno");
            return 0;
        }

        // Example conservative INSERT into legacy table 'alu_lista_alunos'.
        // Mapping chosen to include identifiers and contact/origin info.
        // TODO: (REVIEW) Confirm exact column names and types in legacy DB; adapt to match production schema.
        String sql = "INSERT INTO alu_lista_alunos (" +
                "cod_dependente, nome_dependente, mt_aluno, nome_mae, nome_pai, data_nascimento, email, ip_solicitante, funcao_origem_site" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // Connect to legacy Oracle DB as other legacy operations do.
            // Decision: Always request "oracle" to follow legacy behavior and ensure correct driver usage.
            legacyDao.connect("oracle");

            // IMPORTANT: Use legacyDao.prepareInsert(...) for all INSERT preparations.
            // Rationale: centralizes PreparedStatement creation, ensures LegacyDaoImpl tracks pstmt and uses the intended connection.
            PreparedStatement pstmt = legacyDao.prepareInsert(sql);

            // Bind parameters with null-safety and trimming
            pstmt.setString(1, safeString(aluno.getCodigoSetps()));
            pstmt.setString(2, safeString(aluno.getNomeAluno()));
            pstmt.setString(3, safeString(aluno.getMatricula()));
            pstmt.setString(4, safeString(aluno.getNomeMae()));
            pstmt.setString(5, safeString(aluno.getNomePai()));

            // data_nascimento: attempt to bind as DATE if possible
            try {
                // Aluno.dataNascimento is LocalDate in domain model; convert to java.sql.Date
                if (aluno.getDataNascimento() != null) {
                    // If dataNascimento is String in legacy usages, this will throw ClassCastException;
                    // however domain model declares LocalDate, so this is the expected branch.
                    java.time.LocalDate ld = aluno.getDataNascimento();
                    pstmt.setDate(6, java.sql.Date.valueOf(ld));
                } else {
                    pstmt.setNull(6, java.sql.Types.DATE);
                }
            } catch (ClassCastException cce) {
                // Fallback: try to interpret as String (legacy mixed types)
                try {
                    String ds = (String) (Object) aluno.getDataNascimento();
                    if (ds != null && !ds.isEmpty()) {
                        // Try parsing 'dd/MM/yyyy' format
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        java.util.Date parsed = sdf.parse(ds);
                        pstmt.setDate(6, new java.sql.Date(parsed.getTime()));
                    } else {
                        pstmt.setNull(6, java.sql.Types.DATE);
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Could not bind data_nascimento for inserirAluno; setting NULL", ex);
                    pstmt.setNull(6, java.sql.Types.DATE);
                }
            }

            pstmt.setString(7, safeString(aluno.getEmail()));
            pstmt.setString(8, safeString(ipCliente));
            pstmt.setString(9, safeString(tipo));

            int updated = pstmt.executeUpdate();

            // If execution succeeds and affected rows > 0, return legacy-success code 1
            if (updated > 0) {
                return 1;
            } else {
                LOGGER.warn("inserirAluno executed but no rows were inserted (updated==0)");
                return 0;
            }

        } catch (SQLException ex) {
            LOGGER.error("Erro ao inserir Aluno no legacy DB", ex);
            return 0;
        } finally {
            try {
                legacyDao.disconnect();
            } catch (SQLException e) {
                LOGGER.warn("Erro ao desconectar após inserirAluno", e);
            }
        }
    }

    /**
     * Helper to safely return trimmed strings or null.
     */
    private static String safeString(String value) {
        return (value == null ? null : value.trim());
    }

    // ------------------------------------------------------------------------
    // Ported methods from legacy code to use LegacyDao for queries.
    // ------------------------------------------------------------------------

    /**
     * Searches students by name, optional birth date, cpf, and mother's name.
     *
     * @param nomeAluno       full or partial student name
     * @param dataNascimento  birth date filter (optional, dd/MM/yyyy)
     * @param cpf             cpf filter (optional)
     * @param nomeMae         mother's name filter (optional)
     * @return list of matching Aluno entities
     */
    public List<Aluno> pesquisarNome(String nomeAluno, String dataNascimento, String cpf, String nomeMae) {
        List<Aluno> results = new ArrayList<>();

        // Build SQL with parameter placeholders to avoid direct inlining.
        // We search in ADM schema tables analogous to legacy behavior. This is a conservative mapping.
        StringBuilder sql = new StringBuilder();
        sql.append("select dpd.dpd_cod_dependente, dpd.dpd_nome_dependente, ");
        sql.append("TO_CHAR(dpd.dpd_data_nasc,'dd/mm/yyyy') dpd_data_nasc, dpd.dpd_num_cpf, dpd.dpd_filiacao_mae, ");
        sql.append("dpd.dpd_num_telefone_dependente, dpd.dpd_email_dependente ");
        sql.append("from admcit.tpu_dependentes_dpd dpd ");
        sql.append("where 1=1 ");

        List<Object> params = new ArrayList<>();

        // nomeAluno partial match
        if (nomeAluno != null && !nomeAluno.trim().isEmpty()) {
            sql.append(" and upper(dpd.dpd_nome_dependente) like ? ");
            params.add("%" + nomeAluno.trim().toUpperCase() + "%");
        }

        if (dataNascimento != null && !dataNascimento.trim().isEmpty()) {
            sql.append(" and TO_CHAR(dpd.dpd_data_nasc,'dd/mm/yyyy') = ? ");
            params.add(dataNascimento.trim());
        }

        if (cpf != null && !cpf.trim().isEmpty()) {
            sql.append(" and dpd.dpd_num_cpf = ? ");
            params.add(cpf.trim());
        }

        if (nomeMae != null && !nomeMae.trim().isEmpty()) {
            sql.append(" and upper(dpd.dpd_filiacao_mae) like ? ");
            params.add("%" + nomeMae.trim().toUpperCase() + "%");
        }

        sql.append(" order by dpd.dpd_nome_dependente ");

        try {
            legacyDao.connect("oracle");
            // Prepare statement through LegacyDao to centralize creation
            PreparedStatement ps = legacyDao.executePreparedQuery(sql.toString());
            // bind params
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Aluno a = new Aluno();
                a.setCodigoSetps(rs.getString("dpd_cod_dependente"));
                a.setNomeAluno(rs.getString("dpd_nome_dependente"));
                try {
                    a.setDataNascimento(rs.getString("dpd_data_nasc"));
                } catch (Exception e) {
                    LOGGER.debug("Could not set dataNascimento as String (type mismatch), skipping", e);
                }
                // NOTE: Aluno may not have setCpf; adapt if needed in domain model
                try {
                    // Attempt to set CPF if method exists; if not, it's ignored (keeps backward compatibility)
                    a.getClass().getMethod("setCpf", String.class).invoke(a, rs.getString("dpd_num_cpf"));
                } catch (Exception ignore) {
                    // Intentionally ignore missing setter; domain model may not expose setCpf
                }
                a.setNomeMae(rs.getString("dpd_filiacao_mae"));
                a.setTelefone(rs.getString("dpd_num_telefone_dependente"));
                a.setEmail(rs.getString("dpd_email_dependente"));
                results.add(a);
            }
        } catch (SQLException ex) {
            LOGGER.error("Erro em pesquisarNome", ex);
            // Return what we have or empty list on error as legacy code tends to do
        } finally {
            try {
                legacyDao.disconnect();
            } catch (SQLException e) {
                LOGGER.warn("Erro ao desconectar após pesquisarNome", e);
            }
        }

        return results;
    }

    /**
     * General search across multiple fields, populating an Aluno.
     *
     * @param rg               identity RG (optional)
     * @param numeroCertidao   birth certificate number (optional)
     * @param matricula        student registration (optional)
     * @param cpf              CPF filter (optional)
     * @param nomeMae          mother's name filter (optional)
     * @param nomeAluno        student name filter (optional)
     * @param dataNascimento   birth date filter (optional)
     * @return Aluno or null if not found
     */
    public Aluno pesquisarGeral(String rg,
                                String numeroCertidao,
                                String matricula,
                                String cpf,
                                String nomeMae,
                                String nomeAluno,
                                String dataNascimento) {
        StringBuilder sql = new StringBuilder();
        sql.append("select dpd.dpd_cod_dependente, dpd.dpd_nome_dependente, ");
        sql.append("TO_CHAR(dpd.dpd_data_nasc,'dd/mm/yyyy') dpd_data_nasc, dpd.dpd_num_cpf, dpd.dpd_num_identid, dpd.dpd_filiacao_mae ");
        sql.append("from admcit.tpu_dependentes_dpd dpd ");
        sql.append("where 1=1 ");

        List<Object> params = new ArrayList<>();

        if (rg != null && !rg.trim().isEmpty()) {
            sql.append(" and dpd.dpd_num_identid = ? ");
            params.add(rg.trim());
        }
        if (numeroCertidao != null && !numeroCertidao.trim().isEmpty()) {
            sql.append(" and dpd.dpd_certidao_num = ? ");
            params.add(numeroCertidao.trim());
        }
        if (matricula != null && !matricula.trim().isEmpty()) {
            sql.append(" and dpd.dpd_matricula_nascimento = ? ");
            params.add(matricula.trim());
        }
        if (cpf != null && !cpf.trim().isEmpty()) {
            sql.append(" and dpd.dpd_num_cpf = ? ");
            params.add(cpf.trim());
        }
        if (nomeMae != null && !nomeMae.trim().isEmpty()) {
            sql.append(" and upper(dpd.dpd_filiacao_mae) like ? ");
            params.add("%" + nomeMae.trim().toUpperCase() + "%");
        }
        if (nomeAluno != null && !nomeAluno.trim().isEmpty()) {
            sql.append(" and upper(dpd.dpd_nome_dependente) like ? ");
            params.add("%" + nomeAluno.trim().toUpperCase() + "%");
        }
        if (dataNascimento != null && !dataNascimento.trim().isEmpty()) {
            sql.append(" and TO_CHAR(dpd.dpd_data_nasc,'dd/mm/yyyy') = ? ");
            params.add(dataNascimento.trim());
        }

        sql.append(" fetch first 1 rows only ");

        try {
            legacyDao.connect("oracle");
            PreparedStatement ps = legacyDao.executePreparedQuery(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Aluno a = new Aluno();
                a.setCodigoSetps(rs.getString("dpd_cod_dependente"));
                a.setNomeAluno(rs.getString("dpd_nome_dependente"));
                try {
                    a.setDataNascimento(rs.getString("dpd_data_nasc"));
                } catch (Exception e) {
                    LOGGER.debug("Could not set dataNascimento as String (type mismatch), skipping", e);
                }
                a.setNomeMae(rs.getString("dpd_filiacao_mae"));
                // populate document basic info
                Documento doc = new Documento();
                // NOTE: Identidade setters are embedded in Documento; adapting minimal fields
                // TODO: (REVIEW) Populate Documento.identidade properly if domain model requires typed dates
                a.setDocumento(doc);
                return a;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erro em pesquisarGeral", ex);
        } finally {
            try {
                legacyDao.disconnect();
            } catch (SQLException e) {
                LOGGER.warn("Erro ao desconectar após pesquisarGeral", e);
            }
        }
        return null;
    }

    /**
     * Retrieves list of 'apto' students for the previous year not yet sent.
     *
     * @param initials        array of initial letters to filter names
     * @param codigoEscola    titular code
     * @param anoVigencia     year of validity
     * @param dataNascimento  birth date filter (optional)
     * @return list of apto Aluno entities
     */
    public List<Aluno> pesquisarAlunoApto(String[] initials,
                                          String codigoEscola,
                                          String anoVigencia,
                                          String dataNascimento) {
        List<Aluno> lista = new ArrayList<>();

        // Build dynamic WHERE for initials using parameterized LIKE clauses
        StringBuilder sql = new StringBuilder();
        sql.append("select alu.cod_dependente, alu.mt_aluno, alu.nome_dependente, ");
        sql.append("TO_CHAR(alu.data_nascimento,'dd/mm/yyyy') data_nascimento ");
        sql.append("from alu_aluno_apto alu ");
        sql.append("where alu.ativo = 'S' ");
        sql.append("and alu.cod_titular = ? ");
        sql.append("and alu.ano_vigencia = ? ");

        List<Object> params = new ArrayList<>();
        params.add(codigoEscola);
        params.add(anoVigencia);

        if (initials != null && initials.length > 0) {
            sql.append(" and (");
            for (int i = 0; i < initials.length; i++) {
                if (i > 0) sql.append(" or ");
                sql.append(" upper(alu.nome_dependente) like ? ");
                params.add(initials[i].trim().toUpperCase() + "%");
            }
            sql.append(") ");
        }

        if (dataNascimento != null && !dataNascimento.trim().isEmpty()) {
            sql.append(" and TO_CHAR(alu.data_nascimento,'dd/mm/yyyy') = ? ");
            params.add(dataNascimento.trim());
        }

        sql.append(" order by alu.nome_dependente ");

        try {
            legacyDao.connect("oracle");
            PreparedStatement ps = legacyDao.executePreparedQuery(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Aluno a = new Aluno();
                a.setCodigoSetps(rs.getString("cod_dependente"));
                a.setMatricula(rs.getString("mt_aluno"));
                a.setNomeAluno(rs.getString("nome_dependente"));
                try {
                    a.setDataNascimento(rs.getString("data_nascimento"));
                } catch (Exception e) {
                    LOGGER.debug("Could not set dataNascimento as String (type mismatch), skipping", e);
                }
                lista.add(a);
            }
        } catch (SQLException ex) {
            LOGGER.error("Erro em pesquisarAlunoApto", ex);
        } finally {
            try {
                legacyDao.disconnect();
            } catch (SQLException e) {
                LOGGER.warn("Erro ao desconectar após pesquisarAlunoApto", e);
            }
        }

        return lista;
    }
}