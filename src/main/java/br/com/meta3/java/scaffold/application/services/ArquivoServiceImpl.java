package br.com.meta3.java.scaffold.application.services;

import br.com.meta3.java.scaffold.domain.entities.Arquivo;
import br.com.meta3.java.scaffold.domain.repositories.ArquivoRepository;
import br.com.meta3.java.scaffold.domain.services.ArquivoService;
import br.com.meta3.java.scaffold.infrastructure.repositories.SQLExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service implementation for Arquivo operations.
 */
@Service
public class ArquivoServiceImpl implements ArquivoService {

    private final ArquivoRepository arquivoRepository;
    private final SQLExecutor sqlExecutor;

    public ArquivoServiceImpl(ArquivoRepository arquivoRepository,
                              SQLExecutor sqlExecutor) {
        this.arquivoRepository = arquivoRepository;
        this.sqlExecutor = sqlExecutor;
    }

    @Override
    public List<Arquivo> listBySchoolAndDateRange(Long codigoEscola,
                                                  LocalDate inicialData,
                                                  LocalDate finalData) {
        // TODO: (VALIDATION) Ensure the date range is valid
        if (inicialData != null && finalData != null && inicialData.isAfter(finalData)) {
            throw new IllegalArgumentException("Start date must be on or before end date");
        }
        // Delegate to repository for fetching data
        return arquivoRepository.listBySchoolAndDateRange(codigoEscola, inicialData, finalData);
    }

    /**
     * Executes a custom native SQL query using SQLExecutor.
     * Replaces legacy DAO.executarQuery behavior with executeNativeQuery delegation.
     *
     * @param sql    the native SQL string
     * @param params positional parameters for the query
     * @return list of Object[] each representing a row
     * @throws DataAccessException in case of any data access errors
     */
    public List<Object[]> executeCustomQuery(String sql, Object... params) throws DataAccessException {
        // TODO: (REVIEW) Delegating to SQLExecutor for native SQL execution
        return sqlExecutor.executeNativeQuery(sql, params);
    }
}
