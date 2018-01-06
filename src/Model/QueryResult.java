package Model;

import javax.management.Query;
import java.util.ArrayList;

/**
 * Created by levye on 06/01/2018.
 */
public class QueryResult {

    Document document;
    double cosSim;
    ArrayList<String> matchedTerms;


    public QueryResult(){
        this.matchedTerms = new ArrayList<String>();
    }

    public QueryResult(Document doc){
        this.matchedTerms = new ArrayList<String>();
        this.document = doc;
    }
    public QueryResult(Document doc, double sim){
        this.matchedTerms = new ArrayList<String>();
        this.document = doc;
        this.cosSim = sim;
    }

    public QueryResult(Document doc, double sim, String firstTerm){
        this.matchedTerms = new ArrayList<String>();
        this.addTerm(firstTerm);
        this.document = doc;
        this.cosSim = sim;
    }

    public void addTerm(String term){
        this.matchedTerms.add(term);
    }

    public void addCosSim(double cosSim){
        this.cosSim+=cosSim;
    }

}
