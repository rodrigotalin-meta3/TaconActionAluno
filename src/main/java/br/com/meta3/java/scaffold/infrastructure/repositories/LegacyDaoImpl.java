filetype
package br.com.meta3.java.scaffold.infrastructure.repositories;

import br.com.meta3.java.scaffold.domain.services.LegacyDao;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Implementation of LegacyDao using Spring-managed DataSource and JdbcTemplate.
 * Migrates legacy connection logic, query execution, prepared statements, and resource cleanup.
 *
 * Design notes:
 * - This class attempts to preserve legacy behavior:
 *   * supports explicit "oracle" and "sqlserver" connections using vendor DataSource classes
 *   * falls back to the Spring-managed DataSource (H2 in-memory by default) when dbType is null/unsupported
 * - JDBC resources (Connection, Statement, PreparedStatement, ResultSet) are held as instance fields to
 *   mimic the legacy lifecycle where callers call disconnect() to release them. Callers MUST call disconnect()
 *   or rely on finally blocks when interacting with this DAO to avoid resource leaks.
 *
 * Important security note:
 * - Several legacy methods inline collections into SQL strings. Avoid building SQL with user input directly.
 *   Prefer using JdbcTemplate / NamedParameterJdbcTemplate in new code paths.
 */
@Repository
public class LegacyDaoImpl implements LegacyDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyDaoImpl.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    // ------------------------------------------------------------------------
    // Legacy DB configuration injected from application.properties
    // ------------------------------------------------------------------------
    @Value("${legacy.oracle.url}")
    private String urlOracle;

    @Value("${legacy.oracle.username}")
    private String userOracle;

    @Value("${legacy.oracle.password}")
    private String passwordOracle;

    @Value("${legacy.sqlserver.user}")
    private String userSqlServer;

    @Value("${legacy.sqlserver.password}")
    private String passwordSqlServer;

    @Value("${legacy.sqlserver.server-name}")
    private String serverNameSqlServer;

    @Value("${legacy.sqlserver.port}")
    private int portNumberSqlServer;

    @Value("${legacy.sqlserver.database-name}")
    private String databaseNameSqlServer;

    // JDBC resources for manual legacy operations
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmt;
    private ResultSet rs;

    @Autowired
    public LegacyDaoImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Establishes a connection to the specified database type (e.g., "oracle", "sqlserver").
     * Default fallback to Spring-managed DataSource (H2) when dbType is null, empty, or unsupported.
     *
     * Notes & decisions:
     * - We intentionally attempt to load vendor drivers via Class.forName to mirror legacy behavior.
     *   If driver classes are not present on the classpath, a warning is logged and we fall back to the
     *   Spring-managed datasource when appropriate.
     *
     * - Using vendor DataSource objects (OracleDataSource / SQLServerDataSource) allows explicit connections
     *   to legacy DBs without interfering with the application's primary DataSource.
     *
     * - When falling back to the Spring-managed DataSource we rely on it being correctly configured (H2 by default).
     *
     * Exception handling:
     * - Legacy callers and adapter expect unchecked exceptions with legacy-style messages in many places.
     *   To preserve that behavior and provide clearer migration signals, SQLExceptions encountered here are
     *   logged in detail and wrapped into UnsupportedOperationException with a legacy message.
     *
     * TODO: (REVIEW) Consider adding a distinct exception type (e.g., LegacyConnectionException) to avoid conflating
     * runtime exceptions with SQL-related failures, while still preserving unchecked semantics.
     *
     * @param dbType the database type identifier
     * @throws SQLException kept in the signature to satisfy the LegacyDao contract, but implementations
     *                      will generally wrap SQLExceptions into UnsupportedOperationException for legacy parity.
     */
    @Override
    public void connect(String dbType) throws SQLException {
        // Close existing connection if still open
        if (this.conn != null) {
            try {
                if (!this.conn.isClosed()) {
                    this.conn.close();
                }
            } catch (SQLException ex) {
                LOGGER.warn("Error closing existing legacy connection", ex);
            } finally {
                this.conn = null;
            }
        }

        try {
            // Handle null or empty dbType: fallback to Spring-managed DataSource (H2)
            if (dbType == null || dbType.trim().isEmpty()) {
                LOGGER.info("dbType is null/empty, defaulting to Spring-managed DataSource (H2)");
                try {
                    this.conn = dataSource.getConnection();
                } catch (SQLException ex) {
                    // Wrap into legacy-style unchecked exception for parity with adapter behavior
                    LOGGER.error("Error connecting to default DataSource due to null/empty dbType", ex);
                    throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", ex);
                }
                return;
            }

            // Oracle legacy branch
            if ("oracle".equalsIgnoreCase(dbType)) {
                try {
                    // Attempt to load Oracle driver if available. If not present, log and continue;
                    // we still try to use OracleDataSource which may fail if driver is absent.
                    Class.forName("oracle.jdbc.OracleDriver");
                } catch (ClassNotFoundException e) {
                    // Driver not present; log and proceed to attempt connection which will fail with SQLException.
                    LOGGER.warn("Oracle JDBC Driver not found in classpath", e);
                }
                OracleDataSource ods = new OracleDataSource();
                ods.setURL(urlOracle);
                // Preserve explicit credentials configuration as in legacy code
                ods.setUser(userOracle);
                ods.setPassword(passwordOracle);
                try {
                    this.conn = ods.getConnection();
                } catch (SQLException ex) {
                    LOGGER.error("Erro ao conectar no Oracle usando LegacyDaoImpl", ex);
                    // Wrap into legacy-style unchecked exception for compatibility with adapter and callers
                    throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", ex);
                }

            // SQL Server legacy branch
            } else if ("sqlserver".equalsIgnoreCase(dbType)) {
                try {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("SQLServer JDBC Driver class not found", e);
                }
                SQLServerDataSource ssds = new SQLServerDataSource();
                ssds.setUser(userSqlServer);
                ssds.setPassword(passwordSqlServer);
                ssds.setServerName(serverNameSqlServer);
                ssds.setPortNumber(portNumberSqlServer);
                ssds.setDatabaseName(databaseNameSqlServer);
                try {
                    this.conn = ssds.getConnection();
                } catch (SQLException ex) {
                    LOGGER.error("Erro ao conectar no SQLServer usando LegacyDaoImpl", ex);
                    throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", ex);
                }

            // Unsupported dbType: fallback to Spring-managed DataSource (H2)
            } else {
                LOGGER.warn("Unsupported dbType '{}', falling back to Spring-managed DataSource (H2)", dbType);
                try {
                    this.conn = dataSource.getConnection();
                } catch (SQLException ex) {
                    LOGGER.error("Error connecting to default DataSource due to unsupported dbType '{}'", dbType, ex);
                    throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", ex);
                }
            }
        } catch (UnsupportedOperationException uoex) {
            // rethrow unchanged to preserve original cause and legacy message semantics
            throw uoex;
        } catch (RuntimeException rex) {
            // Defensive: wrap unexpected runtime exceptions into legacy-style unchecked exception
            LOGGER.error("Unexpected runtime error while connecting to legacy DB (dbType={})", dbType, rex);
            throw new UnsupportedOperationException("Não foi possivel conectar com o banco de dados!", rex);
        }
    }

    /**
     * Executes a raw SQL query and returns the ResultSet positioned before the first row.
     *
     * IMPORTANT:
     * - Callers are responsible for closing the returned ResultSet and any associated Statement/Connection
     *   through the disconnect() method or other resource-cleanup mechanisms.
     *
     * Implementation note:
     * - The legacy code wrapped SQLExceptions into runtime UnsupportedOperationException; we preserve that
     *   semantic to avoid changing many call sites. However, the interface declares SQLException for clarity.
     *
     * Behavior ensured by migration step:
     * - If there is no active connection, we open one via connect(null) (fallback to default DataSource).
     * - The created Statement and produced ResultSet are stored into this.stmt and this.rs instance fields
     *   to preserve legacy DAO stateful behavior expected by migrated callers.
     * - Any SQLException is logged with full context and wrapped into an UnsupportedOperationException with
     *   the legacy message "Erro ao realizar transação com o banco de dados!" to keep parity with original code.
     *
     * TODO: (REVIEW) Consider returning a small Result wrapper that encourages callers to close resources via try-with-resources,
     *                reducing the risk of leaks if disconnect() is not called.
     */
    @Override
    public ResultSet executeQuery(String sql) {
        try {
            if (this.conn == null || this.conn.isClosed()) {
                // Fallback to default datasource when no specific dbType was provided.
                // This mirrors legacy behavior where a default connection is used if none was explicitly opened.
                connect(null);
            }
            // Prepare a Statement on the active connection and preserve references to mimic legacy DAO instance state.
            this.stmt = this.conn.createStatement();
            this.rs = this.stmt.executeQuery(sql);
            return this.rs;
        } catch (SQLException ex) {
            // Log detailed context and preserve legacy unchecked-exception semantics expected by higher layers.
            LOGGER.error("Erro ao executar query no banco de dados: {}", sql, ex);
            // Preserve legacy message to minimize behavioral differences after migration.
            throw new UnsupportedOperationException("Erro ao realizar transação com o banco de dados!", ex);
        } catch (RuntimeException rex) {
            // Defensive: wrap unexpected runtime exceptions to provide consistent legacy-style unchecked semantics.
            LOGGER.error("Runtime error ao executar query: {}", sql, rex);
            throw new UnsupportedOperationException("Erro ao realizar transação com o banco de dados!", rex);
        }
    }

    /**
     * Prepares and returns a PreparedStatement for the given SQL statement.
     *
     * Callers must bind parameters and execute the statement. Resource cleanup responsibility stays with the caller.
     *
     * Implementation decision:
     * - This method maps the legacy pattern of calling Connection.prepareStatement(query),
     *   storing the prepared statement in the instance field 'pstmt' (to mimic legacy DAO state),
     *   logging SQLException via SLF4J, and rethrowing as UnsupportedOperationException on failure.
     *
     * TODO: (REVIEW) Consider returning/wrapping a helper that ensures the PreparedStatement/Connection
     *       are closed automatically to avoid resource leaks if callers forget to call disconnect().
     */
    @Override
    public PreparedStatement executePreparedQuery(String sql) {
        try {
            if (this.conn == null || this.conn.isClosed()) {
                // fallback to default DataSource connection when no specific dbType was provided
                connect(null);
            }
            // Prepare statement on the established connection and preserve reference for legacy callers
            this.pstmt = this.conn.prepareStatement(sql);
            return this.pstmt;
        } catch (SQLException ex) {
            // Log full context via SLF4J and preserve the legacy unchecked-exception behavior
            LOGGER.error("Erro ao preparar a query: {}", sql, ex);
            throw new UnsupportedOperationException("Erro ao preparar a query!", ex);
        }
    }

    /**
     * Prepares and returns a PreparedStatement for an INSERT operation.
     *
     * Note: kept separate to reflect legacy semantics but reuses the same code path as executePreparedQuery.
     *
     * The implementation below:
     * - Ensures a connection exists (connect(null) fallback).
     * - Uses Connection.prepareStatement(sql) to create the PreparedStatement.
     * - Assigns the resulting PreparedStatement to the instance field 'pstmt' to preserve legacy DAO state.
     * - Logs SQLException context and wraps the exception into an UnsupportedOperationException with the
     *   legacy-style message "Erro ao preparar o insert!\n\n" + ex.getMessage() as required by migration task.
     *
     * TODO: (REVIEW) Consider returning a wrapper that tracks resource ownership so callers are less error-prone.
     */
    @Override
    public PreparedStatement prepareInsert(String sql) {
        try {
            if (this.conn == null || this.conn.isClosed()) {
                connect(null);
            }
            // Create PreparedStatement on the active connection and store it for legacy callers that expect instance state.
            this.pstmt = this.conn.prepareStatement(sql);
            return this.pstmt;
        } catch (SQLException ex) {
            // Detailed logging for diagnostics while preserving legacy unchecked-exception semantics.
            LOGGER.error("Erro ao preparar o insert: {}", sql, ex);
            // Preserve legacy message format exactly as requested by migration task.
            throw new UnsupportedOperationException("Erro ao preparar o insert!\n\n" + ex.getMessage(), ex);
        }
    }

    /**
     * Prepares and returns a PreparedStatement for an UPDATE operation.
     *
     * Legacy equivalence & rationale:
     * - Historically legacy implementations reused the "insert" preparation path for update statements.
     * - To preserve that behaviour and minimize differences for migrated callers, this method delegates
     *   to prepareInsert(sql) and returns its PreparedStatement.
     *
     * Fallback semantics:
     * - prepareInsert may open a connection (connect(null) fallback) and set the instance field 'pstmt'.
     * - Any SQLException thrown by prepareInsert is propagated as an UnsupportedOperationException per legacy patterns.
     *
     * NOTE:
     * - Keep this delegation to avoid duplicating logic and to maintain the instance field 'pstmt' used by callers.
     *
     * @param sql the UPDATE SQL statement
     * @return PreparedStatement prepared on the active connection
     * @throws SQLException if preparing the statement fails (propagated from prepareInsert)
     */
    @Override
    public PreparedStatement prepareUpdate(String sql) throws SQLException {
        // Delegate to prepareInsert to mirror legacy implementations that reused the insert path for update statements.
        return prepareInsert(sql);
    }

    /**
     * Closes any open JDBC resources and the database connection.
     *
     * Implementation notes:
     * - Safe to call multiple times.
     * - Closes resources in the recommended order: ResultSet -> PreparedStatement -> Statement -> Connection.
     */
    @Override
    public void disconnect() throws SQLException {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
                rs = null;
            }
            if (pstmt != null && !pstmt.isClosed()) {
                pstmt.close();
                pstmt = null;
            }
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
                stmt = null;
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erro ao desconectar do banco de dados", ex);
            // Preserve legacy behavior by wrapping the checked exception in an unchecked one,
            // but also rethrow as SQLException per interface contract.
            throw new UnsupportedOperationException("Erro ao desconectar do Banco!\n\n" + ex.getMessage(), ex);
        }
    }

    /**
     * Exposes JdbcTemplate for advanced legacy query needs.
     *
     * TODO: (REVIEW) Encourage new code to use JdbcTemplate rather than manual ResultSet handling.
     */
    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }
}