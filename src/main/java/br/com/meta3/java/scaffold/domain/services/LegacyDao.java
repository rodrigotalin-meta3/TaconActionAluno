package br.com.meta3.java.scaffold.domain.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstraction of legacy DAO operations for direct JDBC interactions.
 * Provides methods for connecting, executing queries, preparing statements, and disconnecting.
 *
 * Purpose:
 * - Define a minimal, testable contract used by infrastructure implementations (LegacyDaoImpl)
 *   so application/infrastructure code can perform legacy-style JDBC operations without direct
 *   dependency on DriverManager/Oracle/SQLServer APIs.
 *
 * Design decisions and notes:
 * - Methods declare SQLException to reflect low-level JDBC failures. Implementations may choose
 *   to wrap checked exceptions in runtime exceptions for simpler callers, but declaring SQLException
 *   makes the contract explicit.
 *
 * - Returning raw java.sql.ResultSet and PreparedStatement retains parity with the legacy code,
 *   but places responsibility for resource management on the caller. Implementations SHOULD document
 *   and encourage patterns that ensure proper closing of resources (or provide helper methods / wrappers).
 *
 * - The interface intentionally mirrors the procedural API used by the legacy DAO to minimize changes
 *   when migrating each legacy method. Future refactors should consider replacing ResultSet/PreparedStatement
 *   usage with higher-level abstractions (JdbcTemplate, RowMapper, or repository interfaces).
 *
 * TODOs:
 * - (REVIEW) Consider introducing safe helper methods that accept SQL and parameters and return typed results
 *   to avoid exposing ResultSet/PreparedStatement to most callers, reducing resource-leak risks.
 * - (SECURITY) Avoid inlining collections into SQL strings in callers. Prefer implementations that accept
 *   structured parameters or NamedParameterJdbcTemplate for collection binding to prevent SQL injection.
 */
public interface LegacyDao {

    /**
     * Establishes a connection to the specified database type (e.g., "oracle", "sqlserver").
     *
     * Implementation notes:
     * - Implementations may use Spring-managed DataSources or create vendor-specific DataSource objects.
     * - If dbType is null/empty or unsupported, implementations may choose a default (e.g., application DataSource).
     *
     * @param dbType the database type identifier
     * @throws SQLException if a database access error occurs
     */
    void connect(String dbType) throws SQLException;

    /**
     * Executes a raw SQL query and returns the ResultSet positioned before the first row.
     *
     * IMPORTANT:
     * - Callers are responsible for closing the returned ResultSet and any associated Statement/Connection
     *   through the disconnect() method or other resource-cleanup mechanisms provided by the implementation.
     *
     * @param sql the SQL query to execute
     * @return the ResultSet obtained from execution
     * @throws SQLException if a database access error occurs
     */
    ResultSet executeQuery(String sql) throws SQLException;

    /**
     * Prepares and returns a PreparedStatement for the given SQL statement.
     *
     * Implementation notes:
     * - The prepared statement is created against the connection established by connect(...).
     * - Callers must set parameters and execute (executeQuery/executeUpdate) as needed.
     * - Resource cleanup is the caller's responsibility (or should be coordinated with disconnect()).
     *
     * @param sql the SQL statement to prepare
     * @return the PreparedStatement ready for parameter binding and execution
     * @throws SQLException if a database access error occurs
     */
    PreparedStatement executePreparedQuery(String sql) throws SQLException;

    /**
     * Prepares and returns a PreparedStatement for an INSERT operation.
     *
     * Note:
     * - Kept separate to reflect legacy semantics where inserts were prepared using a dedicated helper.
     * - Implementations may reuse the same code path as executePreparedQuery.
     *
     * @param sql the INSERT SQL statement
     * @return the PreparedStatement ready for parameter binding and execution
     * @throws SQLException if a database access error occurs
     */
    PreparedStatement prepareInsert(String sql) throws SQLException;

    /**
     * Prepares and returns a PreparedStatement for an UPDATE operation.
     *
     * Note:
     * - Default behavior in many implementations is to reuse prepareInsert(sql).
     *
     * @param sql the UPDATE SQL statement
     * @return the PreparedStatement ready for parameter binding and execution
     * @throws SQLException if a database access error occurs
     */
    PreparedStatement prepareUpdate(String sql) throws SQLException;

    /**
     * Closes any open JDBC resources and the database connection.
     *
     * Implementation notes:
     * - Implementations should ensure it is safe to call disconnect() multiple times.
     * - Prefer closing ResultSet -> PreparedStatement/Statement -> Connection in that order.
     *
     * @throws SQLException if a database access error occurs during resource cleanup
     */
    void disconnect() throws SQLException;
}