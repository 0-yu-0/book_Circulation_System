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
        String sql = "INSERT INTO readerInformation (readerId, readerName, readerCardType, readerCardNumber, readerPhoneNumber, registerDate, readerStatus, totalBoorowNumber, nowBorrowNumber) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getReaderId());
            ps.setString(2, r.getReaderName());
            ps.setString(3, r.getReaderCardType());
            ps.setString(4, r.getReaderCardNumber());
            ps.setString(5, r.getReaderPhoneNumber());
            LocalDate d = r.getRegisterDate();
            if (d != null) ps.setDate(6, java.sql.Date.valueOf(d)); else ps.setDate(6, null);
            ps.setInt(7, r.getReaderStatus());
            ps.setInt(8, r.getTotalBorrowNumber());
            ps.setInt(9, r.getNowBorrowNumber());
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updateReader(readerInformation r) throws SQLException {
        String sql = "UPDATE readerInformation SET readerName=?, readerCardType=?, readerCardNumber=?, readerPhoneNumber=?, registerDate=?, readerStatus=?, totalBoorowNumber=?, nowBorrowNumber=? WHERE readerId=?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getReaderName());
            ps.setString(2, r.getReaderCardType());
            ps.setString(3, r.getReaderCardNumber());
            ps.setString(4, r.getReaderPhoneNumber());
            LocalDate d = r.getRegisterDate();
            if (d != null) ps.setDate(5, java.sql.Date.valueOf(d)); else ps.setDate(5, null);
            ps.setInt(6, r.getReaderStatus());
            ps.setInt(7, r.getTotalBorrowNumber());
            ps.setInt(8, r.getNowBorrowNumber());
            ps.setString(9, r.getReaderId());
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean deleteReader(String readerId) throws SQLException {
        String sql = "DELETE FROM readerInformation WHERE readerId = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, readerId);
            return ps.executeUpdate() == 1;
        }
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
        r.setTotalBorrowNumber(rs.getInt("totalBoorowNumber"));
        r.setNowBorrowNumber(rs.getInt("nowBorrowNumber"));
        return r;
    }

}
