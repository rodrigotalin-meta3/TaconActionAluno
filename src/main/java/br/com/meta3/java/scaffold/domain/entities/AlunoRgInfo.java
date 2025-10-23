package br.com.meta3.java.scaffold.domain.entities;

import java.util.Objects;

/**
 * DTO representing a dependent (aluno) that is missing RG information.
 *
 * Purpose:
 * - Extracted from ArquivoServiceImpl.AlunoRgInfo inner class to the domain layer
 *   so it can be reused across services, controllers and infrastructure.
 *
 * Design decisions:
 * - codDependente is modeled as long to match legacy ResultSet#getLong("cod_dependente")
 *   and to avoid unnecessary parsing in callers.
 * - dataNascimento is kept as String in 'dd/MM/yyyy' format because legacy queries return
 *   formatted date strings and several layers expect that format. Consider changing to
 *   java.time.LocalDate if consumers are migrated to use typed dates.
 *
 * TODO: (REVIEW) Replace dataNascimento String with LocalDate and centralize formatting/parsing
 *                 if future refactors standardize date handling across the application.
 */
public class AlunoRgInfo {

    /**
     * Identifier of the dependent student (legacy cod_dependente).
     */
    private long codDependente;

    /**
     * Student registration (mt_aluno).
     */
    private String matricula;

    /**
     * Full name of the dependent student (nome_dependente).
     */
    private String nomeDependente;

    /**
     * Birth date formatted as 'dd/MM/yyyy' (legacy formatting).
     */
    private String dataNascimento;

    public AlunoRgInfo() {
    }

    public AlunoRgInfo(long codDependente, String matricula, String nomeDependente, String dataNascimento) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlunoRgInfo that = (AlunoRgInfo) o;
        return codDependente == that.codDependente &&
                Objects.equals(matricula, that.matricula) &&
                Objects.equals(nomeDependente, that.nomeDependente) &&
                Objects.equals(dataNascimento, that.dataNascimento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codDependente, matricula, nomeDependente, dataNascimento);
    }

    @Override
    public String toString() {
        return "AlunoRgInfo{" +
                "codDependente=" + codDependente +
                ", matricula='" + matricula + '\'' +
                ", nomeDependente='" + nomeDependente + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                '}';
    }
}