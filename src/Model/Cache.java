package Model;

import java.io.*;
import java.util.*;

import static Model.ReadFile.postingsPath;

/**
 * Created by levye on 15/12/2017.
 */
public class Cache {
    public static String cacheFilePath = null;
    public static HashSet<String> mostFreqWords = _loadMostFreqWords();
    public static HashMap<String, CacheEntry> md_cache = new HashMap<>();
    public static long size = 0;


    public static void addEntry(String term, CacheEntry cacheEntry){
        md_cache.put(term, cacheEntry);
        size+=term.length();
        size+=cacheEntry.getSize();
    }

    public static HashSet<String> _loadMostFreqWords(){
        try {
            HashSet<String> set = new HashSet<>();
            BufferedReader buffer = new BufferedReader(new FileReader(".\\src\\10000_words" + (Parse.toStem ? "_stem" : "") + ".txt"));
            String line = buffer.readLine();

            while (line!=null){
                set.add(line);
                line = buffer.readLine();
            }

            return set;

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }


    public static void loadCache(String filePath){

        String line;
        try {
            cacheFilePath = filePath;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            HashMap<String, CacheEntry> newCache = new HashMap<>();
            while((line=reader.readLine())!=null){
                String[] parts = line.split("#");
                String key = parts[0];
                newCache.put(key,new CacheEntry((parts[0]),Integer.parseInt(parts[1]),(parts[2])));
            }
            reader.close();

            md_cache.clear();
            md_cache = newCache;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveCache(String folderPath){

        try {
            FileWriter fileWriter = new FileWriter(getCacheFullPath(folderPath));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            TreeMap<String, CacheEntry> sortedMap = new TreeMap<>(md_cache);
            Set set = sortedMap.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                bufferedWriter.write(mentry.getValue().toString());
                if (iterator.hasNext()) bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static String getCacheFullPath(String folderPath){
//        if (cacheFilePath == null){
//            cacheFilePath = postingsPath+"\\Cache " + (Parse.toStem? "_WithStem" : "") + " .txt";
//        }
        return folderPath+"\\Cache" + (Parse.toStem? "_WithStem" : "") + ".txt";
    }

    public static void resetCache(){
        md_cache.clear();
        size = 0;
    }


    /*FOR FINDING 10000 MOST FREQ WORDS*/
    private static void writeWord(List<String> terms){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(ReadFile.postingsPath + "\\tempCache.txt"));
            for (int i=0; i<terms.size(); i++){
                writer.append(terms.get(i));
                if (i+1<terms.size()) writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (Exception e){e.printStackTrace();}
    }


    public static long getSizeInBytes(){
        return size;
    }

}
