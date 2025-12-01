package entity;

import java.time.LocalDate;

public class returnTable {
    /**
     * 归还信息实体类
     * <p>
     * 含义说明： 归还信息
     * 组成：returnId、borrowId、returnDate、overDays、fine
     */
    private String returnId;        // 归还编号（可以是自定义ID）
    private String borrowId;        // 借阅编号
    private LocalDate returnDate;      // 归还日期
    private int overDays;          // 逾期天数
    private double fine;           // 罚款金额
    /**
     * 无参构造函数
     */
    public returnTable() {}
    /**
     * 有参构造函数
     * @param returnId
     * @param borrowId
     * @param returnDate
     * @param overDays
     * @param fine
     */
    public returnTable(String returnId, String borrowId,
                       LocalDate returnDate, int overDays, double fine) {
        this.returnId = returnId;
        this.borrowId = borrowId;
        this.returnDate = returnDate;
        this.overDays = overDays;
        this.fine = fine;
    }
    /**
     * Getter和Setter方法
     * @return
     */
    public String getReturnId() {
        return returnId;
    }
    public void setReturnId(String returnId) {
        this.returnId = returnId;
    }
    public String getBorrowId() {
        return borrowId;
    }
    public void setBorrowId(String borrowId) {
        this.borrowId = borrowId;
    }
    public LocalDate getReturnDate() {
        return returnDate;
    }
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    public int getOverDays() {
        return overDays;
    }
    public void setOverDays(int overDays) {
        this.overDays = overDays;
    }
    public double getFine() {
        return fine;
    }
    public void setFine(double fine) {
        this.fine = fine;
    }
            



}
