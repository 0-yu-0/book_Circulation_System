package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 简单的数据库连接助手类（JDBC）
 * 支持通过环境变量覆盖默认配置：DB_URL / DB_USER / DB_PWD
 */
public class db {
    // 默认配置（可被环境变量覆盖）
    private static final String URL = "jdbc:mysql://localhost:3306/bookSystem?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PWD = "123456zhou";

    /**
     * 获取数据库连接。
     * 优先使用环境变量 DB_URL、DB_USER、DB_PWD，如果未提供则使用默认值。
     * 注意：调用者负责在使用完 Connection 后关闭它（或使用 try-with-resources）。
     */
    public static Connection getConnection() throws SQLException {
        String url = System.getenv(URL);
        String user = System.getenv(USER);
        String pwd = System.getenv(PWD);

        if (url == null || url.isBlank()) url = URL;
        if (user == null || user.isBlank()) user = USER;
        if (pwd == null) pwd = PWD; // 密码允许为空字符串

        // 显式加载驱动（现代JDBC驱动器通常不需要，但显式加载能在某些环境避免问题）
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // 如果驱动缺失，抛出 SQLException 包装
            throw new SQLException("MySQL JDBC Driver not found on classpath.", e);
        }

        return DriverManager.getConnection(url, user, pwd);
    }

    /**
     * 关闭 Statement、ResultSet、Connection 的帮助方法（忽略异常，用于简化清理）
     */
    public static void closeQuietly(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) {}
        try {
            if (st != null) st.close();
        } catch (SQLException ignored) {}
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {}
    }

    /**
     * 简单的连接测试（可做手工测试）。如果想自动测试，请在控制台运行 `java db.db`。
     */
    public static void main(String[] args) {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            System.out.println("DB connected: " + c.getMetaData().getURL());
            try (ResultSet rs = s.executeQuery("SELECT 1")) {
                if (rs.next()) System.out.println("Test query OK: " + rs.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("DB connection test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
