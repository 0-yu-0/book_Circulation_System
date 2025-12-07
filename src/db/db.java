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
    private static final String URL = "jdbc:mysql://localhost:3306/LibraryDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PWD = "123456";

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
     * 初始化数据库表。如果表不存在则创建它们。
     * 建议在应用启动时调用此方法一次。
     */
    public static void initDatabase() throws SQLException {
        String createBook = "CREATE TABLE IF NOT EXISTS bookInformation ("
            + "bookId VARCHAR(20) PRIMARY KEY,"
            + "isbn VARCHAR(13) NOT NULL,"
            + "bookName VARCHAR(255) NOT NULL,"
            + "bookAuthor VARCHAR(100) NOT NULL,"
            + "bookPublisher VARCHAR(100),"
            + "bookPubDate DATE,"
            + "bookCategory VARCHAR(50),"
            + "bookLocation VARCHAR(50) NOT NULL,"
            + "bookTotalCopies INT,"
            + "bookAvailableCopies INT,"
            + "bookPrice INT NOT NULL,"
            + "borrowCount BIGINT NOT NULL,"
            + "CONSTRAINT chk_bookTotalCopies CHECK(bookTotalCopies >= 1),"
            + "CONSTRAINT chk_bookAvailableCopies CHECK(bookAvailableCopies >= 0 AND bookAvailableCopies <= bookTotalCopies)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String createReader = "CREATE TABLE IF NOT EXISTS readerInformation ("
                + "readerId VARCHAR(20) PRIMARY KEY,"
                + "readerName VARCHAR(50) NOT NULL,"
                + "readerCardType VARCHAR(20) NOT NULL,"
                + "readerCardNumber VARCHAR(30) NOT NULL UNIQUE,"
                + "readerPhoneNumber VARCHAR(20),"
                + "registerDate DATE NOT NULL,"
                + "readerStatus INT,"
                + "totalBorrowNumber INT NOT NULL,"
                + "nowBorrowNumber INT NOT NULL"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String createBorrow = "CREATE TABLE IF NOT EXISTS borrowTable ("
                + "borrowId VARCHAR(20) PRIMARY KEY,"
                + "bookId VARCHAR(20),"
                + "readerId VARCHAR(20),"
                + "borrowDate DATE NOT NULL,"
                + "dueDate DATE NOT NULL,"
                + "borrowStates INT NOT NULL,"
                + "FOREIGN KEY (bookId) REFERENCES bookInformation(bookId),"
                + "FOREIGN KEY (readerId) REFERENCES readerInformation(readerId)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        String createReturn = "CREATE TABLE IF NOT EXISTS returnTable ("
                + "returnId VARCHAR(20) PRIMARY KEY,"
                + "borrowId VARCHAR(20) NOT NULL,"
                + "returnDate DATE NOT NULL,"
                + "overDays INT NOT NULL,"
                + "fine DOUBLE NOT NULL,"
                + "FOREIGN KEY (borrowId) REFERENCES borrowTable(borrowId)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // 先创建父表
            st.executeUpdate(createBook);
            st.executeUpdate(createReader);
            // 再创建依赖表
            st.executeUpdate(createBorrow);
            st.executeUpdate(createReturn);

            // 创建索引（若无权限或已存在会抛出异常，捕获并记录）
            String[] indexes = new String[] {
                // bookInformation
                "CREATE UNIQUE INDEX uk_isbn ON bookInformation (isbn)",
                "CREATE INDEX idx_bookName ON bookInformation (bookName)",
                "CREATE INDEX idx_bookAuthor ON bookInformation (bookAuthor)",
                "CREATE INDEX idx_bookCategory ON bookInformation (bookCategory)",
                "CREATE INDEX idx_availableCopies ON bookInformation (bookAvailableCopies)",
                // readerInformation
                "CREATE UNIQUE INDEX uk_cardNumber ON readerInformation (readerCardNumber)",
                "CREATE INDEX idx_readerName ON readerInformation (readerName)",
                "CREATE INDEX idx_readerStatus ON readerInformation (readerStatus)",
                // borrowTable
                "CREATE INDEX fk_bookId ON borrowTable (bookId)",
                "CREATE INDEX fk_readerId ON borrowTable (readerId)",
                "CREATE INDEX idx_borrowStates ON borrowTable (borrowStates)",
                "CREATE INDEX idx_dueDate ON borrowTable (dueDate)",
                "CREATE INDEX idx_reader_borrow ON borrowTable (readerId, borrowDate)",
                "CREATE INDEX idx_book_stats ON borrowTable (bookId, borrowDate)",
                // returnTable
                "CREATE INDEX fk_borrowId ON returnTable (borrowId)",
                "CREATE INDEX idx_returnDate ON returnTable (returnDate)"
            };

            for (String ix : indexes) {
                try {
                    st.executeUpdate(ix);
                } catch (SQLException ie) {
                    // 忽略索引创建失败（例如索引已存在或权限不足）
                    // 只在调试模式下打印详细信息
                    if (Boolean.parseBoolean(System.getProperty("debug", "false"))) {
                        System.err.println("Debug: create index skipped: " + ie.getMessage() + " SQL: " + ix);
                    }
                }
            }
        }
    }

    /**
     * 简单的连接测试（可做手工测试）。如果想自动测试，请在控制台运行 `java db.db`。
     */
    public static void main(String[] args) {
        try {
            initDatabase();
            try (Connection c = getConnection(); Statement s = c.createStatement()) {
                System.out.println("DB connected: " + c.getMetaData().getURL());
                try (ResultSet rs = s.executeQuery("SELECT 1")) {
                    if (rs.next()) System.out.println("Test query OK: " + rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("DB initialization/test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}