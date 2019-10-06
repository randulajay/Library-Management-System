package Model;

public class BookReturnTM {
    private String id;
    private String issuedDate;
    private String returnedDate;
    private float fine;

    public BookReturnTM(String id, String issuedDate, String returnedDate, float fine) {
        this.id = id;
        this.issuedDate = issuedDate;
        this.returnedDate = returnedDate;
        this.fine = fine;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(String issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(String returnedDate) {
        this.returnedDate = returnedDate;
    }

    public float getFine() {
        return fine;
    }

    public void setFine(float fine) {
        this.fine = fine;
    }
}
