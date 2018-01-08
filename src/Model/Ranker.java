package Model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {
    public HashMap<String, QueryResult> queryResults;
    public Query query;
    public double queryVectorSize;
    public ArrayList<QueryResult> result50;


    public Ranker(Query qry) {
        this.queryResults = new HashMap<String, QueryResult>();
        this.query = qry;
        this.queryVectorSize = Math.abs(this.query.parsedQuery.size());
    }

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


    public void calcPartialCosSim(String term, Document doc, MyPair pair, double termIDF) {

        double normalTF = pair.tf / doc.mostFrequentTermValue;
        double Wij = normalTF * termIDF;
        double pCosSim = Wij / (doc.docVectorSize * this.queryVectorSize);

        QueryResult qResult = this.queryResults.get(doc.docID);
        if (qResult == null) {
            this.queryResults.put(doc.docID, new QueryResult(doc, pCosSim, term));
        } else {
            qResult.addTerm(term);
            qResult.addCosSim(pCosSim);
        }

    }

    public ArrayList<QueryResult> getResult50(){
        if (queryResults.size()==0) return null;

        if (this.result50==null) {
            ArrayList<QueryResult> results = new ArrayList<QueryResult>();

            this.queryResults.values().stream().sorted(
                    (val1, val2) -> {
                        return Double.compare(val2.cosSim, val1.cosSim);
                    }
            ).limit(50).forEach(
                    (qResult) -> {
                        results.add(qResult);
                    }
            );

            this.result50 = results;
        }

        return this.result50;
    }

    public void writeResults(String filePath){
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filePath));



            writer.flush();
            writer.close();
        } catch (IOException e){e.printStackTrace();}


    }

    public void printResults(){
        this.getResult50().stream().forEach(
                (qResult) ->{
                    System.out.println(qResult.cosSim + " " + qResult.document.getDocID() + qResult.matchedTerms.size());
                }
        );
    }

    public ArrayList<String> toArrayString(){
        ArrayList<String> res = new ArrayList<>();

        this.getResult50().stream().forEach(
                (qResult) -> {
                    String str = "";
                    if (this.query.queryNumber<10) str+="00";
                    else if (this.query.queryNumber<100) str+="0";
                    str+=String.valueOf(this.query.queryNumber);
                    str+= " 1"; //stam mispar
                    str+= qResult.document.getDocID();
                    str+= " 100";
                    str+= " 5.0";
                    str+=" mt";
                    res.add(str);
                }
        );


        return res;
    }


//    public static HashMap<String, MyPair> getPostingData(int postingLine) {
//        HashMap<String, MyPair> data = new HashMap<>();
//
//        File postingFile = new File(Indexer.getPostingFilePath());
//        String lineData = null;
//        try (Stream<String> lines = Files.lines(postingFile.toPath())) {
//            lineData = lines.skip(postingLine).findFirst().get();
//        } catch (IOException ex) {
//            System.err.println("Could not read from posting file!");
//            ex.printStackTrace();
//            return null;
//        }
//
//        String[] splitData = lineData.split("#|,");
//        System.out.printf("Read posting line number %d of term %s\n", postingLine, splitData[0]);
//
//        for (int i = 1; i < splitData.length; i++) {
//            String[] aux = splitData[i].split(":");
//            MyPair pair = new MyPair(Integer.parseInt(aux[1]), Integer.parseInt(aux[2]));
//            data.put(aux[0], pair);
//        }
//
//        return data;
//    }

}
