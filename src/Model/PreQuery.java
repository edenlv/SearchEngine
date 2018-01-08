package Model;

public class PreQuery {
    public String queryString;
    public int queryNumber;
    public String description;

    public PreQuery(String queryString, int queryNumber, String description) {
        this.queryString = queryString;
        this.queryNumber = queryNumber;
        this.description = description;
    }
}
