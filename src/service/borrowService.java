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
        String selectReader = "SELECT readerStatus, nowBorrowNumber, totalBorrowNumber FROM readerInformation WHERE readerId = ? FOR UPDATE";
        String selectBook = "SELECT bookAvailableCopies, borrowCount FROM bookInformation WHERE bookId = ? FOR UPDATE";
        String insertBorrow = "INSERT INTO borrowTable (borrowId, bookId, readerId, borrowDate, dueDate, borrowStates) VALUES (?,?,?,?,?,?)";
        String updateBook = "UPDATE bookInformation SET bookAvailableCopies = bookAvailableCopies - 1, borrowCount = borrowCount + 1 WHERE bookId = ?";
        String updateReader = "UPDATE readerInformation SET nowBorrowNumber = nowBorrowNumber + 1, totalBorrowNumber = totalBorrowNumber + 1 WHERE readerId = ?";

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

                // Generate borrowId in format: yyyy + sequence number (4 digits)
                String borrowIdStr = generateBorrowId(borrowDate);
                long borrowId;
                try (PreparedStatement pib = conn.prepareStatement(insertBorrow)) {
                    pib.setString(1, borrowIdStr);
                    pib.setString(2, bookId);
                    pib.setString(3, readerId);
                    pib.setDate(4, java.sql.Date.valueOf(borrowDate));
                    pib.setDate(5, java.sql.Date.valueOf(dueDate));
                    pib.setInt(6, 0); // 0 表示在借
                    int rows = pib.executeUpdate();
                    if (rows != 1) throw new SQLException("Insert borrow failed");
                    borrowId = Long.parseLong(borrowIdStr); // Convert to long for return value
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
     * 获取逾期未还列表（dueDate < today 且 borrowStates = 0）
     */
    public static List<borrowTable> getOverdueList(String readerName, String bookTitle, int offset, int limit) throws SQLException {
        List<borrowTable> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT bt.*, bi.bookName as bookTitle, ri.readerName, bi.bookCategory as category FROM borrowTable bt " +
                     "LEFT JOIN bookInformation bi ON bt.bookId = bi.bookId " +
                     "LEFT JOIN readerInformation ri ON bt.readerId = ri.readerId " +
                     "WHERE bt.borrowStates = 0 AND bt.dueDate < CURRENT_DATE()");
        
        if (readerName != null && !readerName.isBlank()) sql.append(" AND ri.readerName LIKE ?");
        if (bookTitle != null && !bookTitle.isBlank()) sql.append(" AND bi.bookName LIKE ?");
        
        sql.append(" ORDER BY bt.dueDate ASC LIMIT ? OFFSET ?");
        
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (readerName != null && !readerName.isBlank()) {
                ps.setString(idx++, "%" + readerName + "%");
            }
            if (bookTitle != null && !bookTitle.isBlank()) {
                ps.setString(idx++, "%" + bookTitle + "%");
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToBorrowWithDetails(rs));
            }
        }
        return list;
    }

    /**
     * 统计逾期未还记录总数（用于分页）
     */
    public static int countOverdue(String readerName, String bookTitle) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM borrowTable bt " +
                     "LEFT JOIN bookInformation bi ON bt.bookId = bi.bookId " +
                     "LEFT JOIN readerInformation ri ON bt.readerId = ri.readerId " +
                     "WHERE bt.borrowStates = 0 AND bt.dueDate < CURRENT_DATE()");
        
        if (readerName != null && !readerName.isBlank()) sql.append(" AND ri.readerName LIKE ?");
        if (bookTitle != null && !bookTitle.isBlank()) sql.append(" AND bi.bookName LIKE ?");
        
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (readerName != null && !readerName.isBlank()) {
                ps.setString(idx++, "%" + readerName + "%");
            }
            if (bookTitle != null && !bookTitle.isBlank()) {
                ps.setString(idx++, "%" + bookTitle + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public static List<borrowTable> listBorrows(String readerId, Integer status, String bookTitle, int offset, int limit) throws SQLException {
        List<borrowTable> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT bt.*, bi.bookName as bookTitle, ri.readerName FROM borrowTable bt LEFT JOIN bookInformation bi ON bt.bookId = bi.bookId LEFT JOIN readerInformation ri ON bt.readerId = ri.readerId WHERE 1=1");
        if (readerId != null && !readerId.isBlank()) sql.append(" AND bt.readerId = ?");
        if (status != null) sql.append(" AND bt.borrowStates = ?");
        if (bookTitle != null && !bookTitle.isBlank()) sql.append(" AND bi.bookName LIKE ?");
        sql.append(" ORDER BY bt.borrowDate DESC LIMIT ? OFFSET ?");

        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (readerId != null && !readerId.isBlank()) {
                ps.setString(idx++, readerId);
            }
            if (status != null) {
                ps.setInt(idx++, status);
            }
            if (bookTitle != null && !bookTitle.isBlank()) {
                ps.setString(idx++, "%" + bookTitle + "%");
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToBorrowWithDetails(rs));
            }
        }
        return list;
    }

    /**
     * Count total borrows matching filters for pagination
     */
    public static int countBorrows(String readerId, Integer status, String bookTitle) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM borrowTable bt LEFT JOIN bookInformation bi ON bt.bookId = bi.bookId WHERE 1=1");
        if (readerId != null && !readerId.isBlank()) sql.append(" AND bt.readerId = ?");
        if (status != null) sql.append(" AND bt.borrowStates = ?");
        if (bookTitle != null && !bookTitle.isBlank()) sql.append(" AND bi.bookName LIKE ?");
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (readerId != null && !readerId.isBlank()) {
                ps.setString(idx++, readerId);
            }
            if (status != null) {
                ps.setInt(idx++, status);
            }
            if (bookTitle != null && !bookTitle.isBlank()) {
                ps.setString(idx++, "%" + bookTitle + "%");
            }
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        }
        return 0;
    }

    /**
     * Batch create borrow records given a list of bookIds. This implementation loops and calls createBorrowSingle
     * inside a transaction to provide atomic behavior. It returns an array of created borrowIds or throws SQLException on failure.
     */
    public static List<Long> createBorrowBatch(List<String> bookIds, String readerId, LocalDate borrowDate, LocalDate dueDate) throws SQLException {
        try (Connection conn = db.getConnection()) {
            try {
                conn.setAutoCommit(false);
                List<Long> created = new ArrayList<>();
                for (String bookId : bookIds) {
                    // reuse createBorrowSingle logic but we need a version that accepts a connection or replicate logic here
                    // For simplicity, call existing single method which opens its own connection — to keep this patch small we will instead
                    // perform per-book create by calling SQL here using the same locks as createBorrowSingle

                    String selectReader = "SELECT readerStatus, nowBorrowNumber, totalBorrowNumber FROM readerInformation WHERE readerId = ? FOR UPDATE";
                    String selectBook = "SELECT bookAvailableCopies, borrowCount FROM bookInformation WHERE bookId = ? FOR UPDATE";
                    String insertBorrow = "INSERT INTO borrowTable (borrowId, bookId, readerId, borrowDate, dueDate, borrowStates) VALUES (?,?,?,?,?,?)";
                    String updateBook = "UPDATE bookInformation SET bookAvailableCopies = bookAvailableCopies - 1, borrowCount = borrowCount + 1 WHERE bookId = ?";
                    String updateReader = "UPDATE readerInformation SET nowBorrowNumber = nowBorrowNumber + 1, totalBorrowNumber = totalBorrowNumber + 1 WHERE readerId = ?";

                    // check reader once per iteration (locks will be repeated but acceptable for now)
                    try (PreparedStatement prs = conn.prepareStatement(selectReader)) {
                        prs.setString(1, readerId);
                        try (ResultSet rs = prs.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Reader not found: " + readerId);
                            int status = rs.getInt("readerStatus");
                            if (status != 0) throw new SQLException("Reader status invalid: " + status);
                        }
                    }

                    try (PreparedStatement psb = conn.prepareStatement(selectBook)) {
                        psb.setString(1, bookId);
                        try (ResultSet rs = psb.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Book not found: " + bookId);
                            int available = rs.getInt("bookAvailableCopies");
                            if (available < 1) throw new SQLException("No available copies for book: " + bookId);
                        }
                    }

                    // Generate borrowId in format: yyyy + sequence number (4 digits)
                    String borrowIdStr = generateBorrowId(borrowDate);
                    long borrowId;
                    try (PreparedStatement pib = conn.prepareStatement(insertBorrow)) {
                        pib.setString(1, borrowIdStr);
                        pib.setString(2, bookId);
                        pib.setString(3, readerId);
                        pib.setDate(4, java.sql.Date.valueOf(borrowDate));
                        pib.setDate(5, java.sql.Date.valueOf(dueDate));
                        pib.setInt(6, 0);
                        int rows = pib.executeUpdate();
                        if (rows != 1) throw new SQLException("Insert borrow failed");
                        borrowId = Long.parseLong(borrowIdStr); // Convert to long for return value
                    }

                    try (PreparedStatement ub = conn.prepareStatement(updateBook)) {
                        ub.setString(1, bookId);
                        ub.executeUpdate();
                    }

                    try (PreparedStatement ur = conn.prepareStatement(updateReader)) {
                        ur.setString(1, readerId);
                        ur.executeUpdate();
                    }

                    created.add(borrowId);
                }
                conn.commit();
                return created;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private static borrowTable mapRowToBorrow(ResultSet rs) throws SQLException {
        borrowTable b = new borrowTable();
        b.setBorrowId(rs.getString("borrowId"));
        b.setBookId(rs.getString("bookId"));
        b.setReaderId(rs.getString("readerId"));
        java.sql.Date bd = rs.getDate("borrowDate"); if (bd != null) b.setBorrowDate(bd.toLocalDate());
        java.sql.Date dd = rs.getDate("dueDate"); if (dd != null) b.setDueDate(dd.toLocalDate());
        b.setBorrowStates(rs.getInt("borrowStates"));
        return b;
    }

    private static borrowTable mapRowToBorrowWithDetails(ResultSet rs) throws SQLException {
        borrowTable b = new borrowTable();
        b.setBorrowId(rs.getString("borrowId"));
        b.setBookId(rs.getString("bookId"));
        b.setReaderId(rs.getString("readerId"));
        java.sql.Date bd = rs.getDate("borrowDate"); if (bd != null) b.setBorrowDate(bd.toLocalDate());
        java.sql.Date dd = rs.getDate("dueDate"); if (dd != null) b.setDueDate(dd.toLocalDate());
        b.setBorrowStates(rs.getInt("borrowStates"));
        
        // Add book and reader details
        b.setBookTitle(rs.getString("bookTitle"));
        b.setReaderName(rs.getString("readerName"));
        return b;
    }

    /**
     * Generate borrowId in format: yyyy + sequence number (4 digits)
     * Example: 20230001
     */
    private static String generateBorrowId(LocalDate borrowDate) throws SQLException {
        String yearPart = borrowDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy"));
        
        // Count existing borrowIds for the same year to determine sequence number
        String countSql = "SELECT COUNT(*) FROM borrowTable WHERE borrowId LIKE ?";
        try (Connection conn = db.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(countSql)) {
            ps.setString(1, yearPart + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    // Format sequence number as 4 digits
                    String sequence = String.format("%04d", count + 1);
                    return yearPart + sequence;
                }
            }
        }
        return yearPart + "0001"; // Default if count fails
    }

}