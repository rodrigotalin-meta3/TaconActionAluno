package br.com.meta3.java.scaffold.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO representing the payload for creating a new Aluno.
 */
public class AlunoRequest {

    @NotBlank(message = "Nome do aluno é obrigatório")
    @Size(max = 100, message = "Nome do aluno deve conter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ser válido")
    @Size(max = 100, message = "E-mail deve conter no máximo 100 caracteres")
    private String email;

    public AlunoRequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
