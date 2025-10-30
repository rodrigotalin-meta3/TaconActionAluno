filetype java
package br.com.meta3.java.scaffold.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for legacy database connections.
 * Maps legacy Oracle and SQL Server settings under prefix 'app.datasource'.
 */
@Configuration
@ConfigurationProperties(prefix = "app.datasource")
public class LegacyDatabaseProperties {

    /**
     * Properties for connecting to legacy Oracle database.
     * TODO: (REVIEW) Ensure application.properties updated to use app.datasource.oracle.* instead of legacy.oracle.*
     */
    private final Oracle oracle = new Oracle();

    /**
     * Properties for connecting to legacy SQL Server database.
     * TODO: (REVIEW) Ensure application.properties updated to use app.datasource.sqlserver.* instead of legacy.sqlserver.*
     */
    private final SqlServer sqlserver = new SqlServer();

    public Oracle getOracle() {
        return oracle;
    }

    public SqlServer getSqlserver() {
        return sqlserver;
    }

    // Nested class for Oracle properties
    public static class Oracle {
        private String url;
        private String username;
        private String password;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Nested class for SQL Server properties
    public static class SqlServer {
        private String user;
        private String password;
        private String serverName;
        private Integer port;
        private String databaseName;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }
    }
}