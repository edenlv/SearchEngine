package Model;

public class PreQuery {
    public String queryString;//raw query string
    public int queryNumber;//query number
    public String description;//description of the query (from query file format) - not used

    public PreQuery(String queryString, int queryNumber, String description) {
        this.queryString = queryString;
        this.queryNumber = queryNumber;
        this.description = description;
    }

    public PreQuery(){

    }
}
