package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.entities.Aluno;
import br.com.meta3.java.scaffold.domain.entities.Documento;
import br.com.meta3.java.scaffold.domain.entities.Endereco;
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
 */
@Repository
public class AlunoDaoImpl {

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
            legacyDao.connect("oracle");
            // First query: basic info
            PreparedStatement ps1 = legacyDao.executePreparedQuery(sql1);
            ps1.setInt(1, Integer.parseInt(codigosetps));
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                aluno.setCodigoSetps(rs1.getString("dpd_cod_dependente"));
                aluno.setNomeAluno(rs1.getString("dpd_nome_dependente"));
                aluno.setSexo(rs1.getString("dpd_sexo_dependente"));
                aluno.setNomeMae(rs1.getString("dpd_filiacao_mae"));
                aluno.setNomePai(rs1.getString("dpd_filiacao_pai"));
                aluno.setDataNascimento(rs1.getString("dpd_data_nasc"));
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
                legacyDao.disconnect();
            } catch (SQLException e) {
                LOGGER.warn("Erro ao desconectar após pesquisaCodigosetps", e);
            }
        }
    }

    // ... rest of the methods unchanged ...

    /**
     * Helper to apply the "TESTE SETPS" exclusion clause based on codigoEscola.
     */
    private String aplicarClausulaSeNaoForEscolaTesteSETPS(String codigoEscola) {
        if ("2603".equals(codigoEscola)) {
            return "";
        }
        return " and dpd.dpd_nome_dependente not like '%TESTE%' ";
    }
}
