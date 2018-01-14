package Model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by levye on 06/01/2018.
 */
public class Query {

    public ArrayList<String> parsedQuery;//holds the parsed query words
    public Integer[] postLinesNeeded;//all the posting lines needed to read from postingfile (numbers of lines)
    public HashMap<String, HashMap<String, MyPair>> postingLines; //key=term, hashmap=data(key=docID, mypair=tf:idx)
    public int queryNumber;//number of the query

    public Query(){
        postingLines = new HashMap<>();
        queryNumber = 0;
    }

    public Query(int num){
        postingLines = new HashMap<>();
        this.queryNumber = num;
    }



}
