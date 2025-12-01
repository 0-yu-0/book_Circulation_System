package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 图书信息实体类
 * <p>
 * 含义说明： 图书信息
 * 组成： bookId、isbn、bookName、bookAuthor、bookPublisher、bookPubDate、
 *       bookCategory、bookLocation、bookTotalCopies、bookAvailableCopies、borrowCount
 */
public class bookInformation {

	private String bookId;              // 图书编号（可以是条形码或自定义ID）
	private String isbn;                // 国际标准书号
	private String bookName;            // 书名
	private String bookAuthor;          // 作者
	private String bookPublisher;       // 出版社
	private LocalDate bookPubDate;      // 出版日期
	private String bookCategory;        // 分类
    private int bookPrice;              // 价格
	private String bookLocation;        // 馆藏/位置信息（书架/楼层）
	private int bookTotalCopies;        // 总藏书数
	private int bookAvailableCopies;    // 可供借阅的副本数
	private int borrowCount;            // 借阅次数（累计）

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

	/**
     * 无参构造函数
     */
    public bookInformation() {
    }
    /**
     * 有参构造函数
     * @param bookId
     * @param isbn
     * @param bookName
     * @param bookAuthor
     * @param bookPublisher
     * @param bookPubDate
     * @param bookCategory
     * @param bookPrice
     * @param bookLocation
     * @param bookTotalCopies
     * @param bookAvailableCopies
     * @param borrowCount
     */
    public bookInformation(String bookId, String isbn, String bookName, String bookAuthor,
                           String bookPublisher, LocalDate bookPubDate, String bookCategory,
                           int bookPrice, String bookLocation, int bookTotalCopies,
                           int bookAvailableCopies, int borrowCount) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
        this.bookPublisher = bookPublisher;
        this.bookPubDate = bookPubDate;
        this.bookCategory = bookCategory;
        this.bookPrice = bookPrice;
        this.bookLocation = bookLocation;
        this.bookTotalCopies = bookTotalCopies;
        this.bookAvailableCopies = bookAvailableCopies;
        this.borrowCount = borrowCount;
    }
    /**
     * Getter和Setter方法
     * @return
     */
    public String getBookId() {
        return bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public String getBookName() {
        return bookName;
    }
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    public String getBookAuthor() {
        return bookAuthor;
    }
    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }
    public String getBookPublisher() {
        return bookPublisher;
    }
    public void setBookPublisher(String bookPublisher) {
        this.bookPublisher = bookPublisher;
    }
    public LocalDate getBookPubDate() {
        return bookPubDate;
    }
    public void setBookPubDate(LocalDate bookPubDate) {
        this.bookPubDate = bookPubDate;
    }
    public String getBookCategory() {
        return bookCategory;
    }
    public void setBookCategory(String bookCategory) {
        this.bookCategory = bookCategory;
    }
    public int getBookPrice() {
        return bookPrice;
    }
    public void setBookPrice(int bookPrice) {
        this.bookPrice = bookPrice;
    }
    public String getBookLocation() {
        return bookLocation;
    }
    public void setBookLocation(String bookLocation) {
        this.bookLocation = bookLocation;
    }
    public int getBookTotalCopies() {
        return bookTotalCopies;
    }
    public void setBookTotalCopies(int bookTotalCopies) {
        this.bookTotalCopies = bookTotalCopies;
    }
    public int getBookAvailableCopies() {
        return bookAvailableCopies;
    }
    public void setBookAvailableCopies(int bookAvailableCopies) {
        this.bookAvailableCopies = bookAvailableCopies;
    }
    public int getBorrowCount() {
        return borrowCount;
    }
    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }
    public static DateTimeFormatter getDateFormatter() {
        return DATE_FORMATTER;
    }
    
}
