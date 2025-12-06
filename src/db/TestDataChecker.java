package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 测试数据检查工具：检查LibraryDB数据库中的数据量
 */
public class TestDataChecker {

    public static void checkData() throws SQLException {
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            // 切换到LibraryDB数据库
            st.execute("USE LibraryDB");

            System.out.println("Connected to: " + conn.getMetaData().getURL());

            System.out.println("\n-- Checking data counts in LibraryDB --");
            
            // 检查图书表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM bookInformation")) {
                if (rs.next()) {
                    System.out.println("Books count: " + rs.getInt("count"));
                }
            }

            // 检查读者表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM readerInformation")) {
                if (rs.next()) {
                    System.out.println("Readers count: " + rs.getInt("count"));
                }
            }

            // 检查借阅表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM borrowTable")) {
                if (rs.next()) {
                    System.out.println("Borrow records count: " + rs.getInt("count"));
                }
            }

            // 检查归还表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM returnTable")) {
                if (rs.next()) {
                    System.out.println("Return records count: " + rs.getInt("count"));
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            checkData();
        } catch (SQLException e) {
            System.err.println("Data check failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}