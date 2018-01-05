package Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class Ranker {

    public static double getQueryVectorSize(String query){
        int queryLength = query.split(" ").length;
        double vectorSize = Math.sqrt(queryLength);
        return vectorSize;
    }


    public static HashMap<String, MyPair> getPostingData(String term){
        HashMap<String, MyPair> data = new HashMap<>();

        int postingLine = 2560301;

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

        for (int i=1; i<splitData.length;i++){
            String[] aux = splitData[i].split(":");
            MyPair pair = new MyPair(Integer.parseInt(aux[1]),Integer.parseInt(aux[2]));
            data.put(aux[0],pair);
        }
        return data;
    }

    public static DictionaryEntry getDictionaryData(String term){
        return Dictionary.preDictionary.get(term);
    }

}
