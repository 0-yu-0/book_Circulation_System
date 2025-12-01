package entity;

import java.time.LocalDate;

public class borrowTable {
    /**
     * 借阅信息实体类
     * <p>
     * 含义说明： 借阅信息
     * 组成：borrowId、bookId、readerId、borrowDate、dueDate、borrowStates
     */
    private String borrowId;        // 借阅编号（可以是自定义ID）
    private String bookId;          // 图书编号
    private String readerId;        // 读者编号
    private LocalDate borrowDate;    // 借阅日期
    private LocalDate dueDate;       // 应还日期
    private int borrowStates;      // 借阅状态（如：已借出、已归还、逾期未还）
    /**
     * 无参构造函数
     */
    public borrowTable() {}
    /**
     * 有参构造函数
     * @param borrowId
     * @param bookId
     * @param readerId
     * @param borrowDate
     * @param dueDate
     * @param borrowStates
     */
    public borrowTable(String borrowId, String bookId, String readerId,
                       LocalDate borrowDate, LocalDate dueDate, int borrowStates) {
        this.borrowId = borrowId;
        this.bookId = bookId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.borrowStates = borrowStates;
    }    
    /**
     * Getter和Setter方法
     * @return
     */
    public String getBorrowId() {
        return borrowId;
    }
    public void setBorrowId(String borrowId) {
        this.borrowId = borrowId;
    }
    public String getBookId() {
        return bookId;
    }
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    public String getReaderId() {
        return readerId;
    }
    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }
    public LocalDate getBorrowDate() {
        return borrowDate;
    }
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public int getBorrowStates() {
        return borrowStates;
    }
    public void setBorrowStates(int borrowStates) {
        this.borrowStates = borrowStates;
    }
    
}
