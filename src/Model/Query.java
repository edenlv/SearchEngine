package Model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by levye on 06/01/2018.
 */
public class Query {

    ArrayList<String> parsedQuery;
    int[] postLinesNeeded;
    HashMap<String, HashMap<String, MyPair>> postingLines; //key=term, hashmap=data(key=docID, mypair=tf:idx)

}
