package br.com.meta3.java.scaffold.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Embeddable class representing birth certificate details for a student.
 * Maps legacy fields:
 *   - num_certidao_nasc (certificate number)
 *   - liv_certidao_nasc (book number)
 *   - fol_certidao_nasc (sheet number)
 *   - matricula_nascimento (birth registration number)
 */
@Embeddable
public class Certidao {

    @Column(name = "num_certidao_nasc", length = 20, nullable = true)
    private String numeroCertidao;

    @Column(name = "liv_certidao_nasc", length = 10, nullable = true)
    private String livroCertidao;

    @Column(name = "fol_certidao_nasc", length = 10, nullable = true)
    private String folhaCertidao;

    @Column(name = "matricula_nascimento", length = 20, nullable = true)
    private String matriculaNascimento;

    public Certidao() {
        // JPA requires a default constructor
    }

    /**
     * Constructor to initialize certificate number, book and sheet.
     *
     * @param numeroCertidao   certificate number
     * @param livroCertidao    book number
     * @param folhaCertidao    sheet number
     */
    public Certidao(String numeroCertidao, String livroCertidao, String folhaCertidao) {
        this.setNumeroCertidao(numeroCertidao);
        this.setLivroCertidao(livroCertidao);
        this.setFolhaCertidao(folhaCertidao);
    }

    /**
     * Constructor to initialize only the birth registration number.
     *
     * @param matriculaNascimento birth registration number
     */
    public Certidao(String matriculaNascimento) {
        this.setMatriculaNascimento(matriculaNascimento);
    }

    public String getNumeroCertidao() {
        return numeroCertidao;
    }

    public void setNumeroCertidao(String numeroCertidao) {
        // Normalize to uppercase to match legacy behavior
        this.numeroCertidao = numeroCertidao != null ? numeroCertidao.toUpperCase() : null;
    }

    /**
     * Book number setter with uppercase normalization for legacy compatibility.
     *
     * @param livroCertidao book number
     */
    public void setLivroCertidao(String livroCertidao) {
        // Normalize to uppercase to match legacy behavior
        this.livroCertidao = livroCertidao != null ? livroCertidao.toUpperCase() : null;
    }

    public String getLivroCertidao() {
        return livroCertidao;
    }

    public String getFolhaCertidao() {
        return folhaCertidao;
    }

    /**
     * Sheet number setter with uppercase normalization for legacy compatibility.
     *
     * @param folhaCertidao sheet number
     */
    public void setFolhaCertidao(String folhaCertidao) {
        // Normalize to uppercase to match legacy behavior
        this.folhaCertidao = folhaCertidao != null ? folhaCertidao.toUpperCase() : null;
    }

    public String getMatriculaNascimento() {
        return matriculaNascimento;
    }

    public void setMatriculaNascimento(String matriculaNascimento) {
        // Normalize birth registration number to uppercase to preserve legacy behavior
        this.matriculaNascimento = matriculaNascimento != null ? matriculaNascimento.toUpperCase() : null;
    }

    @Override
    public String toString() {
        // Align with legacy Certidaodenascimento.toString() format and field names
        return "Certidao{" +
               "numerodacertidao=" + numeroCertidao +
               ", livrodacertidao=" + livroCertidao +
               ", folhadacertidao=" + folhaCertidao +
               ", matriculanascimento=" + matriculaNascimento +
               '}';
    }
}
