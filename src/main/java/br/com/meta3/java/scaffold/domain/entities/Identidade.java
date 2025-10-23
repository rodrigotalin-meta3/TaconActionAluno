package br.com.meta3.java.scaffold.domain.entities;

import br.com.meta3.java.scaffold.application.util.StringNormalizationUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

/**
 * Embeddable class representing identity document details for a student.
 * Maps CPF, RG, date of emission, and issuing authority.
 * Integrates special-character removal for RG, CPF, and issuing authority fields.
 */
@Embeddable
public class Identidade {

    /**
     * Cadastro de Pessoas Físicas (CPF) - exactly 11 digits, no punctuation.
     */
    @Column(name = "cpf", length = 11, nullable = true)
    private String cpf;

    /**
     * Registro Geral (RG) number, alphanumeric.
     */
    @Column(name = "rg", length = 20, nullable = true)
    private String rg;

    /**
     * Date of issuance of the RG document.
     * Stored as a DATE column in the database.
     */
    @Column(name = "dt_emissao_rg")
    private LocalDate dataEmissao;

    /**
     * Issuing authority for the RG.
     */
    @Column(name = "orgao_expedidor", length = 50, nullable = true)
    private String orgaoExpedidor;

    public Identidade() {
        // JPA requires a default constructor
    }

    public String getCpf() {
        return cpf;
    }

    /**
     * Sets the CPF after normalizing and removing any special characters.
     *
     * @param cpf raw CPF string, possibly containing punctuation
     */
    public void setCpf(String cpf) {
        this.cpf = (cpf != null ? removeSpecialChars(cpf) : null);
    }

    public String getRg() {
        return rg;
    }

    /**
     * Sets the RG after normalizing and removing any special characters.
     *
     * @param rg raw RG string, possibly containing punctuation or accents
     */
    public void setRg(String rg) {
        this.rg = (rg != null ? removeSpecialChars(rg) : null);
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public String getOrgaoExpedidor() {
        return orgaoExpedidor;
    }

    /**
     * Sets the issuing authority after normalizing special characters and converting to upper case.
     *
     * @param orgaoExpedidor raw issuing authority string
     */
    public void setOrgaoExpedidor(String orgaoExpedidor) {
        if (orgaoExpedidor == null) {
            this.orgaoExpedidor = null;
        } else {
            String cleaned = removeSpecialChars(orgaoExpedidor);
            this.orgaoExpedidor = cleaned != null ? cleaned.toUpperCase() : null;
        }
    }

    /**
     * Helper method to normalize and remove special/accented characters from strings,
     * delegates to StringNormalizationUtil.normalize.
     *
     * TODO: (REVIEW) Centralize normalization logic in StringNormalizationUtil.
     *
     * @param value input string to sanitize
     * @return sanitized string with accents replaced and apostrophes removed
     */
    private static String removeSpecialChars(String value) {
        // Delegate normalization to utility class
        return StringNormalizationUtil.normalize(value);
    }

    @Override
    public String toString() {
        return "Identidade{" +
                "cpf='" + cpf + '\'' +
                ", rg='" + rg + '\'' +
                ", dataEmissao=" + dataEmissao +
                ", orgaoExpedidor='" + orgaoExpedidor + '\'' +
                '}';
    }
}
