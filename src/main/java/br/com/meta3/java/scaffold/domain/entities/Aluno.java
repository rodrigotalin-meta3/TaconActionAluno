package br.com.meta3.java.scaffold.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity representing a student (Aluno).
 * Maps legacy table ALU_LISTA_ALUNOS and its columns.
 */
@Entity
@Table(name = "ALU_LISTA_ALUNOS")
public class Aluno {

    /**
     * Primary key: legacy ALU_CONTROLE sequence value.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ALU_CONTROLE")
    private Long id;

    /**
     * Year of validity (legacy ANO_VIGENCIA column).
     */
    @Column(name = "ANO_VIGENCIA", nullable = false)
    private Integer anoVigencia;

    /**
     * School code (legacy COD_TITULAR column).
     */
    @Column(name = "COD_TITULAR", nullable = false)
    private Integer codigoEscola;

    /**
     * Student registration number (legacy MT_ALUNO).
     */
    @Column(name = "MT_ALUNO", length = 20)
    private String matricula;

    /**
     * Dependent code (legacy COD_DEPENDENTE).
     * Business ID from legacy; not used as @Id in JPA.
     */
    @Column(name = "COD_DEPENDENTE", length = 20)
    private String codigoSetps;

    /**
     * Full name of the student (legacy NOME_DEPENDENTE).
     */
// TODO: (REVIEW) Legacy column is 'NOME_DEPENDENTE'
    @Column(name = "NOME_DEPENDENTE", length = 100)
    private String nomeAluno;

    /**
     * Mother's name (legacy NOME_MAE).
     */
    @Column(name = "NOME_MAE", length = 100)
    private String nomeMae;

    /**
     * Father's name (legacy NOME_PAI).
     */
    @Column(name = "NOME_PAI", length = 100)
    private String nomePai;

    /**
     * Birth date (legacy DATA_NASCIMENTO).
     */
    @Column(name = "DATA_NASCIMENTO")
    private LocalDate dataNascimento;

    /**
     * Telephone number (legacy TELEFONE).
     */
    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    /**
     * Email address (legacy EMAIL).
     */
    @Column(name = "EMAIL", length = 100)
    private String email;

    /**
     * Gender (legacy SEXO_ALUNO).
     */
    @Column(name = "SEXO_ALUNO", length = 1)
    private String sexo;

    /**
     * Grade level (legacy GRAU_ESTUDANTE or GRAU).
     */
    @Column(name = "GRAU", length = 10)
    private String grau;

    /**
     * Series/period (legacy SERIE_PERIODO or SERIE).
     */
    @Column(name = "SERIE", length = 10)
    private String serie;

    /**
     * Shift/turn (legacy TURNO).
     */
    @Column(name = "TURNO", length = 10)
    private String turno;

    /**
     * Timestamp when the record arrived (legacy DT_CHEGADA).
     */
    @Column(name = "DT_CHEGADA")
    private LocalDateTime dataDeEnvio;

    /**
     * Embedded address details (legacy ENDERECO fields).
     */
// TODO: (REVIEW) Ensure an @Embeddable Endereco class exists in domain.entities
    @Embedded
    private Endereco endereco;

    /**
     * Embedded document details (legacy DOCUMENTO: RG/CPF & CERTIDAO).
     */
    @Embedded
    private Documento documento;

    /**
     * Client IP that performed insertion (legacy IP_SOLICITANTE).
     */
    @Column(name = "IP_SOLICITANTE", length = 50)
    private String ipCliente;

    /**
     * Origin function of insertion (legacy FUNCAO_ORIGEM_SITE).
     */
    @Column(name = "FUNCAO_ORIGEM_SITE", length = 50)
    private String tipoOperacao;

    public Aluno() {
        // JPA requires a default constructor
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Integer getAnoVigencia() {
        return anoVigencia;
    }

    public void setAnoVigencia(Integer anoVigencia) {
        this.anoVigencia = anoVigencia;
    }

    public Integer getCodigoEscola() {
        return codigoEscola;
    }

    public void setCodigoEscola(Integer codigoEscola) {
        this.codigoEscola = codigoEscola;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCodigoSetps() {
        return codigoSetps;
    }

    public void setCodigoSetps(String codigoSetps) {
        this.codigoSetps = codigoSetps;
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

    public String getNomePai() {
        return nomePai;
    }

    public void setNomePai(String nomePai) {
        this.nomePai = nomePai;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        this.grau = grau;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public LocalDateTime getDataDeEnvio() {
        return dataDeEnvio;
    }

    public void setDataDeEnvio(LocalDateTime dataDeEnvio) {
        this.dataDeEnvio = dataDeEnvio;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
    }

    public String getIpCliente() {
        return ipCliente;
    }

    public void setIpCliente(String ipCliente) {
        this.ipCliente = ipCliente;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(String tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    @Override
    public String toString() {
        return "Aluno{" +
                "id=" + id +
                ", anoVigencia=" + anoVigencia +
                ", codigoEscola=" + codigoEscola +
                ", matricula='" + matricula + '\'' +
                ", codigoSetps='" + codigoSetps + '\'' +
                ", nomeAluno='" + nomeAluno + '\'' +
                ", nomeMae='" + nomeMae + '\'' +
                ", nomePai='" + nomePai + '\'' +
                ", dataNascimento=" + dataNascimento +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                ", sexo='" + sexo + '\'' +
                ", grau='" + grau + '\'' +
                ", serie='" + serie + '\'' +
                ", turno='" + turno + '\'' +
                ", dataDeEnvio=" + dataDeEnvio +
                ", endereco=" + endereco +
                ", documento=" + documento +
                ", ipCliente='" + ipCliente + '\'' +
                ", tipoOperacao='" + tipoOperacao + '\'' +
                '}';
    }
}
