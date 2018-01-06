package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {
    public HashMap<String, QueryResult> queryResults;
    public ArrayList<String> parsedQuery;
    public HashMap<String, HashMap<String, MyPair>> postingLinesData;
    public double queryVectorSize;


    public Ranker(ArrayList<String> parsedQry, HashMap<String, HashMap<String, MyPair>> postings) {
        this.queryResults = new HashMap<String, QueryResult>();
        this.parsedQuery = parsedQry;
        this.postingLinesData = postings;
        this.queryVectorSize = Math.abs(parsedQry.size());
    }


    public void runRanking() {
        this.queryResults.clear();

        this.postingLinesData.entrySet().stream().forEach(
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


    public static HashMap<String, MyPair> getPostingData(int postingLine) {
        HashMap<String, MyPair> data = new HashMap<>();

        File postingFile = new File(Indexer.getPostingFilePath());
        String lineData = null;
        try (Stream<String> lines = Files.lines(postingFile.toPath())) {
            lineData = lines.skip(postingLine).findFirst().get();
        } catch (IOException ex) {
            System.err.println("Could not read from posting file!");
            ex.printStackTrace();
            return null;
        }

        String[] splitData = lineData.split("#|,");
        System.out.printf("Read posting line number %d of term %s\n", postingLine, splitData[0]);

        for (int i = 1; i < splitData.length; i++) {
            String[] aux = splitData[i].split(":");
            MyPair pair = new MyPair(Integer.parseInt(aux[1]), Integer.parseInt(aux[2]));
            data.put(aux[0], pair);
        }

        return data;
    }

}
