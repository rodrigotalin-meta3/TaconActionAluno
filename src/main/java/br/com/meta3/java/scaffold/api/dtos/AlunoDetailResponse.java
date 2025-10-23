package br.com.meta3.java.scaffold.api.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import br.com.meta3.java.scaffold.application.util.StringNormalizationUtil;
import java.util.Objects;

/**
 * DTO representing full detail of an Aluno fetched by codigoSetps.
 */
public class AlunoDetailResponse {

    @JsonProperty("codigoSetps")
    @JsonAlias("codigosetps")
    private String codigoSetps;

    @JsonProperty("codigoEscola")
    private String codigoEscola;

    @JsonProperty("matricula")
    private String matricula;

    @JsonProperty("nomeAluno")
    private String nomeAluno;

    @JsonProperty("nomeMae")
    private String nomeMae;

    @JsonProperty("nomePai")
    private String nomePai;

    @JsonProperty("dataNascimento")
    private String dataNascimento;

    @JsonProperty("telefone")
    private String telefone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("sexo")
    private String sexo;

    @JsonProperty("serie")
    private String serie;

    @JsonProperty("grau")
    private String grau;

    @JsonProperty("turno")
    private String turno;

    // Address fields
    @JsonProperty("logradouro")
    private String logradouro;

    @JsonProperty("numero")
    private String numero;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    private String bairro;

    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("cep")
    private String cep;

    // Document (Identidade) fields
    @JsonProperty("cpf")
    private String cpf;

    @JsonProperty("rg")
    private String rg;

    @JsonProperty("dataEmissaoRg")
    @JsonAlias("dataemissao")
    private String dataEmissaoRg;

    @JsonProperty("orgaoExpedidor")
    @JsonAlias("orgaoexpeditor")
    private String orgaoExpedidor;

    // Document (Certidao) fields
    @JsonProperty("numeroCertidao")
    private String numeroCertidao;

    @JsonProperty("livroCertidao")
    private String livroCertidao;

    @JsonProperty("folhaCertidao")
    @JsonAlias("folhadacertidao")
    private String folhaCertidao;

    @JsonProperty("matriculaNascimento")
    private String matriculaNascimento;

    public AlunoDetailResponse() {
        // Default constructor for JSON deserialization
    }

    public String getCodigoSetps() {
        return codigoSetps;
    }

    public void setCodigoSetps(String codigoSetps) {
        this.codigoSetps = codigoSetps;
    }

    public String getCodigoEscola() {
        return codigoEscola;
    }

    public void setCodigoEscola(String codigoEscola) {
        this.codigoEscola = codigoEscola;
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

    public String getNomePai() {
        return nomePai;
    }

    public void setNomePai(String nomePai) {
        this.nomePai = nomePai;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
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

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        this.grau = grau;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    /**
     * Sets the RG after normalizing special characters for backward compatibility.
     * TODO: (REVIEW) Normalize RG with StringNormalizationUtil.normalize().
     */
    public void setRg(String rg) {
        this.rg = (rg != null ? StringNormalizationUtil.normalize(rg) : null);
    }

    public String getDataEmissaoRg() {
        return dataEmissaoRg;
    }

    public void setDataEmissaoRg(String dataEmissaoRg) {
        this.dataEmissaoRg = dataEmissaoRg;
    }

    /**
     * Legacy setter for backward compatibility with legacy JSON field "dataemissao".
     */
    public void setDataemissao(String dataemissao) {
        this.dataEmissaoRg = dataemissao;
    }

    public String getOrgaoExpedidor() {
        return orgaoExpedidor;
    }

    public void setOrgaoExpedidor(String orgaoExpedidor) {
        this.orgaoExpedidor = orgaoExpedidor;
    }

    /**
     * Legacy setter for backward compatibility with legacy JSON field "orgaoexpeditor".
     * TODO: (REVIEW) Normalize by removing special characters and converting to upper case to match legacy behavior.
     */
    public void setOrgaoexpeditor(String orgaoexpeditor) {
        if (orgaoexpeditor == null) {
            this.orgaoExpedidor = null;
        } else {
            String cleaned = StringNormalizationUtil.normalize(orgaoexpeditor);
            this.orgaoExpedidor = (cleaned != null ? cleaned.toUpperCase() : null);
        }
    }

    public String getNumeroCertidao() {
        return numeroCertidao;
    }

    public void setNumeroCertidao(String numeroCertidao) {
        this.numeroCertidao = numeroCertidao;
    }

    public String getLivroCertidao() {
        return livroCertidao;
    }

    public void setLivroCertidao(String livroCertidao) {
        this.livroCertidao = livroCertidao;
    }

    public String getFolhaCertidao() {
        return folhaCertidao;
    }

    public void setFolhaCertidao(String folhaCertidao) {
        this.folhaCertidao = folhaCertidao;
    }

    public String getMatriculaNascimento() {
        return matriculaNascimento;
    }

    public void setMatriculaNascimento(String matriculaNascimento) {
        this.matriculaNascimento = matriculaNascimento;
    }

    @Override
    public String toString() {
        return "AlunoDetailResponse{" +
                "codigoSetps='" + codigoSetps + '\'' +
                ", codigoEscola='" + codigoEscola + '\'' +
                ", matricula='" + matricula + '\'' +
                ", nomeAluno='" + nomeAluno + '\'' +
                ", nomeMae='" + nomeMae + '\'' +
                ", nomePai='" + nomePai + '\'' +
                ", dataNascimento='" + dataNascimento + '\'' +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                ", sexo='" + sexo + '\'' +
                ", serie='" + serie + '\'' +
                ", grau='" + grau + '\'' +
                ", turno='" + turno + '\'' +
                ", logradouro='" + logradouro + '\'' +
                ", numero='" + numero + '\'' +
                ", complemento='" + complemento + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", cep='" + cep + '\'' +
                ", cpf='" + cpf + '\'' +
                ", rg='" + rg + '\'' +
                ", dataEmissaoRg='" + dataEmissaoRg + '\'' +
                ", orgaoExpedidor='" + orgaoExpedidor + '\'' +
                ", numeroCertidao='" + numeroCertidao + '\'' +
                ", livroCertidao='" + livroCertidao + '\'' +
                ", folhaCertidao='" + folhaCertidao + '\'' +
                ", matriculaNascimento='" + matriculaNascimento + '\'' +
                '}';
    }
}
