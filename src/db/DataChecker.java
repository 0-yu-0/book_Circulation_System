package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据检查工具：检查各个表中的数据量
 */
public class DataChecker {

    public static void checkData() throws SQLException {
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            System.out.println("Connected to: " + conn.getMetaData().getURL());

            System.out.println("\n-- Checking data counts --");
            
            // 检查图书表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM bookinformation")) {
                if (rs.next()) {
                    System.out.println("Books count: " + rs.getInt("count"));
                }
            }

            // 检查读者表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM readerinformation")) {
                if (rs.next()) {
                    System.out.println("Readers count: " + rs.getInt("count"));
                }
            }

            // 检查借阅表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM borrowtable")) {
                if (rs.next()) {
                    System.out.println("Borrow records count: " + rs.getInt("count"));
                }
            }

            // 检查归还表数据量
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) AS count FROM returntable")) {
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