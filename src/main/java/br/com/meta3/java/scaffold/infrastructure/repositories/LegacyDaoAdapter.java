filetype
package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.services.LegacyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Adapter that exposes legacy-style method names expected by migrated legacy callers.
 *
 * Purpose:
 * - Provide compatibility layer mapping legacy-named methods (Portuguese + English variants)
 *   to the new LegacyDao abstraction methods.
 * - Preserve a small amount of legacy-like state (last PreparedStatement / ResultSet references)
 *   so callers that relied on DAO instance fields can continue to function when they are migrated
 *   gradually.
 *
 * Design decisions:
 * - Methods catch SQLException from LegacyDao and rethrow as UnsupportedOperationException to
 *   preserve legacy unchecked-exception semantics used by original code.
 * - Keeps lightweight fields (pstmt, rs) to mimic legacy DAO instance state; these reference the exact
 *   PreparedStatement/ResultSet objects returned by LegacyDao implementations when available.
 * - Both Portuguese and English method names are provided to ease incremental migration.
 *
 * TODO: (REVIEW) Once all legacy callers are migrated to use LegacyDao (or higher-level JdbcTemplate),
 *       remove this adapter and update callers to the new interface.
 */
@Repository
public class LegacyDaoAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDaoAdapter.class);

    private final LegacyDao legacyDao;

    /**
     * Last PreparedStatement returned/prepared via this adapter.
     * Preserved to mimic legacy DAO instance state.
     */
    private PreparedStatement pstmt;

    /**
     * Last ResultSet returned via this adapter.
     * Preserved to mimic legacy DAO instance state.
     */
    private ResultSet rs;

    public LegacyDaoAdapter(LegacyDao legacyDao) {
        this.legacyDao = legacyDao;
    }

    // -------------------------
    // Connection management
    // -------------------------

    /**
     * Legacy-named method: conectarBanco()
     * Delegates to LegacyDao.connect(null) (fallback to configured/default DataSource).
     */
    public void conectarBanco() {
        try {
            legacyDao.connect(null);
        } catch (SQLException ex) {
            LOGGER.error("conectarBanco() failed", ex);
            throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", ex);
        }
    }

    /**
     * Legacy-named method: conectarBanco(String tipobanco)
     * Delegates to LegacyDao.connect(tipobanco).
     *
     * @param tipobanco database type identifier (e.g., "oracle", "sqlserver")
     */
    public void conectarBanco(String tipobanco) {
        try {
            legacyDao.connect(tipobanco);
        } catch (SQLException ex) {
            LOGGER.error("conectarBanco({}) failed", tipobanco, ex);
            throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", ex);
        }
    }

    /**
     * English variant for compatibility: connect(...)
     *
     * @param dbType database type identifier
     */
    public void connect(String dbType) {
        conectarBanco(dbType);
    }

    // -------------------------
    // Query execution
    // -------------------------

    /**
     * Legacy-named method: executarQuery
     * Delegates to LegacyDao.executeQuery and stores the ResultSet.
     *
     * @param query SQL query to execute
     * @return ResultSet from execution
     */
    public ResultSet executarQuery(String query) {
        try {
            ResultSet result = legacyDao.executeQuery(query);
            this.rs = result;
            return result;
        } catch (RuntimeException rex) {
            // LegacyDao implementations may wrap SQLExceptions into unchecked exceptions already.
            LOGGER.error("Erro ao executarQuery", rex);
            throw new UnsupportedOperationException("Erro ao realizar transação com o banco de dados!", rex);
        }
    }

    /**
     * English variant: executeQuery
     *
     * @param query SQL query
     * @return ResultSet
     */
    public ResultSet executeQuery(String query) {
        return executarQuery(query);
    }

    // -------------------------
    // Prepared statements
    // -------------------------

    /**
     * Legacy-named method: executarPreparedQuery
     * Prepares a statement delegating to LegacyDao.executePreparedQuery and stores the PreparedStatement.
     *
     * @param query SQL to prepare
     * @return PreparedStatement ready for parameter binding
     */
    public PreparedStatement executarPreparedQuery(String query) {
        try {
            PreparedStatement ps = legacyDao.executePreparedQuery(query);
            this.pstmt = ps;
            return ps;
        } catch (RuntimeException rex) {
            LOGGER.error("Erro ao executarPreparedQuery", rex);
            throw new UnsupportedOperationException("Erro ao preparar a query!", rex);
        }
    }

    /**
     * English variant: executePreparedQuery
     *
     * @param query SQL to prepare
     * @return PreparedStatement
     */
    public PreparedStatement executePreparedQuery(String query) {
        return executarPreparedQuery(query);
    }

    /**
     * Legacy-named method: prepareInsert
     * Delegates to LegacyDao.prepareInsert and stores the PreparedStatement.
     *
     * Updated behavior:
     * - Delegates to legacyDao.prepareInsert(insert)
     * - Stores returned PreparedStatement in adapter.pstmt to preserve legacy instance state
     * - Catches SQLException and other runtime exceptions and wraps them into UnsupportedOperationException
     *   with a legacy-style message including underlying exception message for better diagnostics.
     *
     * Rationale:
     * - Centralizes error semantics in the adapter so callers see consistent unchecked-exception behavior
     *   matching historical code.
     *
     * TODO: (REVIEW) Consider logging a deprecation warning when this adapter method is called to help identify
     *       call sites that should migrate to LegacyDao or JdbcTemplate directly.
     *
     * @param insert INSERT SQL
     * @return PreparedStatement
     */
    public PreparedStatement prepareInsert(String insert) {
        try {
            // Delegate to LegacyDao.prepareInsert to centralize PreparedStatement creation and connection handling.
            PreparedStatement ps = legacyDao.prepareInsert(insert);
            // Preserve adapter-level state as legacy code expects dao instance to hold the last pstmt.
            this.pstmt = ps;
            return ps;
        } catch (SQLException | RuntimeException ex) {
            // Centralize legacy error semantics: wrap checked SQLException and runtime exceptions into unchecked
            LOGGER.error("Erro ao prepareInsert", ex);
            // Preserve detailed message similar to legacy implementations (include cause message)
            String msg = "Erro ao preparar o insert!\n\n" + (ex.getMessage() != null ? ex.getMessage() : ex.toString());
            throw new UnsupportedOperationException(msg, ex);
        }
    }

    /**
     * Portuguese legacy alias: preparaInsert
     *
     * @param insert INSERT SQL
     * @return PreparedStatement
     */
    public PreparedStatement preparaInsert(String insert) {
        return prepareInsert(insert);
    }

    /**
     * Legacy-named method: prepareUpdate
     * Delegates to LegacyDao.prepareUpdate and stores the PreparedStatement.
     *
     * @param update UPDATE SQL
     * @return PreparedStatement
     */
    public PreparedStatement prepareUpdate(String update) {
        try {
            PreparedStatement ps = legacyDao.prepareUpdate(update);
            this.pstmt = ps;
            return ps;
        } catch (RuntimeException rex) {
            LOGGER.error("Erro ao prepareUpdate", rex);
            throw new UnsupportedOperationException("Erro ao preparar o Update!", rex);
        }
    }

    /**
     * Portuguese legacy alias: preparaUpdate
     *
     * @param update UPDATE SQL
     * @return PreparedStatement
     */
    public PreparedStatement preparaUpdate(String update) {
        return prepareUpdate(update);
    }

    // -------------------------
    // Disconnect / cleanup
    // -------------------------

    /**
     * Legacy-named method: desconectarBanco
     * Delegates to LegacyDao.disconnect and clears local references to JDBC resources.
     */
    public void desconectarBanco() {
        try {
            legacyDao.disconnect();
        } catch (SQLException ex) {
            LOGGER.error("Erro ao desconectarBanco", ex);
            throw new UnsupportedOperationException("Erro ao desconectar do Banco!\n\n" + ex.getMessage(), ex);
        } finally {
            // Clear local references to help GC and match legacy behaviour of nulling fields.
            this.pstmt = null;
            this.rs = null;
        }
    }

    /**
     * English variant: disconnect
     */
    public void disconnect() {
        desconectarBanco();
    }

    // -------------------------
    // Helpers / Accessors
    // -------------------------

    /**
     * Returns the last prepared statement created by this adapter (may be null).
     * Useful for legacy code that relied on the DAO instance holding statement references.
     */
    public PreparedStatement getLastPreparedStatement() {
        return this.pstmt;
    }

    /**
     * Returns the last ResultSet returned by this adapter (may be null).
     */
    public ResultSet getLastResultSet() {
        return this.rs;
    }

    // -------------------------
    // Backwards-compatible small shims (no-op wrappers that mimic legacy naming)
    // -------------------------

    /**
     * Legacy method present in original codebase: prepareUpdate(String insert)
     * Kept to avoid breaking callers that used this signature where name was inconsistent.
     *
     * NOTE:
     * - Historically some callers invoked a void-style prepareUpdate(insert) expecting the adapter to prepare
     *   the statement and store it inside the DAO instance (pstmt) for later use. The original migration preserved
     *   prepareUpdateVoid that called prepareUpdate(insert) which in turn delegates to LegacyDao.prepareUpdate(...).
     *
     * - For backward compatibility with older callers that expected a void-style shim and in recognition that many
     *   legacy implementations reused the insert-preparation path for updates, we retain an explicit shim that
     *   delegates to prepareInsert(insert) to mirror that legacy semantic.
     *
     * This method remains intentionally void to match legacy call-sites that did not use the returned PreparedStatement.
     *
     * @param insert SQL string (update)
     */
    public void prepareUpdateVoid(String insert) {
        // Keep previous behavior for compatibility with code that relied on calling prepareUpdateVoid.
        // We delegate to prepareUpdate to preserve existing semantics.
        prepareUpdate(insert);
    }

    /**
     * Backwards-compatible void shim for legacy callers that expected a void prepareUpdate(insert).
     *
     * WHY:
     * - Preserve legacy callers that relied on a void-style prepareUpdate which internally reused the
     *   insert preparation logic (some historical implementations prepared updates using the insert path).
     *
     * WHERE:
     * - LegacyDaoAdapter class
     *
     * HOW:
     * - Provide this method to call prepareInsert(insert) while preserving existing prepareUpdate/prepareInsert signatures.
     *
     * Legacy usage reference:
     * - Older codebases invoked dao.prepareUpdate(sql) (void) and then relied on the adapter's internal pstmt
     *   state. This shim explicitly documents and preserves that behaviour for callers still using the void-style API.
     *
     * TODO: (REVIEW) Deprecate this method once all legacy callers are migrated to PreparedStatement-returning methods
     *       (prepareInsert/prepareUpdate) or to the LegacyDao interface directly.
     *
     * @param insert SQL string to prepare (treated via insert preparation path for backward compatibility)
     */
    public void prepareUpdateLegacy(String insert) {
        // Delegate to prepareInsert to mimic legacy implementations that reused insert preparation logic for updates.
        prepareInsert(insert);
    }

    // ------------------------------------------------------------------------
    // Notes:
    // - This adapter intentionally keeps a minimal surface area: just enough legacy-named methods
    //   to ease migration. It delegates the actual JDBC work to LegacyDao implementations.
    // - Callers should be migrated to use LegacyDao directly or, preferably, JdbcTemplate/Repositories.
    // ------------------------------------------------------------------------
}