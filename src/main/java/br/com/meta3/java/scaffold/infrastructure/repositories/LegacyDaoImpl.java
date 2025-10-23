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
     */
    @Override
    public void connect(String dbType) throws SQLException {
        // Close existing connection if still open
        if (this.conn != null && !this.conn.isClosed()) {
            try {
                this.conn.close();
            } catch (SQLException ex) {
                LOGGER.warn("Error closing existing legacy connection", ex);
            }
        }

        // Handle null or empty dbType: fallback to Spring-managed DataSource (H2)
        if (dbType == null || dbType.trim().isEmpty()) {
            LOGGER.info("dbType is null/empty, defaulting to Spring-managed DataSource (H2)");
            try {
                this.conn = dataSource.getConnection();
            } catch (SQLException ex) {
                LOGGER.error("Error connecting to default DataSource due to null/empty dbType", ex);
                throw ex;
            }
            return;
        }

        // Oracle legacy branch
        if ("oracle".equalsIgnoreCase(dbType)) {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                LOGGER.warn("Oracle JDBC Driver not found in classpath", e);
            }
            OracleDataSource ods = new OracleDataSource();
            ods.setURL(urlOracle);
            ods.setUser(userOracle);
            ods.setPassword(passwordOracle);
            try {
                this.conn = ods.getConnection();
            } catch (SQLException ex) {
                LOGGER.error("Erro ao conectar no Oracle usando LegacyDaoImpl", ex);
                throw ex;
            }

        // SQL Server legacy branch
        } else if ("sqlserver".equalsIgnoreCase(dbType)) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            } catch (ClassNotFoundException e) {
                LOGGER.error("SQLServer JDBC Driver class not found", e);
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
                throw ex;
            }

        // Unsupported dbType: fallback to Spring-managed DataSource (H2)
        } else {
            LOGGER.warn("Unsupported dbType '{}', falling back to Spring-managed DataSource (H2)", dbType);
            try {
                this.conn = dataSource.getConnection();
            } catch (SQLException ex) {
                LOGGER.error("Error connecting to default DataSource due to unsupported dbType '{}'", dbType, ex);
                throw ex;
            }
        }
    }

    @Override
    public ResultSet executeQuery(String sql) {
        try {
            if (this.conn == null || this.conn.isClosed()) {
                connect(null);
            }
            this.stmt = this.conn.createStatement();
            this.rs = this.stmt.executeQuery(sql);
            return this.rs;
        } catch (SQLException ex) {
            LOGGER.error("Erro ao executar query no banco de dados: {}", sql, ex);
            throw new UnsupportedOperationException("Erro ao realizar transação com o banco de dados!", ex);
        }
    }

    @Override
    public PreparedStatement executePreparedQuery(String sql) {
        try {
            if (this.conn == null || this.conn.isClosed()) {
                connect(null);
            }
            this.pstmt = this.conn.prepareStatement(sql);
            return this.pstmt;
        } catch (SQLException ex) {
            LOGGER.error("Erro ao preparar a query: {}", sql, ex);
            throw new UnsupportedOperationException("Erro ao preparar a query!\n\n" + ex.getMessage(), ex);
        }
    }

    @Override
    public PreparedStatement prepareInsert(String sql) {
        try {
            if (this.conn == null || this.conn.isClosed()) {
                connect(null);
            }
            this.pstmt = this.conn.prepareStatement(sql);
            return this.pstmt;
        } catch (SQLException ex) {
            LOGGER.error("Erro ao preparar o insert: {}", sql, ex);
            throw new UnsupportedOperationException("Erro ao preparar o insert!\n\n" + ex.getMessage(), ex);
        }
    }

    @Override
    public PreparedStatement prepareUpdate(String sql) throws SQLException {
        // Reuse insert prep for updates
        return prepareInsert(sql);
    }

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
            throw new UnsupportedOperationException("Erro ao desconectar do Banco!\n\n" + ex.getMessage(), ex);
        }
    }

    /**
     * Exposes JdbcTemplate for advanced legacy query needs.
     */
    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }
}
