package Model;

import View.Controller;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by levye on 04/01/2018.
 */
public class Searcher {
    public static HashMap<Integer, String> postingLinesCache = new HashMap<>();
    public static ArrayList<Query> Queries = new ArrayList<Query>();

    public static LinkedList<Ranker> setQueries(LinkedList<PreQuery> queries){
//        postingLinesCache.clear();
        Queries.clear();

        ArrayList<Integer> postLinesToRead = new ArrayList<>();
        LinkedList<Ranker> rankers = new LinkedList<Ranker>();

        for (int i=0; i<queries.size(); i++) {
            PreQuery pQuery = queries.get(i);
            String query = pQuery.queryString;

            ArrayList<String> parsedQuery = getParsedQuery(query);
            ArrayList<Integer> neededLines = new ArrayList<>();

            for (int j=0; j< parsedQuery.size(); j++) {
                String term = parsedQuery.get(j);
                DictionaryEntry dEntry = Dictionary.md_Dictionary.get(term);
                if (dEntry == null) {
                    parsedQuery.remove(term);
                } //if the word in the parsed query is not in the dictionary...
                else {
                    int line = dEntry.postingLine;
                    neededLines.add(line);
                }
            }

             postLinesToRead.addAll(neededLines);

             Query qry = new Query(pQuery.queryNumber);

             qry.parsedQuery = parsedQuery;
             qry.postLinesNeeded = new Integer[neededLines.size()];
             neededLines.toArray(qry.postLinesNeeded);
             Queries.add(qry);

             rankers.add(new Ranker(qry));

        }

        for (int k=0; k<postLinesToRead.size(); k++){
            if (postingLinesCache.containsKey(postLinesToRead.get(k))) continue;
            String line = getLineFromPostingFile(postLinesToRead.get(k));
            postingLinesCache.put(postLinesToRead.get(k), line);
        }

        for (int k=0; k<Queries.size(); k++) {
            Query query = Queries.get(k);
            for (int i = 0; i < query.postLinesNeeded.length; i++) {
                String postline = postingLinesCache.get(query.postLinesNeeded[i]);

                PostingEntry pEntry = processPostingLine(postline);

                query.postingLines.put(pEntry.term, pEntry.data);

            }
        }


        System.out.println("Setting queries completed");
        return rankers;
    }


    public static ArrayList<String> getParsedQuery(String query){
        ReadFile.docBuffer.clear();
        Parse.uppercaseLongTerm.setLength(0);
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

        for (int i = 1; i < splitData.length; i++) {
            String[] aux = splitData[i].split(":");
            MyPair pair = new MyPair(Integer.parseInt(aux[1]), Integer.parseInt(aux[2]));
            data.put(aux[0], pair);
        }

        PostingEntry pEntry = new PostingEntry(splitData[0],data);

        return pEntry;
    }

//    public static ArrayList<String> getExtendedQuery(String q){
//        String[] words = q.split(" ");
////        HttpURLConnection conn = new Http
//    }



    public static String getLineFromPostingFile(int lineNumber){
        try (Stream<String> lines = Files.lines(Paths.get(Indexer.getPostingFilePath()))) {
            return lines.skip(lineNumber).findFirst().get();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }



//    private static <K, V extends Comparable<? super V>> List<Map.Entry<K, V>>
//    findGreatest(Map<K, V> map, int n) {
//        Comparator<? super Map.Entry<K, V>> comparator =
//                new Comparator<Map.Entry<K, V>>() {
//                    @Override
//                    public int compare(Map.Entry<K, V> e0, Map.Entry<K, V> e1) {
//                        V v0 = e0.getValue();
//                        V v1 = e1.getValue();
//                        return v0.compareTo(v1);
//                    }
//                };
//        PriorityQueue<Map.Entry<K, V>> highest =
//                new PriorityQueue<Map.Entry<K, V>>(n, comparator);
//        for (Map.Entry<K, V> entry : map.entrySet()) {
//            highest.offer(entry);
//            while (highest.size() > n) {
//                highest.poll();
//            }
//        }
//
//        List<Map.Entry<K, V>> result = new ArrayList<Map.Entry<K, V>>();
//        while (highest.size() > 0) {
//            result.add(highest.poll());
//        }
//        return result;
//    }




}
