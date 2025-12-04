package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 简单的数据库检查工具：列出表、视图及索引信息（通过 JDBC）
 */
public class DbInspector {

    public static void inspectSchema() throws SQLException {
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            System.out.println("Connected to: " + conn.getMetaData().getURL());

            System.out.println("\n-- Tables --");
            try (ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='bookSystem' AND TABLE_TYPE='BASE TABLE'")) {
                while (rs.next()) System.out.println(rs.getString(1));
            }

            System.out.println("\n-- Views --");
            try (ResultSet rs = st.executeQuery("SELECT TABLE_NAME FROM information_schema.VIEWS WHERE TABLE_SCHEMA='bookSystem'")) {
                while (rs.next()) System.out.println(rs.getString(1));
            }

            System.out.println("\n-- Indexes (information_schema.STATISTICS) --");
            try (ResultSet rs = st.executeQuery("SELECT TABLE_NAME, INDEX_NAME, NON_UNIQUE, SEQ_IN_INDEX, COLUMN_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='bookSystem' ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX")) {
                while (rs.next()) {
                    String t = rs.getString("TABLE_NAME");
                    String ix = rs.getString("INDEX_NAME");
                    int nonUnique = rs.getInt("NON_UNIQUE");
                    int seq = rs.getInt("SEQ_IN_INDEX");
                    String col = rs.getString("COLUMN_NAME");
                    System.out.printf("%s | %s | non_unique=%d | seq=%d | %s%n", t, ix, nonUnique, seq, col);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            inspectSchema();
        } catch (SQLException e) {
            System.err.println("Schema inspection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
