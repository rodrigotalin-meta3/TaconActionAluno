filetype java
package br.com.meta3.java.scaffold.infrastructure.legacy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manager for obtaining Connections from the legacy database DataSource.
 * Migrates legacy conectarBanco() logic by delegating to the Spring-configured DataSource.
 */
@Component
public class LegacyDatabaseConnectionManager {

    private final DataSource legacyDataSource;

    public LegacyDatabaseConnectionManager(
        @Qualifier("legacyDataSource") DataSource legacyDataSource
    ) {
        this.legacyDataSource = legacyDataSource;
    }

    /**
     * Obtain a Connection to the legacy database.
     * @return a new Connection from the configured legacy DataSource
     * @throws DataAccessResourceFailureException if obtaining the Connection fails
     */
    public Connection getConnection() {
        try {
            // TODO: (REVIEW) In legacy code, conectarBanco also created a Statement.
            // Modern callers should create Statements or use JDBC/Spring templates as needed.
            return legacyDataSource.getConnection();
        } catch (SQLException ex) {
            // Wrap SQLException in Spring's DataAccessException hierarchy
            throw new DataAccessResourceFailureException(
                "Unable to obtain connection from legacy DataSource", ex
            );
        }
    }
}