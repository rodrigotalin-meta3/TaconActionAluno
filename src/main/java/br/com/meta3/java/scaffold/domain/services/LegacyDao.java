package br.com.meta3.java.scaffold.domain.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstraction of legacy DAO operations for direct JDBC interactions.
 * Provides methods for connecting, executing queries, preparing statements, and disconnecting.
 */
public interface LegacyDao {

    /**
     * Establishes a connection to the specified database type (e.g., "oracle", "sqlserver").
     *
     * @param dbType the database type identifier
     * @throws SQLException if a database access error occurs
     */
    void connect(String dbType) throws SQLException;

    /**
     * Executes a raw SQL query.
     *
     * @param sql the SQL query to execute
     * @return the ResultSet obtained from execution
     * @throws SQLException if a database access error occurs
     */
    ResultSet executeQuery(String sql) throws SQLException;

    /**
     * Prepares and returns a PreparedStatement for the given SQL statement.
     *
     * @param sql the SQL statement to prepare
     * @return the PreparedStatement ready for parameter binding and execution
     * @throws SQLException if a database access error occurs
     */
    PreparedStatement executePreparedQuery(String sql) throws SQLException;

    /**
     * Prepares and returns a PreparedStatement for an INSERT operation.
     *
     * @param sql the INSERT SQL statement
     * @return the PreparedStatement ready for parameter binding and execution
     * @throws SQLException if a database access error occurs
     */
    PreparedStatement prepareInsert(String sql) throws SQLException;

    /**
     * Prepares and returns a PreparedStatement for an UPDATE operation.
     *
     * @param sql the UPDATE SQL statement
     * @return the PreparedStatement ready for parameter binding and execution
     * @throws SQLException if a database access error occurs
     */
    PreparedStatement prepareUpdate(String sql) throws SQLException;

    /**
     * Closes any open JDBC resources and the database connection.
     *
     * @throws SQLException if a database access error occurs during resource cleanup
     */
    void disconnect() throws SQLException;
}
