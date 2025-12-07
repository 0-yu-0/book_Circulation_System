package service;

import db.db;
import entity.returnTable;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
 

/**
 * 归还服务：处理归还记录创建并更新相关表（事务示例）
 */
public class returnService {

    /**
     * 为给定 borrowId 创建归还记录，并更新 borrowTable、bookInformation、readerInformation。
     * 计罚规则：每超期一天罚款 1（单位与表设计中的 int/double 一致请按需修改）。
     * 返回新创建的 returnId（数据库自增）。
     */
    public static long createReturn(String borrowId, LocalDate returnDate) throws SQLException {
        String selectBorrow = "SELECT borrowId, bookId, readerId, dueDate, borrowStates FROM borrowTable WHERE borrowId = ? FOR UPDATE";
        String insertReturn = "INSERT INTO returnTable (returnId, borrowId, returnDate, overDays, fine) VALUES (?,?,?,?,?)";
        String updateBorrow = "UPDATE borrowTable SET borrowStates = ? WHERE borrowId = ?";
        String updateBook = "UPDATE bookInformation SET bookAvailableCopies = bookAvailableCopies + 1 WHERE bookId = ?";
        String updateReader = "UPDATE readerInformation SET nowBorrowNumber = GREATEST(0, nowBorrowNumber - 1) WHERE readerId = ?";

        try (Connection conn = db.getConnection()) {
            try {
                conn.setAutoCommit(false);

                String bookId;
                String readerId;
                LocalDate dueDate;
                int status;

                try (PreparedStatement ps = conn.prepareStatement(selectBorrow)) {
                    ps.setString(1, borrowId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Borrow record not found: " + borrowId);
                        status = rs.getInt("borrowStates");
                        bookId = rs.getString("bookId");
                        readerId = rs.getString("readerId");
                        java.sql.Date dd = rs.getDate("dueDate");
                        if (dd != null) dueDate = dd.toLocalDate(); else dueDate = null;
                    }
                }

                if (status != 0) throw new SQLException("Borrow record is not in borrowed state: " + status);

                long overDays = 0;
                if (dueDate != null && returnDate != null) {
                    overDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                    if (overDays < 0) overDays = 0;
                }
                double fine = overDays * 1.0; // 1 unit per day

                // Generate returnId in format: RT + yyyyMMdd + sequence number
                String returnIdStr = generateReturnId(returnDate);
                long returnId;
                try (PreparedStatement ir = conn.prepareStatement(insertReturn)) {
                    ir.setString(1, returnIdStr);
                    ir.setString(2, borrowId);
                    ir.setDate(3, java.sql.Date.valueOf(returnDate));
                    ir.setLong(4, overDays);
                    ir.setDouble(5, fine);
                    int r = ir.executeUpdate();
                    if (r != 1) throw new SQLException("Insert return failed");
                    returnId = Long.parseLong(returnIdStr.substring(10)); // Extract sequence number
                }

                try (PreparedStatement ub = conn.prepareStatement(updateBorrow)) {
                    ub.setInt(1, 1); // 1 表示已归还
                    ub.setString(2, borrowId);
                    ub.executeUpdate();
                }

                try (PreparedStatement ubk = conn.prepareStatement(updateBook)) {
                    ubk.setString(1, bookId);
                    ubk.executeUpdate();
                }

                try (PreparedStatement ur = conn.prepareStatement(updateReader)) {
                    ur.setString(1, readerId);
                    ur.executeUpdate();
                }

                conn.commit();
                return returnId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static returnTable getReturnById(String returnId) throws SQLException {
        String sql = "SELECT * FROM returnTable WHERE returnId = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, returnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToReturn(rs);
            }
        }
        return null;
    }

    /**
     * Batch return processing: accepts an array of borrowIds and processes each within a single transaction.
     * Returns a simple summary array of created returnIds and fines per borrow.
     */
    public static java.util.List<java.util.Map<String, Object>> createReturnBatch(java.util.List<String> borrowIds, LocalDate returnDate) throws SQLException {
        try (Connection conn = db.getConnection()) {
            try {
                conn.setAutoCommit(false);
                java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();
                for (String borrowId : borrowIds) {
                    String selectBorrow = "SELECT borrowId, bookId, readerId, dueDate, borrowStates FROM borrowTable WHERE borrowId = ? FOR UPDATE";
                    String insertReturn = "INSERT INTO returnTable (returnId, borrowId, returnDate, overDays, fine) VALUES (?,?,?,?,?)";
                    String updateBorrow = "UPDATE borrowTable SET borrowStates = ? WHERE borrowId = ?";
                    String updateBook = "UPDATE bookInformation SET bookAvailableCopies = bookAvailableCopies + 1 WHERE bookId = ?";
                    String updateReader = "UPDATE readerInformation SET nowBorrowNumber = GREATEST(0, nowBorrowNumber - 1) WHERE readerId = ?";

                    String bookId;
                    String readerId;
                    LocalDate dueDate;
                    int status;

                    try (PreparedStatement ps = conn.prepareStatement(selectBorrow)) {
                        ps.setString(1, borrowId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Borrow record not found: " + borrowId);
                            status = rs.getInt("borrowStates");
                            bookId = rs.getString("bookId");
                            readerId = rs.getString("readerId");
                            java.sql.Date dd = rs.getDate("dueDate");
                            if (dd != null) dueDate = dd.toLocalDate(); else dueDate = null;
                        }
                    }

                    if (status != 0) throw new SQLException("Borrow record is not in borrowed state: " + status);

                    long overDays = 0;
                    if (dueDate != null && returnDate != null) {
                        overDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
                        if (overDays < 0) overDays = 0;
                    }
                    double fine = overDays * 1.0;

                    // Generate returnId in format: RT + yyyyMMdd + sequence number
                    String returnIdStr = generateReturnId(returnDate);
                    long returnId;
                    try (PreparedStatement ir = conn.prepareStatement(insertReturn)) {
                        ir.setString(1, returnIdStr);
                        ir.setString(2, borrowId);
                        ir.setDate(3, java.sql.Date.valueOf(returnDate));
                        ir.setLong(4, overDays);
                        ir.setDouble(5, fine);
                        int r = ir.executeUpdate();
                        if (r != 1) throw new SQLException("Insert return failed");
                        returnId = Long.parseLong(returnIdStr.substring(10)); // Extract sequence number
                    }

                    try (PreparedStatement ub = conn.prepareStatement(updateBorrow)) {
                        ub.setInt(1, 1);
                        ub.setString(2, borrowId);
                        ub.executeUpdate();
                    }

                    try (PreparedStatement ubk = conn.prepareStatement(updateBook)) {
                        ubk.setString(1, bookId);
                        ubk.executeUpdate();
                    }

                    try (PreparedStatement ur = conn.prepareStatement(updateReader)) {
                        ur.setString(1, readerId);
                        ur.executeUpdate();
                    }

                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("borrowId", borrowId);
                    item.put("returnId", returnId);
                    item.put("fine", fine);
                    results.add(item);
                }
                conn.commit();
                return results;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static returnTable mapRowToReturn(ResultSet rs) throws SQLException {
        returnTable r = new returnTable();
        r.setReturnId(rs.getString("returnId"));
        r.setBorrowId(rs.getString("borrowId"));
        java.sql.Date rd = rs.getDate("returnDate"); if (rd != null) r.setReturnDate(rd.toLocalDate());
        r.setOverDays(rs.getInt("overDays"));
        r.setFine(rs.getDouble("fine"));
        return r;
    }

    /**
     * Generate returnId in format: RT + yyyyMMdd + sequence number (3 digits)
     * Example: RT20231201001
     */
    private static String generateReturnId(LocalDate returnDate) throws SQLException {
        String datePart = returnDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RT" + datePart;
        
        // Count existing returnIds for the same date to determine sequence number
        String countSql = "SELECT COUNT(*) FROM returnTable WHERE returnId LIKE ?";
        try (Connection conn = db.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    // Format sequence number as 3 digits
                    String sequence = String.format("%03d", count + 1);
                    return prefix + sequence;
                }
            }
        }
        return prefix + "001"; // Default if count fails
    }

}
