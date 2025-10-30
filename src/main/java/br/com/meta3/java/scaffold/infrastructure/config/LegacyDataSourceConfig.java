filetype java
package br.com.meta3.java.scaffold.infrastructure.config;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Configuration to instantiate a legacy DataSource (Oracle or SQL Server)
 * based on the property app.datasource.type.
 */
@Configuration
@EnableConfigurationProperties(LegacyDatabaseProperties.class)
public class LegacyDataSourceConfig {

    /**
     * The type of legacy database to connect to: "oracle" or "sqlserver".
     * Must be provided in application.properties under app.datasource.type.
     */
    @Value("${app.datasource.type}")
    private String tipoBanco;

    private final LegacyDatabaseProperties props;

    public LegacyDataSourceConfig(LegacyDatabaseProperties props) {
        this.props = props;
    }

    /**
     * Primary DataSource bean for legacy database access.
     * Chooses between OracleDataSource and SQLServerDataSource.
     *
     * @return configured DataSource
     */
    @Bean
    @Primary
    public DataSource legacyDataSource() {
        try {
            if ("oracle".equalsIgnoreCase(tipoBanco)) {
                OracleDataSource ds = new OracleDataSource();
                // TODO: (REVIEW) Ensure Oracle JDBC driver is on the classpath
                ds.setURL(props.getOracle().getUrl());
                ds.setUser(props.getOracle().getUsername());
                ds.setPassword(props.getOracle().getPassword());
                return ds;
            } else if ("sqlserver".equalsIgnoreCase(tipoBanco)) {
                SQLServerDataSource ds = new SQLServerDataSource();
                // TODO: (REVIEW) Ensure SQL Server JDBC driver is on the classpath
                ds.setUser(props.getSqlserver().getUser());
                ds.setPassword(props.getSqlserver().getPassword());
                ds.setServerName(props.getSqlserver().getServerName());
                ds.setPortNumber(props.getSqlserver().getPort());
                ds.setDatabaseName(props.getSqlserver().getDatabaseName());
                return ds;
            } else {
                throw new IllegalArgumentException("Invalid legacy datasource type: " + tipoBanco);
            }
        } catch (SQLException ex) {
            // Wrap checked exceptions in a BeanCreationException for Spring context
            throw new BeanCreationException("Failed to create legacy DataSource for type: " + tipoBanco, ex);
        }
    }
}