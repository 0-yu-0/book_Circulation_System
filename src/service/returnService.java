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
    public static long createReturn(long borrowId, LocalDate returnDate) throws SQLException {
        String selectBorrow = "SELECT borrowId, bookId, readerId, dueDate, borrowStatus FROM borrowTable WHERE borrowId = ? FOR UPDATE";
        String insertReturn = "INSERT INTO returnTable (borrowId, returnDate, overDays, fine) VALUES (?,?,?,?)";
        String updateBorrow = "UPDATE borrowTable SET borrowStatus = ? WHERE borrowId = ?";
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
                    ps.setLong(1, borrowId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Borrow record not found: " + borrowId);
                        status = rs.getInt("borrowStatus");
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

                long returnId;
                try (PreparedStatement ir = conn.prepareStatement(insertReturn, Statement.RETURN_GENERATED_KEYS)) {
                    ir.setLong(1, borrowId);
                    ir.setDate(2, java.sql.Date.valueOf(returnDate));
                    ir.setLong(3, overDays);
                    ir.setDouble(4, fine);
                    int r = ir.executeUpdate();
                    if (r != 1) throw new SQLException("Insert return failed");
                    try (ResultSet gk = ir.getGeneratedKeys()) {
                        if (gk.next()) returnId = gk.getLong(1);
                        else throw new SQLException("Failed to obtain generated returnId");
                    }
                }

                try (PreparedStatement ub = conn.prepareStatement(updateBorrow)) {
                    ub.setInt(1, 1); // 1 表示已归还
                    ub.setLong(2, borrowId);
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

    public static returnTable getReturnById(long returnId) throws SQLException {
        String sql = "SELECT * FROM returnTable WHERE returnId = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, returnId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToReturn(rs);
            }
        }
        return null;
    }

    private static returnTable mapRowToReturn(ResultSet rs) throws SQLException {
        returnTable r = new returnTable();
        r.setReturnId(String.valueOf(rs.getLong("returnId")));
        r.setBorrowId(String.valueOf(rs.getLong("borrowId")));
        java.sql.Date rd = rs.getDate("returnDate"); if (rd != null) r.setReturnDate(rd.toLocalDate());
        r.setOverDays(rs.getInt("overDays"));
        r.setFine(rs.getDouble("fine"));
        return r;
    }

}
