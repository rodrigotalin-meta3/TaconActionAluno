package br.com.meta3.java.scaffold.domain.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Embeddable class representing a student's document details,
 * grouping identity (RG/CPF) and birth certificate information.
 * Migrates legacy Documento class (with fields identidade and certidao).
 */
@Embeddable
public class Documento {

    // Formatter for parsing dates in 'dd/MM/yyyy' format matching legacy inputs
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Identity document details including CPF, RG, issue date, and issuing authority.
     */
    @Embedded
    private Identidade identidade = new Identidade();

    /**
     * Birth certificate details including certificate number, book, sheet, and registration number.
     */
    @Embedded
    private Certidao certidao = new Certidao();

    public Documento() {
        // JPA requires a default constructor
    }

    /**
     * Overloaded constructor matching legacy signature:
     * initializes both identidade and certidao with full details.
     *
     * @param rg                 Registro Geral (RG) number
     * @param orgaoExpedidor     Issuing authority for RG
     * @param dataEmissao        Date of RG issuance in 'dd/MM/yyyy' format
     * @param cpf                Cadastro de Pessoas Físicas (CPF) number
     * @param numeroCertidao     Birth certificate number
     * @param livroCertidao      Birth certificate book number
     * @param folhaCertidao      Birth certificate sheet number
     */
    public Documento(String rg,
                     String orgaoExpedidor,
                     String dataEmissao,
                     String cpf,
                     String numeroCertidao,
                     String livroCertidao,
                     String folhaCertidao) {
        Identidade ident = new Identidade();
        ident.setRg(rg);
        ident.setOrgaoExpedidor(orgaoExpedidor);
        // TODO: (REVIEW) Validate and handle parsing exceptions consistently
        try {
            LocalDate parsed = LocalDate.parse(dataEmissao, DATE_FORMATTER);
            ident.setDataEmissao(parsed);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Invalid date format for dataEmissao, expected dd/MM/yyyy", ex);
        }
        ident.setCpf(cpf);
        this.identidade = ident;

        // Initialize certidao with number, book and sheet
        Certidao cert = new Certidao(numeroCertidao, livroCertidao, folhaCertidao);
        this.certidao = cert;
    }

    /**
     * Overloaded constructor matching legacy signature:
     * initializes identidade and certidao with only birth registration number.
     *
     * @param rg                    Registro Geral (RG) number
     * @param orgaoExpedidor        Issuing authority for RG
     * @param dataEmissao           Date of RG issuance in 'dd/MM/yyyy' format
     * @param cpf                   Cadastro de Pessoas Físicas (CPF) number
     * @param matriculaNascimento   Birth registration number
     */
    public Documento(String rg,
                     String orgaoExpedidor,
                     String dataEmissao,
                     String cpf,
                     String matriculaNascimento) {
        Identidade ident = new Identidade();
        ident.setRg(rg);
        ident.setOrgaoExpedidor(orgaoExpedidor);
        try {
            LocalDate parsed = LocalDate.parse(dataEmissao, DATE_FORMATTER);
            ident.setDataEmissao(parsed);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Invalid date format for dataEmissao, expected dd/MM/yyyy", ex);
        }
        ident.setCpf(cpf);
        this.identidade = ident;

        // Initialize certidao with registration number only
        Certidao cert = new Certidao(matriculaNascimento);
        this.certidao = cert;
    }

    public Identidade getIdentidade() {
        return identidade;
    }

    public void setIdentidade(Identidade identidade) {
        this.identidade = identidade;
    }

    public Certidao getCertidao() {
        return certidao;
    }

    public void setCertidao(Certidao certidao) {
        this.certidao = certidao;
    }

    @Override
    public String toString() {
        return "Documento{" +
                "identidade=" + identidade +
                ", certidao=" + certidao +
                '}';
    }
}
