package br.com.meta3.java.scaffold.application.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.meta3.java.scaffold.domain.services.AlunoService;
import br.com.meta3.java.scaffold.domain.repositories.AlunoRepository;
import br.com.meta3.java.scaffold.domain.entities.Aluno;
import br.com.meta3.java.scaffold.domain.repositories.AlunoLegacyDao;
import br.com.meta3.java.scaffold.infrastructure.repositories.AlunoDaoImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementation of the AlunoService interface.
 * Delegates new persistence to JPA repository and legacy operations to an injected AlunoLegacyDao.
 *
 * Migration notes / decisions:
 * - The legacy implementation (AlunoDaoImpl) exposes several methods (verificaData, verificaCpf,
 *   pesquisaCodigosetps, pesquisarNome, pesquisarGeral, pesquisarAlunoApto, excluirAluno) that are
 *   not declared on the AlunoLegacyDao interface (which currently only declares inserirAluno).
 * - To keep the service decoupled from the concrete implementation while preserving functionality,
 *   this class depends on the AlunoLegacyDao abstraction. When callers require legacy methods not
 *   present in the interface, we attempt a safe cast to AlunoDaoImpl and call the concrete methods.
 *   If the injected bean is not an instance of AlunoDaoImpl, we provide a conservative fallback:
 *   - For queries/validations: return empty/negative results and log a warning.
 *   - For mutating operations: throw UnsupportedOperationException to highlight missing legacy support.
 *
 * TODO: (REVIEW) Consider extending AlunoLegacyDao to include all required legacy methods so explicit
 *                 contracts are available and casts are unnecessary.
 */
@Service
@Transactional
public class AlunoServiceImpl implements AlunoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlunoServiceImpl.class);

    private final AlunoRepository alunoRepository;
    private final AlunoLegacyDao alunoLegacyDao;

    public AlunoServiceImpl(AlunoRepository alunoRepository,
                            AlunoLegacyDao alunoLegacyDao) {
        this.alunoRepository = alunoRepository;
        this.alunoLegacyDao = alunoLegacyDao;
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

        // Delegate actual insert to legacy DAO for external legacy database via the AlunoLegacyDao abstraction
        try {
            return alunoLegacyDao.inserirAluno(aluno, ipCliente, tipo);
        } catch (Exception ex) {
            // Preserve legacy-like behavior: log and return failure code
            LOGGER.error("Erro ao delegar inserirAluno para legacy DAO", ex);
            return 0;
        }
    }

    /**
     * Validates a date string and age constraints by delegating to legacy DAO where available.
     */
    @Override
    public int verificaData(String data) {
        // Prefer calling concrete implementation when available
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                return ((AlunoDaoImpl) alunoLegacyDao).verificaData(data);
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar verificaData via AlunoDaoImpl", ex);
                return 0;
            }
        }
        // Fallback conservative behavior: cannot validate without legacy impl
        LOGGER.warn("verificaData requested but injected AlunoLegacyDao does not implement AlunoDaoImpl; returning 0");
        return 0;
    }

    /**
     * Validates CPF format and checksum by delegating to legacy DAO where available.
     */
    @Override
    public boolean verificaCpf(String cpf) {
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                return ((AlunoDaoImpl) alunoLegacyDao).verificaCpf(cpf);
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar verificaCpf via AlunoDaoImpl", ex);
                return false;
            }
        }
        LOGGER.warn("verificaCpf requested but injected AlunoLegacyDao does not implement AlunoDaoImpl; returning false");
        return false;
    }

    /**
     * Searches by codigoSetps, delegating to legacy DAO where available.
     */
    @Override
    public String pesquisaCodigosetps(String codigoSetps, String codigoEscola) {
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                return ((AlunoDaoImpl) alunoLegacyDao).pesquisaCodigosetps(codigoSetps, codigoEscola);
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar pesquisaCodigosetps via AlunoDaoImpl", ex);
                return "-1";
            }
        }
        LOGGER.warn("pesquisaCodigosetps requested but injected AlunoLegacyDao does not implement AlunoDaoImpl; returning -1");
        return "-1";
    }

    /**
     * Searches students by name (and optional filters) via legacy DAO where available.
     */
    @Override
    public List<Aluno> pesquisarNome(String nomeAluno, String dataNascimento, String cpf, String nomeMae) {
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                return ((AlunoDaoImpl) alunoLegacyDao).pesquisarNome(nomeAluno, dataNascimento, cpf, nomeMae);
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar pesquisarNome via AlunoDaoImpl", ex);
                return List.of();
            }
        }
        LOGGER.warn("pesquisarNome requested but injected AlunoLegacyDao does not implement AlunoDaoImpl; returning empty list");
        return List.of();
    }

    /**
     * General search across multiple fields, delegating to legacy DAO where available.
     */
    @Override
    public Aluno pesquisarGeral(String rg, String numeroCertidao, String matricula, String cpf, String nomeMae, String nomeAluno, String dataNascimento) {
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                return ((AlunoDaoImpl) alunoLegacyDao).pesquisarGeral(rg, numeroCertidao, matricula, cpf, nomeMae, nomeAluno, dataNascimento);
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar pesquisarGeral via AlunoDaoImpl", ex);
                return null;
            }
        }
        LOGGER.warn("pesquisarGeral requested but injected AlunoLegacyDao does not implement AlunoDaoImpl; returning null");
        return null;
    }

    /**
     * Retrieves list of 'apto' students for the previous year not yet sent via legacy DAO where available.
     */
    @Override
    public List<Aluno> pesquisarAlunoApto(String[] initials, String codigoEscola, String anoVigencia, String dataNascimento) {
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                return ((AlunoDaoImpl) alunoLegacyDao).pesquisarAlunoApto(initials, codigoEscola, anoVigencia, dataNascimento);
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar pesquisarAlunoApto via AlunoDaoImpl", ex);
                return List.of();
            }
        }
        LOGGER.warn("pesquisarAlunoApto requested but injected AlunoLegacyDao does not implement AlunoDaoImpl; returning empty list");
        return List.of();
    }

    /**
     * Flags an Aluno as excluded (legacy 'E' flag) by delegating to the legacy DAO layer where available.
     */
    @Override
    public void excluirAluno(Aluno aluno) {
        if (alunoLegacyDao instanceof AlunoDaoImpl) {
            try {
                ((AlunoDaoImpl) alunoLegacyDao).excluirAluno(aluno);
                return;
            } catch (Exception ex) {
                LOGGER.error("Erro ao executar excluirAluno via AlunoDaoImpl", ex);
                throw new RuntimeException("Erro ao excluir aluno via legacy DAO", ex);
            }
        }
        // If we cannot delegate to legacy impl, we choose to fail fast to avoid silent data divergence.
        String msg = "excluirAluno not supported: injected AlunoLegacyDao does not implement AlunoDaoImpl";
        LOGGER.error(msg);
        throw new UnsupportedOperationException(msg);
    }
}
