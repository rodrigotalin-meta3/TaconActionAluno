package br.com.meta3.java.scaffold.domain.services;

import java.util.List;
import br.com.meta3.java.scaffold.domain.entities.Aluno;

/**
 * Service abstraction for Aluno-related business operations.
 */
public interface AlunoService {

    /**
     * Persists a new Aluno record capturing the client IP, operation type, and email.
     *
     * @param ipCliente the IP address of the client performing the operation
     * @param tipo      the type of operation (e.g., "INCLUSAO", "EXCLUSAO")
     * @param email     the email to be stored for the Aluno
     * @return 1 if insertion was successful; other values indicate failure
     */
    // TODO: (REVIEW) Updated signature to include 'email' parameter to align with AlunoServiceImpl
    int inserirAluno(String ipCliente, String tipo, String email);

    /**
     * Verifies if a date string is valid and meets age requirements.
     *
     * @param data date in 'dd/MM/yyyy' format
     * @return 1 if valid; 0 otherwise
     */
    int verificaData(String data);

    /**
     * Validates a CPF number for format and checksum.
     *
     * @param cpf string with exactly 11 digits
     * @return true if CPF is valid; false otherwise
     */
    boolean verificaCpf(String cpf);

    /**
     * Searches by codigoSetps and populates Aluno attributes.
     *
     * @param codigoSetps   identifier of the dependent
     * @param codigoEscola  titular (school) code for filtering
     * @return the same codigoSetps if found; "-1" on error
     */
    String pesquisaCodigosetps(String codigoSetps, String codigoEscola);

    /**
     * Searches students by name, optional birth date, cpf, and mother's name.
     *
     * @param nomeAluno       full or partial student name
     * @param dataNascimento  birth date filter (optional)
     * @param cpf             CPF filter (optional)
     * @param nomeMae         mother's name filter (optional)
     * @return list of matching Aluno entities
     */
    List<Aluno> pesquisarNome(String nomeAluno,
                              String dataNascimento,
                              String cpf,
                              String nomeMae);

    /**
     * General search across multiple legacy fields populating an Aluno.
     *
     * @param rg                identity RG (optional)
     * @param numeroCertidao    birth certificate number (optional)
     * @param matricula         student registration (optional)
     * @param cpf               CPF filter (optional)
     * @param nomeMae           mother's name filter (optional)
     * @param nomeAluno         student name filter (optional)
     * @param dataNascimento    birth date filter (optional)
     * @return populated Aluno or null if none found
     */
    Aluno pesquisarGeral(String rg,
                         String numeroCertidao,
                         String matricula,
                         String cpf,
                         String nomeMae,
                         String nomeAluno,
                         String dataNascimento);

    /**
     * Retrieves list of 'apto' students for the previous year not yet sent.
     *
     * @param initials        array of initial letters to filter names
     * @param codigoEscola    titular code
     * @param anoVigencia     year of validity
     * @param dataNascimento  birth date filter (optional)
     * @return list of apto Aluno entities
     */
    List<Aluno> pesquisarAlunoApto(String[] initials,
                                   String codigoEscola,
                                   String anoVigencia,
                                   String dataNascimento);

    /**
     * Flags an Aluno as excluded (legacy 'E' flag) in the system.
     *
     * @param aluno Aluno entity to exclude
     */
    void excluirAluno(Aluno aluno);
}
