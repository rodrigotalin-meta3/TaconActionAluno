package br.com.meta3.java.scaffold.infrastructure.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Utility for executing native SQL queries using JPA EntityManager.
 * Maps any JPA exceptions to Spring's DataAccessException hierarchy.
 */
@Repository
public class SQLExecutorImpl implements SQLExecutor {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Executes a native SQL query with positional parameters.
     *
     * @param sql    the native SQL string
     * @param params positional parameters for the query
     * @return result list of Object arrays, each array representing a row
     * @throws DataAccessException in case of any persistence or data access errors
     */
    @Override
    public List<Object[]> executeNativeQuery(String sql, Object... params) throws DataAccessException {
        try {
            Query query = entityManager.createNativeQuery(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    // JPA positional parameters are 1-based
                    query.setParameter(i + 1, params[i]);
                }
            }
            @SuppressWarnings("unchecked")
            List<Object[]> results = (List<Object[]>) query.getResultList();
            return results;
        } catch (PersistenceException | IllegalArgumentException ex) {
            // TODO: (REVIEW) Wrap JPA exceptions into Spring DataAccessException
            throw new DataAccessResourceFailureException("Error executing native SQL query", ex);
        }
    }
}

