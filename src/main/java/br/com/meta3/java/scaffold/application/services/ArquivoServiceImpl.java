package br.com.meta3.java.scaffold.application.services;

import br.com.meta3.java.scaffold.domain.services.ArquivoService;
import br.com.meta3.java.scaffold.domain.entities.ArquivoSecSmec;
import br.com.meta3.java.scaffold.domain.entities.AlunoRgInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of ArquivoService, executing legacy SQL via JdbcTemplate.
 */
@Service
public class ArquivoServiceImpl implements ArquivoService {

    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ArquivoServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ArquivoSecSmec> listarArquivosEnviadosSecSmec(String mes, String anoBase, String codigoTitular) {
        // Determine institution and substring offset based on codigoTitular
        String instituicao;
        int deslocamento;
        if ("9999".equals(codigoTitular)) {
            instituicao = "sec";
            deslocamento = 29;
        } else {
            instituicao = "smec";
            deslocamento = 30;
        }

        // Build SQL query based on legacy logic
        String sql = ""
                + "select substr(s.ass_nm_arquivo," + deslocamento + ") as nomearquivo, "
                + "to_char(s.ass_dt_recebido, 'dd/mm/yyyy HH24:MI:SS') as datarecebimento, "
                + "s.ass_quantidade as quantidadealunos "
                + "from alu_arquivos_sec_smec s "
                + "where s.ass_nm_arquivo like ? "
                + "and   s.ass_dt_recebido between ? and trunc(last_day(?)) "
                + "order by s.ass_dt_recebido";

        String likePattern = "\\\\setps.com.br\\ftp\\" + instituicao + "\\" + anoBase + "%";
        LocalDate firstOfMonth = LocalDate.parse("01/" + mes + "/" + anoBase, DTF);
        Date sqlDate = Date.valueOf(firstOfMonth);

        return jdbcTemplate.query(
                sql,
                new Object[]{likePattern, sqlDate, sqlDate},
                new RowMapper<ArquivoSecSmec>() {
                    @Override
                    public ArquivoSecSmec mapRow(ResultSet rs, int rowNum) throws SQLException {
                        ArquivoSecSmec a = new ArquivoSecSmec();
                        a.setNomeArquivo(rs.getString("nomearquivo"));
                        a.setDataRecebimento(rs.getString("datarecebimento"));
                        a.setQuantidadeAlunos(rs.getInt("quantidadealunos"));
                        return a;
                    }
                }
        );
    }

    @Override
    public List<AlunoRgInfo> verificaRgAlunos(String codigos, String codigoTitular) {
        // Convert hyphen-separated list into comma-separated list
        // Using regex replace to handle multiple hyphens
        String listaCodigos = codigos != null ? codigos.replaceAll("-", ",") : "";
        // Remove trailing comma if present
        if (listaCodigos.endsWith(",")) {
            listaCodigos = listaCodigos.substring(0, listaCodigos.length() - 1);
        }

        // Build SQL query following legacy AlunoDAO.verificaRgAlunos logic
        String sql = ""
                + "select alu.cod_dependente, "
                + "       alu.mt_aluno, "
                + "       alu.nome_dependente, "
                + "       to_char(alu.data_nascimento,'dd/mm/yyyy') as data_nascimento "
                + "from alu_aluno_apto alu "
                + "join admcit.tpu_dependentes_dpd dpd on dpd.dpd_cod_dependente = alu.cod_dependente "
                + "where alu.ativo = 'S' "
                + "  and alu.cod_dependente in (0," + listaCodigos + ") "
                + "  and alu.cod_titular = ? "
                + "  and alu.ano_vigencia = to_char(sysdate,'yyyy')-1 "
                + "  and to_char(alu.cod_dependente) not in ( "
                + "      select lista.cod_dependente "
                + "      from alu_lista_alunos lista "
                + "      where lista.ano_vigencia = to_char(sysdate,'yyyy') "
                + "        and lista.cod_dependente is not null "
                + "  ) "
                + "  and ( to_char(sysdate,'yyyymmdd') - to_char(dpd.dpd_data_nasc,'yyyymmdd') ) >= 100000 "
                + "  and trim(dpd.dpd_num_identid) is null "
                + "order by alu.nome_dependente";

        // TODO: (REVIEW) Inlining list into SQL risks SQL injection; consider NamedParameterJdbcTemplate with a proper collection parameter.
        return jdbcTemplate.query(
                sql,
                new Object[]{codigoTitular},
                new RowMapper<AlunoRgInfo>() {
                    @Override
                    public AlunoRgInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        AlunoRgInfo info = new AlunoRgInfo();
                        info.setCodDependente(rs.getLong("cod_dependente"));
                        info.setMatricula(rs.getString("mt_aluno"));
                        info.setNomeDependente(rs.getString("nome_dependente"));
                        info.setDataNascimento(rs.getString("data_nascimento"));
                        return info;
                    }
                }
        );
    }

    // NOTE:
    // - The inner DTO previously declared here (ArquivoServiceImpl.AlunoRgInfo) was removed intentionally.
    // - We now use the domain-level br.com.meta3.java.scaffold.domain.entities.AlunoRgInfo so controllers and services
    //   can share the same type and avoid duplication.
    // TODO: (REVIEW) Consider migrating date handling to LocalDate in AlunoRgInfo if callers are updated to use typed dates.
}