package br.com.meta3.java.scaffold.api.dtos;

/**
 * DTO representing the result of an Aluno insertion operation.
 * Contains flags to indicate success and whether to reset the form.
 */
public class AlunoResponse {

    /**
     * Indicates if the Aluno insertion was successful.
     */
    private boolean sucesso;

    /**
     * Indicates if the client should reset the form/state.
     */
    private boolean resetar;

    public AlunoResponse() {
    }

    public AlunoResponse(boolean sucesso, boolean resetar) {
        this.sucesso = sucesso;
        this.resetar = resetar;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public boolean isResetar() {
        return resetar;
    }

    public void setResetar(boolean resetar) {
        this.resetar = resetar;
    }

    // TODO: (REVIEW) If frontend expects "resetar" as numeric/string flag, adapt type accordingly.
}
