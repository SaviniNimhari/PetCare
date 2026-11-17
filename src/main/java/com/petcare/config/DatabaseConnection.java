package com.petcare.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages database connectivity using HikariCP connection pooling
 * with automatic fallback to standard JDBC DriverManager.
 */
public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static String dbUrl = "jdbc:mysql://localhost:3306/petcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String dbUser = "root";
    private static String dbPassword = "";
    private static HikariDataSource dataSource;
    private static boolean poolInitialized = false;

    static {
        loadProperties();
        initDataSource();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                dbUrl = prop.getProperty("db.url", dbUrl);
                dbUser = prop.getProperty("db.user", dbUser);
                dbPassword = prop.getProperty("db.password", dbPassword);
            }
        } catch (Exception e) {
            logger.warn("Could not load application.properties, using defaults. Error: {}", e.getMessage());
        }
    }

    private static synchronized void initDataSource() {
        if (poolInitialized && dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(5000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            poolInitialized = true;
            logger.info("HikariCP connection pool initialized successfully.");
        } catch (Exception ex) {
            logger.warn("Could not initialize HikariCP pool (MySQL server may be offline): {}. Fallback to direct JDBC available.", ex.getMessage());
            poolInitialized = false;
        }
    }

    /**
     * Obtains a connection from the pool or via DriverManager if pool is unavailable.
     */
    public static Connection getConnection() throws SQLException {
        if (poolInitialized && dataSource != null && !dataSource.isClosed()) {
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                logger.warn("Pool getConnection failed, attempting direct connection fallback: {}", e.getMessage());
            }
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests whether database connection is active.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }

    public static void setCredentials(String url, String user, String password) {
        dbUrl = url;
        dbUser = user;
        dbPassword = password;
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        poolInitialized = false;
        initDataSource();
    }

    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
