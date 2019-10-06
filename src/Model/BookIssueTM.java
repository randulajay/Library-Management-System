package Model;

public class BookIssueTM {
    private String issueId;
    private String date;
    private String memberId;
    private String bookId;

    public BookIssueTM(String issueId, String date, String memberId, String bookId) {
        this.issueId = issueId;
        this.date = date;
        this.memberId = memberId;
        this.bookId = bookId;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
