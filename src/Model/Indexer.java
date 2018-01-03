package Model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Dictionary;
import java.util.stream.Stream;

public class Indexer {
    public static HashMap<String, StringBuilder> currentTermsDictionary = new HashMap<>();
    public static int fileCounter = 0; // Files Counter
    public static String tempPostingDirName = "temp";
    public static String postingFilePath = null;

    private static void resetIndexer(){ //CALL THIS METHOD AFTER INDEXER IS FINISHED WITH 1 CHUNK
        currentTermsDictionary.clear();
        fileCounter++;
    }

    /*Creates a temporary dictionary from entire documents in iteration -> currentTErmsDictionary*/
    public static void createDicFromParsedDocs() {

        ArrayList<Document> docsFromParser = Parse.parsedDocs;

        for (Document d : docsFromParser) {
            Iterator termsIterator = d.hMap.keySet().iterator();

            while (termsIterator.hasNext()){
                String term = (String)termsIterator.next();
                StringBuilder sb = currentTermsDictionary.get(term);
                if (sb==null) {
                    sb = new StringBuilder();
                    currentTermsDictionary.put(term, sb);
                }
                if (sb.length()!=0) sb.append(",");
                sb.append(d.docID);
                sb.append(":");
                sb.append(d.hMap.get(term).getDf());
//                sb.append(":");
//                sb.append(d.hMap.get(term).getIdxInDoc());

//                Model.Dictionary.IncrementDF(term);
//                Model.Dictionary.AddTF(term, d.hMap.get(term));
            }

//            d.hMap.clear();
//            d.hMap = null;
            Document.documentsCollection.put(d.docID, d);
        }
        createTempPostingFile();
        resetIndexer();

    }

    /*Goes through the temporary dictionary and creates a temporary posting file accrodingly*/
    public static void createTempPostingFile() {
        ArrayList<String> termsList = new ArrayList<>(currentTermsDictionary.keySet());
        Collections.sort(termsList);

        PrintWriter writer = getPW(fileCounter);

        if (writer==null){
            System.out.println("Posting folder not found!! - Cannot create temp posting number: " + fileCounter);
            return;
        }

        for (String term : termsList) {
            writer.println(term + "#" + currentTermsDictionary.get(term).toString());
        }

        writer.flush();
        writer.close();

    }

    /*returns a PrintWriter to which you can write the next temporary posting file*/
    public static PrintWriter getPW(int counter){
        try{
            String tempPostingDirPath = ReadFile.postingsPath+"\\"+tempPostingDirName;

            File f = new File(tempPostingDirPath);
            if (!f.exists())
                f.mkdir();
            File file = new File(tempPostingDirPath + "\\"  + counter + ".txt");
            if (file.exists()) file.delete();
            PrintWriter pw = new PrintWriter(
                                new FileOutputStream(
                                    file,true)
            );
            return pw;
        } catch(IOException e){}
        return null;
    }

    /*MERGE ALGORITHM -> Merges all temp posting files into one big final file*/
    public static void mergeSort(){
        try {

            PriorityQueue<MyReader> queue = new PriorityQueue<>(364, new Comparator<MyReader>() {
                @Override
                public int compare(MyReader o1, MyReader o2) {
                    return o1.key.compareTo(o2.key);
                }
            });

            postingFilePath = ReadFile.postingsPath + "\\" + "PostingFile" + ((Parse.toStem) ? "_WithStem" : "") + ".txt";
            File posting_file = new File(postingFilePath);
            if (posting_file.exists()) posting_file.delete();
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(postingFilePath)));

            for (int i = 0; i < fileCounter; i++) {
                String path = ReadFile.postingsPath + "\\" + tempPostingDirName +"\\"+ i + ".txt";
                File f = new File(path);
                queue.add(new MyReader(new BufferedReader(new FileReader(f)), path));
            }

            if (queue.peek().empty()) return;

            String lastTermWritten = queue.peek().key;
            boolean firstLoop = true;
            while (queue.size()>0){
                MyReader reader = queue.poll();
                String nextTermToWrite = reader.key;
                if (firstLoop){
                    writer.print(reader.cache);
                    lastTermWritten = null;
                    firstLoop = false;
                } else {
                    if (nextTermToWrite.equals(lastTermWritten)) {
                        writer.print(",");
                        writer.print(reader.val);
                    } else {
                        lastTermWritten = nextTermToWrite;
                        writer.println();
                        writer.print(reader.cache);
                    }
                }
                reader.reload();
                if (!reader.empty()) queue.add(reader);
                else {
                    reader.close();
                    reader.deleteFile();
                }
            }


            writer.close();

        } catch(Exception e){e.printStackTrace();}

        new File(ReadFile.postingsPath+"\\"+tempPostingDirName).delete();
        System.out.println("Size of Inverted Index file: " + getIndexSizeInBytes() + " [bytes]");

    }

    public static long getIndexSizeInBytes(){
        return new File(postingFilePath).length();
    }




}


