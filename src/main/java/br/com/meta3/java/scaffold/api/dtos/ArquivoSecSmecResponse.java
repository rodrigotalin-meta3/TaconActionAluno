package br.com.meta3.java.scaffold.api.dtos;

/**
 * DTO representing a file record from SEC/SMEC.
 * Contains file name, reception timestamp, and number of students.
 */
public class ArquivoSecSmecResponse {

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

    public ArquivoSecSmecResponse() {
    }

    public ArquivoSecSmecResponse(String nomeArquivo, String dataRecebimento, int quantidadeAlunos) {
        this.nomeArquivo = nomeArquivo;
        this.dataRecebimento = dataRecebimento;
        this.quantidadeAlunos = quantidadeAlunos;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getDataRecebimento() {
        return dataRecebimento;
    }

    public void setDataRecebimento(String dataRecebimento) {
        this.dataRecebimento = dataRecebimento;
    }

    public int getQuantidadeAlunos() {
        return quantidadeAlunos;
    }

    public void setQuantidadeAlunos(int quantidadeAlunos) {
        this.quantidadeAlunos = quantidadeAlunos;
    }

    @Override
    public String toString() {
        return "ArquivoSecSmecResponse{" +
                "nomeArquivo='" + nomeArquivo + '\'' +
                ", dataRecebimento='" + dataRecebimento + '\'' +
                ", quantidadeAlunos=" + quantidadeAlunos +
                '}';
    }
}
