package service;

import db.db;
import entity.bookInformation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书相关服务层（使用简单的 JDBC 实现）
 * 注意：方法抛出 SQLException，调用方应负责事务和异常处理。
 */
public class bookService {

	public static List<bookInformation> listBooks(int offset, int limit, String search, String author, String category) throws SQLException {
		List<bookInformation> list = new ArrayList<>();
		StringBuilder sql = new StringBuilder("SELECT * FROM bookInformation WHERE 1=1");
		if (search != null && !search.isBlank()) sql.append(" AND (bookName LIKE ? OR isbn LIKE ?)");
		if (author != null && !author.isBlank()) sql.append(" AND bookAuthor = ?");
		if (category != null && !category.isBlank()) sql.append(" AND bookCategory = ?");
		sql.append(" LIMIT ? OFFSET ?");

		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
			int idx = 1;
			if (search != null && !search.isBlank()) {
				String s = "%" + search + "%";
				ps.setString(idx++, s);
				ps.setString(idx++, s);
			}
			if (author != null && !author.isBlank()) ps.setString(idx++, author);
			if (category != null && !category.isBlank()) ps.setString(idx++, category);
			ps.setInt(idx++, limit);
			ps.setInt(idx, offset);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) list.add(mapRowToBook(rs));
			}
		}

		return list;
	}

	public static bookInformation getBookById(String bookId) throws SQLException {
		String sql = "SELECT * FROM bookInformation WHERE bookId = ?";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, bookId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return mapRowToBook(rs);
			}
		}
		return null;
	}

	public static boolean createBook(bookInformation b) throws SQLException {
		String sql = "INSERT INTO bookInformation (bookId, isbn, bookName, bookAuthor, bookPublisher, bookPubDate, bookCategory, bookPrice, bookLocation, bookTotalCopies, bookAvailableCopies, borrowCount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			// Generate bookId if not provided
			String bookId = b.getBookId();
			if (bookId == null || bookId.trim().isEmpty()) {
				bookId = generateBookId();
				b.setBookId(bookId);
			}
			
			ps.setString(1, bookId);
			ps.setString(2, b.getIsbn());
			ps.setString(3, b.getBookName());
			ps.setString(4, b.getBookAuthor());
			ps.setString(5, b.getBookPublisher());
			LocalDate d = b.getBookPubDate();
			if (d != null) ps.setDate(6, java.sql.Date.valueOf(d)); else ps.setDate(6, null);
			ps.setString(7, b.getBookCategory());
			ps.setInt(8, b.getBookPrice());
			ps.setString(9, b.getBookLocation());
			ps.setInt(10, b.getBookTotalCopies());
			ps.setInt(11, b.getBookAvailableCopies());
			ps.setInt(12, b.getBorrowCount());
			return ps.executeUpdate() == 1;
		}
	}

	public static boolean updateBook(bookInformation b) throws SQLException {
		String sql = "UPDATE bookInformation SET isbn=?, bookName=?, bookAuthor=?, bookPublisher=?, bookPubDate=?, bookCategory=?, bookPrice=?, bookLocation=?, bookTotalCopies=?, bookAvailableCopies=?, borrowCount=? WHERE bookId=?";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, b.getIsbn());
			ps.setString(2, b.getBookName());
			ps.setString(3, b.getBookAuthor());
			ps.setString(4, b.getBookPublisher());
			LocalDate d = b.getBookPubDate();
			if (d != null) ps.setDate(5, java.sql.Date.valueOf(d)); else ps.setDate(5, null);
			ps.setString(6, b.getBookCategory());
			ps.setInt(7, b.getBookPrice());
			ps.setString(8, b.getBookLocation());
			ps.setInt(9, b.getBookTotalCopies());
			ps.setInt(10, b.getBookAvailableCopies());
			ps.setInt(11, b.getBorrowCount());
			ps.setString(12, b.getBookId());
			return ps.executeUpdate() == 1;
		}
	}

	public static boolean deleteBook(String bookId) throws SQLException {
		// First check if book has any borrowing records
		String checkSql = "SELECT COUNT(*) FROM borrowTable WHERE bookId = ?";
		try (Connection c = db.getConnection(); PreparedStatement checkPs = c.prepareStatement(checkSql)) {
			checkPs.setString(1, bookId);
			try (ResultSet rs = checkPs.executeQuery()) {
				if (rs.next() && rs.getInt(1) > 0) {
					// Book has borrowing records, cannot delete
					throw new SQLException("Cannot delete book with borrowing records");
				}
			}
		}
		
		// If no borrowing records, proceed with deletion
		String sql = "DELETE FROM bookInformation WHERE bookId = ?";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, bookId);
			return ps.executeUpdate() == 1;
		}
	}

	/**
	 * 调整库存（正为增加、负为减少）。此方法会在单条更新中同时修改 total 和 available。
	 */
	public static boolean adjustStock(String bookId, int adjustment) throws SQLException {
		String sql = "UPDATE bookInformation SET bookTotalCopies = bookTotalCopies + ?, bookAvailableCopies = bookAvailableCopies + ? WHERE bookId = ? AND (bookTotalCopies + ? ) >= 0 AND (bookAvailableCopies + ?) >= 0";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, adjustment);
			ps.setInt(2, adjustment);
			ps.setString(3, bookId);
			ps.setInt(4, adjustment);
			ps.setInt(5, adjustment);
			return ps.executeUpdate() == 1;
		}
	}

	public static List<bookInformation> getPopularBooks(int top) throws SQLException {
		List<bookInformation> list = new ArrayList<>();
		String sql = "SELECT bi.*, COUNT(bt.borrowId) AS borrowTimes FROM borrowTable bt JOIN bookInformation bi ON bt.bookId = bi.bookId GROUP BY bi.bookId ORDER BY borrowTimes DESC LIMIT ?";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, top);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) list.add(mapRowToBook(rs));
			}
		}
		return list;
	}

	public static List<bookInformation> getVacantBooks(int offset, int limit) throws SQLException {
		List<bookInformation> list = new ArrayList<>();
		String sql = "SELECT * FROM bookInformation WHERE bookAvailableCopies > 0 LIMIT ? OFFSET ?";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setInt(1, limit);
			ps.setInt(2, offset);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) list.add(mapRowToBook(rs));
			}
		}
		return list;
	}

	/**
	 * 统计可借图书总数（用于分页）
	 */
	public static int countVacantBooks() throws SQLException {
		String sql = "SELECT COUNT(*) FROM bookInformation WHERE bookAvailableCopies > 0";
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return rs.getInt(1);
			}
		}
		return 0;
	}

	/**
	 * Return total count matching filters for pagination
	 */
	public static int countBooks(String search, String author, String category) throws SQLException {
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM bookInformation WHERE 1=1");
		if (search != null && !search.isBlank()) sql.append(" AND (bookName LIKE ? OR isbn LIKE ?)");
		if (author != null && !author.isBlank()) sql.append(" AND bookAuthor = ?");
		if (category != null && !category.isBlank()) sql.append(" AND bookCategory = ?");
		try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
			int idx = 1;
			if (search != null && !search.isBlank()) {
				String s = "%" + search + "%";
				ps.setString(idx++, s);
				ps.setString(idx++, s);
			}
			if (author != null && !author.isBlank()) ps.setString(idx++, author);
			if (category != null && !category.isBlank()) ps.setString(idx++, category);
			try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
		}
		return 0;
	}

	private static bookInformation mapRowToBook(ResultSet rs) throws SQLException {
		bookInformation b = new bookInformation();
		b.setBookId(rs.getString("bookId"));
		b.setIsbn(rs.getString("isbn"));
		b.setBookName(rs.getString("bookName"));
		b.setBookAuthor(rs.getString("bookAuthor"));
		b.setBookPublisher(rs.getString("bookPublisher"));
		java.sql.Date pd = rs.getDate("bookPubDate");
		if (pd != null) b.setBookPubDate(pd.toLocalDate());
		b.setBookCategory(rs.getString("bookCategory"));
		b.setBookPrice(rs.getInt("bookPrice"));
		b.setBookLocation(rs.getString("bookLocation"));
		b.setBookTotalCopies(rs.getInt("bookTotalCopies"));
		b.setBookAvailableCopies(rs.getInt("bookAvailableCopies"));
		b.setBorrowCount(rs.getInt("borrowCount"));
		return b;
	}

	/**
	 * Generate bookId in format: 41, 42, 43... (start from current max + 1)
	 * Avoid duplicate entries by checking existing IDs
	 */
	private static String generateBookId() throws SQLException {
		// Get the maximum bookId from the database
		String maxIdSql = "SELECT MAX(CAST(bookId AS UNSIGNED)) FROM bookInformation";
		try (Connection conn = db.getConnection(); 
			 PreparedStatement ps = conn.prepareStatement(maxIdSql)) {
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int maxId = rs.getInt(1);
					if (!rs.wasNull() && maxId > 0) {
						// Start from current max + 1
						int newId = maxId + 1;
						
						// Double check if this ID already exists (for safety)
						String checkSql = "SELECT COUNT(*) FROM bookInformation WHERE bookId = ?";
						try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
							checkPs.setString(1, String.valueOf(newId));
							try (ResultSet checkRs = checkPs.executeQuery()) {
								if (checkRs.next() && checkRs.getInt(1) > 0) {
									// If ID exists, try next one
									newId++;
								}
							}
						}
						return String.valueOf(newId);
					}
				}
			}
		}
		// If no records exist or max is 0, start from 41 (since test data has 40 records)
		return "41";
	}

}
