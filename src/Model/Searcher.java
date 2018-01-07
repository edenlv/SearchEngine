package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Created by levye on 04/01/2018.
 */
public class Searcher {
    public static HashMap<Integer, String> postingLinesCache = new HashMap<>();
    public static ArrayList<Query> Queries;

    public static void setQueries(ArrayList<String> qries){
        ArrayList<Integer> postLinesToRead = new ArrayList<>();

        qries.stream().forEach(
                (query) -> {
                    ArrayList<String> parsedQuery = getParsedQuery(query);
                    ArrayList<Integer> neededLines = new ArrayList<>();
                    parsedQuery.forEach( (term) ->{
                        int line = Dictionary.md_Dictionary.get(term).postingLine;
                        neededLines.add(line);
                    });

                    postLinesToRead.addAll(neededLines);

                    Query qry = new Query();
                    qry.parsedQuery = parsedQuery;
                    qry.postLinesNeeded = new Integer[neededLines.size()];
                    neededLines.toArray(qry.postLinesNeeded);
                    Queries.add(qry);
                }
        );

        try (Stream<String> lines = Files.lines(Paths.get(Indexer.getPostingFilePath()))) {
            int lastLine = 0;

            postLinesToRead.sort(Comparator.naturalOrder());

            for (int lineNumber : postLinesToRead){
                if (postingLinesCache.containsKey(lineNumber)) continue;

                String currentLine = lines.skip(lineNumber - lastLine).findFirst().get();
                postingLinesCache.put(lineNumber, currentLine);
                lastLine = lineNumber;
            }

        } catch (IOException e){
            e.printStackTrace();
            System.out.println("Couldn't read posting file.");
        }

        Queries.stream().forEach(
                (query) -> {
                    for(int i=0;i<query.postLinesNeeded.length;i++){
                        String postline = postingLinesCache.get(query.postLinesNeeded[i]);

                        PostingEntry pEntry = processPostingLine(postline);

                        query.postingLines.put(pEntry.term,pEntry.data);

                    }
                }
        );
    }



    public static ArrayList<String> getParsedQuery(String query){
        ReadFile.docBuffer.clear();
        ReadFile.docBuffer.add(new DocPair(query, ""));

        Parse.queryParsing = true;
        Parse.parse();
        Parse.queryParsing = false;

        Document tDoc = Parse.parsedDocs.get(0);
        ArrayList<String> parsedQuery = new ArrayList<>(tDoc.hMap.keySet());

        return parsedQuery;
    }


    public static PostingEntry processPostingLine(String postingLine){
        HashMap<String, MyPair> data = new HashMap<>();

        String[] splitData = postingLine.split("#|,");
        System.out.printf("Read posting line number %d of term %s\n", postingLine, splitData[0]);

        for (int i = 1; i < splitData.length; i++) {
            String[] aux = splitData[i].split(":");
            MyPair pair = new MyPair(Integer.parseInt(aux[1]), Integer.parseInt(aux[2]));
            data.put(aux[0], pair);
        }

        PostingEntry pEntry = new PostingEntry(splitData[0],data);

        return pEntry;
    }


}
