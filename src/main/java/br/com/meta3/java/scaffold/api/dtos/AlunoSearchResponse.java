package br.com.meta3.java.scaffold.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the result of an Aluno search operation,
 * now including 'serie' and 'grau' fields to match legacy data.
 */
public class AlunoSearchResponse {

    @JsonProperty("codigoSetps")
    private String codigoSetps;

    @JsonProperty("matricula")
    private String matricula;

    @JsonProperty("nomeAluno")
    private String nomeAluno;

    @JsonProperty("nomeMae")
    private String nomeMae;

    @JsonProperty("dataNascimento")
    private String dataNascimento;

    @JsonProperty("serie")
    private String serie;

    @JsonProperty("grau")
    private String grau;

    public AlunoSearchResponse() {
        // Default constructor for JSON deserialization
    }

    public AlunoSearchResponse(String codigoSetps,
                               String matricula,
                               String nomeAluno,
                               String nomeMae,
                               String dataNascimento) {
        this.codigoSetps = codigoSetps;
        this.matricula = matricula;
        this.nomeAluno = nomeAluno;
        this.nomeMae = nomeMae;
        this.dataNascimento = dataNascimento;
    }

    public String getCodigoSetps() {
        return codigoSetps;
    }

    public void setCodigoSetps(String codigoSetps) {
        this.codigoSetps = codigoSetps;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getNomeMae() {
        return nomeMae;
    }

    public void setNomeMae(String nomeMae) {
        this.nomeMae = nomeMae;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    /**
     * Grade level of the student (legacy 'grau').
     * TODO: (REVIEW) Populate 'grau' in controller mapping from domain model.
     */
    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        this.grau = grau;
    }

    @Override
    public String toString() {
        return "AlunoSearchResponse{" +
                "codigoSetps='" + codigoSetps + '\'' +
                ", matricula='" + matricula + '\'' +
                ", nomeAluno='" + nomeAluno + '\'' +
                ", nomeMae='" + nomeMae + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                ", serie='" + serie + '\'' +
                ", grau='" + grau + '\'' +
                '}';
    }
}
