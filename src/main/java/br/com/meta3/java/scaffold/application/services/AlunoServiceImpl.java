package br.com.meta3.java.scaffold.application.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.meta3.java.scaffold.domain.services.AlunoService;
import br.com.meta3.java.scaffold.domain.repositories.AlunoRepository;
import br.com.meta3.java.scaffold.domain.entities.Aluno;
import br.com.meta3.java.scaffold.infrastructure.repositories.AlunoDaoImpl;

import java.util.List;

/**
 * Implementation of the AlunoService interface.
 * Delegates new persistence to JPA repository and legacy operations to AlunoDaoImpl.
 */
@Service
@Transactional
public class AlunoServiceImpl implements AlunoService {

    private final AlunoRepository alunoRepository;
    private final AlunoDaoImpl alunoDao;

    public AlunoServiceImpl(AlunoRepository alunoRepository,
                            AlunoDaoImpl alunoDao) {
        this.alunoRepository = alunoRepository;
        this.alunoDao = alunoDao;
    }

    /**
     * Persists a new Aluno record capturing the client IP, operation type, and email.
     * Delegates to JPA repository and legacy DAO.
     */
    @Override
    public int inserirAluno(String ipCliente, String tipo, String email) {
        // Build new Aluno entity for JPA persistence
        Aluno aluno = new Aluno();
        aluno.setIpCliente(ipCliente);
        aluno.setTipoOperacao(tipo);
        aluno.setEmail(email);
        // TODO: (REVIEW) Map additional fields from AlunoRequest (e.g., nome, anoVigencia) as needed

        // Persist to application DB via JPA
        alunoRepository.save(aluno);

        // Delegate actual insert to legacy DAO for external legacy database
        // TODO: (REVIEW) Confirm legacy DAO covers all required fields mapping including email
        return alunoDao.inserirAluno(aluno, ipCliente, tipo);
    }

    /**
     * Validates a date string and age constraints by delegating to legacy DAO.
     */
    @Override
    public int verificaData(String data) {
        return alunoDao.verificaData(data);
    }

    /**
     * Validates CPF format and checksum by delegating to legacy DAO.
     */
    @Override
    public boolean verificaCpf(String cpf) {
        return alunoDao.verificaCpf(cpf);
    }

    /**
     * Searches by codigoSetps, delegating to legacy DAO.
     */
    @Override
    public String pesquisaCodigosetps(String codigoSetps, String codigoEscola) {
        return alunoDao.pesquisaCodigosetps(codigoSetps, codigoEscola);
    }

    /**
     * Searches students by name (and optional filters) via legacy DAO.
     */
    @Override
    public List<Aluno> pesquisarNome(String nomeAluno,
                                     String dataNascimento,
                                     String cpf,
                                     String nomeMae) {
        return alunoDao.pesquisarNome(nomeAluno, dataNascimento, cpf, nomeMae);
    }

    /**
     * General search across multiple fields, delegating to legacy DAO.
     */
    @Override
    public Aluno pesquisarGeral(String rg,
                                String numeroCertidao,
                                String matricula,
                                String cpf,
                                String nomeMae,
                                String nomeAluno,
                                String dataNascimento) {
        // TODO: (REVIEW) Ensure AlunoDaoImpl implements this method or create it
        return alunoDao.pesquisarGeral(rg, numeroCertidao, matricula, cpf, nomeMae, nomeAluno, dataNascimento);
    }

    /**
     * Retrieves list of 'apto' students for the previous year not yet sent via legacy DAO.
     */
    @Override
    public List<Aluno> pesquisarAlunoApto(String[] initials,
                                          String codigoEscola,
                                          String anoVigencia,
                                          String dataNascimento) {
        // TODO: (REVIEW) Ensure AlunoDaoImpl implements this method or create it
        return alunoDao.pesquisarAlunoApto(initials, codigoEscola, anoVigencia, dataNascimento);
    }

    /**
     * Flags an Aluno as excluded (legacy 'E' flag) by delegating to the legacy DAO layer.
     */
    @Override
    public void excluirAluno(Aluno aluno) {
        alunoDao.excluirAluno(aluno);
    }
}
