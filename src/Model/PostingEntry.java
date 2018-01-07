package Model;

import java.util.HashMap;

public class PostingEntry {

    String term;
    HashMap<String, MyPair> data;

    public PostingEntry(String term, HashMap<String,MyPair> data){
        this.term = term;
        this.data = data;
    }

}
