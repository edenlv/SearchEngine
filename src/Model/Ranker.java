package Model;

import View.Controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {
    public HashMap<String, QueryResult> queryResults;//holds ALL of the results of the query
    public Query query;//the query of this specific ranker
    public double queryVectorSize;//the query's vector size, calcualted once
    public LinkedList<QueryResult> result50;//holds the 50\70 most relevant results of the query (saved once as cache)


    public Ranker(Query qry) {
        this.queryResults = new HashMap<String, QueryResult>();
        this.query = qry;
        this.queryVectorSize = Math.abs(this.query.parsedQuery.size());
    }

    /*
    Runs the actual query ranking computings. Calls calcPartialCosSim() to calculate the ranking score.
     */
    public void runRanking() {
        this.queryResults.clear();

        this.query.postingLines.entrySet().stream().forEach(
                (postingLine) -> {
                    String currTerm = postingLine.getKey();
                    HashMap<String, MyPair> currentLine = postingLine.getValue();
                    double currTermIDF = Dictionary.getWordIDF(currTerm);

                    currentLine.entrySet().stream().forEach(
                            (entry) -> {
                                MyPair pair = entry.getValue();
                                calcPartialCosSim(currTerm, Document.get(entry.getKey()), pair, currTermIDF);
                            });
                }
        );

    }

    /*
    Calculates the score of a document. Adds up to previous score if document was found more than once.
    Using CosSim formula and BM25 formula.
     */
    public void calcPartialCosSim(String term, Document doc, MyPair pair, double termIDF) {

        double normalTF = pair.tf / doc.mostFrequentTermValue;
        double Wij = normalTF * termIDF;
        double pCosSim = Wij / (doc.docVectorSize * this.queryVectorSize);

        double k1 = 1.4;
        double b = 0.75;

        double df = Dictionary.md_Dictionary.get(term).df;
        double altIDF = Math.log((Document.documentsCollection.size()-df+0.5)/(df+0.5))/Math.log(2);

        double top = altIDF * pair.tf * (k1+1);
        double bottom = pair.tf + (k1*(1-b+(b*doc.documentLength/Document.getAvgDocSize())));
        double bm25 = top/bottom;

        double docRank = (1*pCosSim) + (1*bm25);

        QueryResult qResult = this.queryResults.get(doc.docID);
        if (qResult == null) {
            this.queryResults.put(doc.docID, new QueryResult(doc, docRank, term));
        } else {
            qResult.addTerm(term);
            qResult.addCosSim(docRank);
        }

    }

    /*
    Returns the 50 or 70 most relevant results of the query. Saves in a variable for cache - no need to compute twice.
     */
    public LinkedList<QueryResult> getResult50(){
        if (queryResults.size()==0) return null;

        if (this.result50==null) {
            LinkedList<QueryResult> results = new LinkedList<QueryResult>();

            this.queryResults.values().stream().sorted(
                    (val1, val2) -> {
                        return Double.compare(val2.cosSim, val1.cosSim);
                    }
            ).limit(Controller.extendedQuery ? 70 : 50).forEach(
                    (qResult) -> {
                        results.add(qResult);
                    }
            );

            this.result50 = results;
        }

        return this.result50;
    }


    /*
    returns toString for TREC format of the results of this query.
     */
    public LinkedList<String> toArrayString(){
        LinkedList<String> res = new LinkedList<>();

        this.getResult50().stream().sequential().forEach(
                (qResult) -> {
                    String str = "";
                    if (this.query.queryNumber<10) str+="00";
                    else if (this.query.queryNumber<100) str+="0";
                    str+=String.valueOf(this.query.queryNumber);
                    str+= " 1 "; //stam mispar
                    str+= qResult.document.getDocID();
                    str+= " 100";
                    str+= " 5.0";
                    str+=" mt";
                    res.add(str);
                }
        );


        return res;
    }

    /*
    Returns toString of the results to show after each query run.
     */
    public LinkedList<String> prettifyResult(){
        LinkedList<String> res = new LinkedList<>();

        this.getResult50().stream().sequential().forEach(
                (qResult) -> {
                    res.add(qResult.document.docID);
                }
        );

        return res;
    }

}
