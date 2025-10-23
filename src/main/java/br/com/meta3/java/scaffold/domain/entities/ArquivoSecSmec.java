package br.com.meta3.java.scaffold.domain.entities;

/**
 * Domain entity representing a file record from SEC/SMEC.
 * Migrated from legacy Arquivo_Sec_Smec POJO.
 */
public class ArquivoSecSmec {

    /**
     * The name of the file (substring of the original path).
     */
    private String nomeArquivo;

    /**
     * Reception date and time formatted as 'dd/MM/yyyy HH24:MI:SS'.
     */
    private String dataRecebimento;

    /**
     * Quantity of students associated with this file.
     */
    private int quantidadeAlunos;

    public ArquivoSecSmec() {
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    /**
     * Legacy setter preserved for backward compatibility.
     * TODO: (REVIEW) Remove once all legacy references are updated to use setNomeArquivo.
     *
     * @param nomearquivo original property name in legacy code
     */
    public void setNomearquivo(String nomearquivo) {
        this.nomeArquivo = nomearquivo;
    }

    public String getDataRecebimento() {
        return dataRecebimento;
    }

    /**
     * JavaBean-compliant setter for dataRecebimento.
     *
     * @param dataRecebimento formatted reception date-time string
     */
    public void setDataRecebimento(String dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    /**
     * Legacy setter preserved for backward compatibility.
     * TODO: (REVIEW) Remove once all legacy references are updated to use setDataRecebimento.
     *
     * @param datarecebimento original property name in legacy code
     */
    public void setDatarecebimento(String datarecebimento) {
        this.dataRecebimento = datarecebimento;
    }

    public int getQuantidadeAlunos() {
        return quantidadeAlunos;
    }

    public void setQuantidadeAlunos(int quantidadeAlunos) {
        this.quantidadeAlunos = quantidadeAlunos;
    }
}
