package br.com.meta3.java.scaffold.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO representing the payload for advanced Aluno search operations.
 * Allows filtering by:
 *  - nomeAluno       (required, partial or full match)
 *  - dataNascimento  (optional, format dd/MM/yyyy)
 *  - cpf             (optional, exactly 11 digits, no punctuation)
 *  - nomeMae         (optional, partial or full match)
 */
public class AlunoSearchRequest {

    @NotBlank(message = "Nome do aluno é obrigatório")
    @Size(max = 100, message = "Nome do aluno deve conter no máximo 100 caracteres")
    private String nomeAluno;

    @Pattern(
        regexp = "\\d{2}/\\d{2}/\\d{4}",
        message = "Data de nascimento deve estar no formato dd/MM/yyyy"
    )
    private String dataNascimento;

    @Pattern(
        regexp = "\\d{11}",
        message = "CPF deve conter exatamente 11 dígitos sem pontuação"
    )
    private String cpf;

    @Size(max = 100, message = "Nome da mãe deve conter no máximo 100 caracteres")
    private String nomeMae;

    public AlunoSearchRequest() {
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNomeMae() {
        return nomeMae;
    }

    public void setNomeMae(String nomeMae) {
        this.nomeMae = nomeMae;
    }

    @Override
    public String toString() {
        return "AlunoSearchRequest{" +
                "nomeAluno='" + nomeAluno + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                ", cpf='" + cpf + '\'' +
                ", nomeMae='" + nomeMae + '\'' +
                '}';
    }
}
