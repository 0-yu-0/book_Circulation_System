package service;

import db.db;
import entity.readerInformation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 读者相关服务（JDBC 简单实现）
 */
public class readerService {

    public static List<readerInformation> listReaders(int offset, int limit, String search) throws SQLException {
        List<readerInformation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM readerInformation WHERE 1=1");
        if (search != null && !search.isBlank()) sql.append(" AND (readerName LIKE ? OR readerCardNumber LIKE ?)");
        sql.append(" LIMIT ? OFFSET ?");

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (search != null && !search.isBlank()) {
                String s = "%" + search + "%";
                ps.setString(idx++, s);
                ps.setString(idx++, s);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToReader(rs));
            }
        }
        return list;
    }

    public static readerInformation getReaderById(String readerId) throws SQLException {
        String sql = "SELECT * FROM readerInformation WHERE readerId = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToReader(rs);
            }
        }
        return null;
    }

    public static readerInformation getReaderByCardNumber(String cardNumber) throws SQLException {
        String sql = "SELECT * FROM readerInformation WHERE readerCardNumber = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cardNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToReader(rs);
            }
        }
        return null;
    }

    public static boolean createReader(readerInformation r) throws SQLException {
        // Validate required fields
        if (r.getReaderName() == null || r.getReaderName().trim().isEmpty()) {
            throw new SQLException("readerName cannot be empty");
        }
        
        String sql = "INSERT INTO readerInformation (readerId, readerName, readerCardType, readerCardNumber, readerPhoneNumber, registerDate, readerStatus, totalBorrowNumber, nowBorrowNumber) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            // Generate readerId if not provided
            String readerId = r.getReaderId();
            if (readerId == null || readerId.trim().isEmpty()) {
                readerId = generateReaderId(r.getRegisterDate() != null ? r.getRegisterDate() : java.time.LocalDate.now());
                r.setReaderId(readerId);
            }
            
            ps.setString(1, readerId);
            ps.setString(2, r.getReaderName());
            ps.setString(3, r.getReaderCardType());
            ps.setString(4, r.getReaderCardNumber());
            ps.setString(5, r.getReaderPhoneNumber());
            LocalDate d = r.getRegisterDate();
            if (d != null) ps.setDate(6, java.sql.Date.valueOf(d)); else ps.setDate(6, null);
            ps.setInt(7, r.getReaderStatus());
            ps.setInt(8, r.getMaxBorrowNumber()); // 修改为正确的getter方法
            ps.setInt(9, r.getNowBorrowNumber());
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updateReader(readerInformation r) throws SQLException {
        String sql = "UPDATE readerInformation SET readerName=?, readerCardType=?, readerCardNumber=?, readerPhoneNumber=?, registerDate=?, readerStatus=?, totalBorrowNumber=?, nowBorrowNumber=? WHERE readerId=?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getReaderName());
            ps.setString(2, r.getReaderCardType());
            ps.setString(3, r.getReaderCardNumber());
            ps.setString(4, r.getReaderPhoneNumber());
            LocalDate d = r.getRegisterDate();
            if (d != null) ps.setDate(5, java.sql.Date.valueOf(d)); else ps.setDate(5, null);
            ps.setInt(6, r.getReaderStatus());
            ps.setInt(7, r.getMaxBorrowNumber()); // 修改为正确的getter方法
            ps.setInt(8, r.getNowBorrowNumber());
            ps.setString(9, r.getReaderId());
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean deleteReader(String readerId) throws SQLException {
        // First check if reader has any borrowing records
        String checkSql = "SELECT COUNT(*) FROM borrowTable WHERE readerId = ?";
        try (Connection c = db.getConnection(); PreparedStatement checkPs = c.prepareStatement(checkSql)) {
            checkPs.setString(1, readerId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Reader has borrowing records, cannot delete
                    throw new SQLException("Cannot delete reader with borrowing records");
                }
            }
        }
        
        // If no borrowing records, proceed with deletion
        String sql = "DELETE FROM readerInformation WHERE readerId = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, readerId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Return total matching readers for pagination
     */
    public static int countReaders(String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM readerInformation WHERE 1=1");
        if (search != null && !search.isBlank()) sql.append(" AND (readerName LIKE ? OR readerCardNumber LIKE ?)");
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (search != null && !search.isBlank()) {
                String s = "%" + search + "%";
                ps.setString(idx++, s);
                ps.setString(idx++, s);
            }
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        return 0;
    }

    private static readerInformation mapRowToReader(ResultSet rs) throws SQLException {
        readerInformation r = new readerInformation();
        r.setReaderId(rs.getString("readerId"));
        r.setReaderName(rs.getString("readerName"));
        r.setReaderCardType(rs.getString("readerCardType"));
        r.setReaderCardNumber(rs.getString("readerCardNumber"));
        r.setReaderPhoneNumber(rs.getString("readerPhoneNumber"));
        java.sql.Date rd = rs.getDate("registerDate");
        if (rd != null) r.setRegisterDate(rd.toLocalDate());
        r.setReaderStatus(rs.getInt("readerStatus"));
        // 修改字段名称，使其与数据库设计文档一致
        r.setMaxBorrowNumber(rs.getInt("totalBorrowNumber"));
        r.setNowBorrowNumber(rs.getInt("nowBorrowNumber"));
        return r;
    }

    /**
     * Generate readerId in format: 1001, 1002, 1003... (simple incrementing numbers)
     */
    private static String generateReaderId(LocalDate registerDate) throws SQLException {
        // Get the maximum readerId from the database
        String maxIdSql = "SELECT MAX(readerId) FROM readerInformation";
        try (Connection conn = db.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(maxIdSql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maxId = rs.getString(1);
                    if (maxId != null) {
                        // Parse the current maximum ID and increment by 1
                        try {
                            int currentMax = Integer.parseInt(maxId);
                            return String.valueOf(currentMax + 1);
                        } catch (NumberFormatException e) {
                            // If parsing fails, start from 1001
                            return "1001";
                        }
                    }
                }
            }
        }
        // Default starting point if no records exist
        return "1001";
    }

}