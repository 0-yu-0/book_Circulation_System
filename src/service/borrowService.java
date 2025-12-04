package service;

import db.db;
import entity.borrowTable;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 借阅服务：包含创建借阅记录的事务示例，以及基本查询接口
 */
public class borrowService {

    /**
     * 为单本图书创建借阅记录（事务）：
     * - 检查读者状态
     * - 检查库存
     * - 插入 borrowTable，更新 bookInformation.available 和 borrowCount
     * - 更新 readerInformation.nowBorrowNumber 和 totalBorrowNumber
     * 返回生成的 borrowId（数据库自增），若失败抛出 SQLException
     */
    public static long createBorrowSingle(String bookId, String readerId, LocalDate borrowDate, LocalDate dueDate) throws SQLException {
        String selectReader = "SELECT readerStatus, nowBorrowNumber, totalBoorowNumber FROM readerInformation WHERE readerId = ? FOR UPDATE";
        String selectBook = "SELECT bookAvailableCopies, borrowCount FROM bookInformation WHERE bookId = ? FOR UPDATE";
        String insertBorrow = "INSERT INTO borrowTable (bookId, readerId, borrowDate, dueDate, borrowStatus) VALUES (?,?,?,?,?)";
        String updateBook = "UPDATE bookInformation SET bookAvailableCopies = bookAvailableCopies - 1, borrowCount = borrowCount + 1 WHERE bookId = ?";
        String updateReader = "UPDATE readerInformation SET nowBorrowNumber = nowBorrowNumber + 1, totalBoorowNumber = totalBoorowNumber + 1 WHERE readerId = ?";

        try (Connection conn = db.getConnection()) {
            try {
                conn.setAutoCommit(false);

                // check reader
                try (PreparedStatement prs = conn.prepareStatement(selectReader)) {
                    prs.setString(1, readerId);
                    try (ResultSet rs = prs.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Reader not found: " + readerId);
                        int status = rs.getInt("readerStatus");
                        if (status != 0) throw new SQLException("Reader status invalid: " + status);
                    }
                }

                // check book availability
                try (PreparedStatement psb = conn.prepareStatement(selectBook)) {
                    psb.setString(1, bookId);
                    try (ResultSet rs = psb.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Book not found: " + bookId);
                        int available = rs.getInt("bookAvailableCopies");
                        if (available < 1) throw new SQLException("No available copies for book: " + bookId);
                    }
                }

                // insert borrow
                long borrowId;
                try (PreparedStatement pib = conn.prepareStatement(insertBorrow, Statement.RETURN_GENERATED_KEYS)) {
                    pib.setString(1, bookId);
                    pib.setString(2, readerId);
                    pib.setDate(3, java.sql.Date.valueOf(borrowDate));
                    pib.setDate(4, java.sql.Date.valueOf(dueDate));
                    pib.setInt(5, 0); // 0 表示在借
                    int rows = pib.executeUpdate();
                    if (rows != 1) throw new SQLException("Insert borrow failed");
                    try (ResultSet gk = pib.getGeneratedKeys()) {
                        if (gk.next()) borrowId = gk.getLong(1);
                        else throw new SQLException("Failed to obtain generated borrowId");
                    }
                }

                // update book
                try (PreparedStatement ub = conn.prepareStatement(updateBook)) {
                    ub.setString(1, bookId);
                    ub.executeUpdate();
                }

                // update reader
                try (PreparedStatement ur = conn.prepareStatement(updateReader)) {
                    ur.setString(1, readerId);
                    ur.executeUpdate();
                }

                conn.commit();
                return borrowId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public static borrowTable getBorrowById(long borrowId) throws SQLException {
        String sql = "SELECT * FROM borrowTable WHERE borrowId = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, borrowId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToBorrow(rs);
            }
        }
        return null;
    }

    /**
     * 获取逾期未还列表（dueDate < today 且 borrowStatus = 0）
     */
    public static List<borrowTable> getOverdueList(int offset, int limit) throws SQLException {
        List<borrowTable> list = new ArrayList<>();
        String sql = "SELECT * FROM borrowTable WHERE borrowStatus = 0 AND dueDate < CURRENT_DATE() ORDER BY dueDate ASC LIMIT ? OFFSET ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToBorrow(rs));
            }
        }
        return list;
    }

    public static List<borrowTable> listBorrows(String readerId, Integer status, int offset, int limit) throws SQLException {
        List<borrowTable> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM borrowTable WHERE 1=1");
        if (readerId != null && !readerId.isBlank()) sql.append(" AND readerId = '").append(readerId).append("'");
        if (status != null) sql.append(" AND borrowStatus = ").append(status);
        sql.append(" ORDER BY borrowDate DESC LIMIT ? OFFSET ?");

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToBorrow(rs));
            }
        }
        return list;
    }

    private static borrowTable mapRowToBorrow(ResultSet rs) throws SQLException {
        borrowTable b = new borrowTable();
        b.setBorrowId(String.valueOf(rs.getLong("borrowId")));
        b.setBookId(rs.getString("bookId"));
        b.setReaderId(rs.getString("readerId"));
        java.sql.Date bd = rs.getDate("borrowDate"); if (bd != null) b.setBorrowDate(bd.toLocalDate());
        java.sql.Date dd = rs.getDate("dueDate"); if (dd != null) b.setDueDate(dd.toLocalDate());
        b.setBorrowStates(rs.getInt("borrowStatus"));
        return b;
    }

}
