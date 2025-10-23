package br.com.meta3.java.scaffold.api.dtos;

/**
 * DTO representing student RG check result.
 * Contains dependent code, matricula, dependent name, and birth date.
 */
public class AlunoRgResponse {

    /**
     * Identifier of the dependent student.
     */
    private long codDependente;

    /**
     * Matricula (registration) of the student.
     */
    private String matricula;

    /**
     * Full name of the dependent student.
     */
    private String nomeDependente;

    /**
     * Birth date formatted as 'dd/MM/yyyy'.
     */
    private String dataNascimento;

    public AlunoRgResponse() {
    }

    public AlunoRgResponse(long codDependente, String matricula, String nomeDependente, String dataNascimento) {
        this.codDependente = codDependente;
        this.matricula = matricula;
        this.nomeDependente = nomeDependente;
        this.dataNascimento = dataNascimento;
    }

    public long getCodDependente() {
        return codDependente;
    }

    public void setCodDependente(long codDependente) {
        this.codDependente = codDependente;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNomeDependente() {
        return nomeDependente;
    }

    public void setNomeDependente(String nomeDependente) {
        this.nomeDependente = nomeDependente;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    @Override
    public String toString() {
        return "AlunoRgResponse{" +
                "codDependente=" + codDependente +
                ", matricula='" + matricula + '\'' +
                ", nomeDependente='" + nomeDependente + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                '}';
    }
}
