package entity;

import java.time.LocalDate;

public class readerInformation {
    /**
     * 读者信息实体类
     * <p>
     * 含义说明： 读者信息
     * 组成：readerId、readerName、readerCardType、readerCardNumber、
     * readerPhoneNumber、registerDate、readerStatus、
     * maxBorrowNumber、nowBorrowNumber
     */
    private String readerId;              // 读者编号（可以是自定义ID）
    private String readerName;            // 读者姓名
    private String readerCardType;        // 证件类型
    private String readerCardNumber;      // 证件号码
    private String readerPhoneNumber;     // 联系电话
    private LocalDate registerDate;         // 注册日期
    private int readerStatus;          // 读者状态（如：正常、挂失
    private int maxBorrowNumber;       // 最大借书数目
    private int nowBorrowNumber;         // 当前借阅数量

    /**
     * 无参构造函数
     */
    public readerInformation() {}
    /**
     * 有参构造函数
     * @param readerId
     * @param readerName
     * @param readerCardType
     * @param readerCardNumber
     * @param readerPhoneNumber
     * @param registerDate
     * @param readerStatus
     * @param maxBorrowNumber
     * @param nowBorrowNumber
     */
    public readerInformation(String readerId, String readerName, String readerCardType,
                             String readerCardNumber,  String readerPhoneNumber,
                             LocalDate registerDate, int readerStatus,
                             int maxBorrowNumber, int nowBorrowNumber) {
        this.readerId = readerId;
        this.readerName = readerName;
        this.readerCardType = readerCardType;
        this.readerCardNumber = readerCardNumber;
        this.readerPhoneNumber = readerPhoneNumber;
        this.registerDate = registerDate;
        this.readerStatus = readerStatus;
        this.maxBorrowNumber = maxBorrowNumber;
        this.nowBorrowNumber = nowBorrowNumber;
    }
    /**
     * Getter和Setter方法
     * @return
     */
    public String getReaderId() {
        return readerId;
    }
    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }
    public String getReaderName() {
        return readerName;
    }
    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }
    public String getReaderCardType() {
        return readerCardType;
    }
    public void setReaderCardType(String readerCardType) {
        this.readerCardType = readerCardType;
    }
    public String getReaderCardNumber() {
        return readerCardNumber;
    }
    public void setReaderCardNumber(String readerCardNumber) {
        this.readerCardNumber = readerCardNumber;
    }
    public String getReaderPhoneNumber() {
        return readerPhoneNumber;
    }
    public void setReaderPhoneNumber(String readerPhoneNumber) {
        this.readerPhoneNumber = readerPhoneNumber;
    }
    public LocalDate getRegisterDate() {
        return registerDate;
    }
    public void setRegisterDate(LocalDate registerDate) {
        this.registerDate = registerDate;
    }
    public int getReaderStatus() {
        return readerStatus;
    }
    public void setReaderStatus(int readerStatus) {
        this.readerStatus = readerStatus;
    }
    // 修改字段名称，使其与数据库设计文档一致
    public int getMaxBorrowNumber() {
        return maxBorrowNumber;
    }
    // 修改字段名称，使其与数据库设计文档一致
    public void setMaxBorrowNumber(int maxBorrowNumber) {
        this.maxBorrowNumber = maxBorrowNumber;
    }
    public int getNowBorrowNumber() {
        return nowBorrowNumber;
    }
    public void setNowBorrowNumber(int nowBorrowNumber) {
        this.nowBorrowNumber = nowBorrowNumber;
    }
    
}