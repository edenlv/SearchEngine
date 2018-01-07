package Model;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import static Model.ReadFile.postingsPath;

/**
 * Created by levye on 15/12/2017.
 */
public class Dictionary {

    public static HashMap<String, DictionaryEntry> md_Dictionary = new HashMap<>();
    public static HashMap<String, DictionaryEntry> preDictionary = new HashMap<>();

    public static int lineCounter = 0;
    public static String dictionaryFullPath = null;

    /*
     * CREATES DICTIONARY AND CACHE FROM FINAL POSTING FILE
     */
    public static void createDictionaryFromPosting(String postingFilePath){
        File f = new File(postingFilePath);
        Dictionary.md_Dictionary.clear();
        Cache.md_cache.clear();

        HashSet<String> terms = Cache.mostFreqWords;

        try (Stream<String> lines = Files.lines(Paths.get(postingFilePath))){
            lines.forEach((line)->{
                String[] splitted = line.split("#");
                String term = splitted[0];
                String data = splitted[1];

                String[] docs = data.split(",");

                /*STUFF FOR DICTIONARY*/
                int sumTF = 0;
                for (int i=0;i<docs.length; i++){
                    String number = docs[i].split(":")[1];
                    sumTF += Parse.parseInt(number);
                }

                if (sumTF>1){

                    DictionaryEntry dicEntry = new DictionaryEntry(docs.length, sumTF, lineCounter);


                    /*STUFF FOR CACHE*/
                    if (terms.contains(term)){
                        CacheEntry cacheEntry = new CacheEntry(term, lineCounter);
                        if (docs.length>150){
                            StringBuilder stringBuilder = new StringBuilder(200);
                            for (int k=0; k<150; k++){
                                stringBuilder.append(docs[k]);
                                if (k+1<150) stringBuilder.append(",");
                            }
                            cacheEntry.data = new String(stringBuilder.toString());
                        } else {
                            cacheEntry.data = new String(data);
                        }
                        Cache.addEntry(term, cacheEntry);
                        dicEntry.isCached = true;
                    }

                    Dictionary.md_Dictionary.put(term, dicEntry);
                /*END OF DICTIONARY & CACHE CREATION*/
                }

                lineCounter++;

            });
        } catch (IOException e){}
    }

    public static String getDictionaryPath(String folderPath){
        return folderPath+"\\Dictionary"+ (Parse.toStem? "_WithStem" : "") + ".txt";

//        if (dictionaryFullPath == null){
//            dictionaryFullPath = postingsPath+"\\Dictionary"+ (Parse.toStem? "_WithStem" : "") + ".txt";
//        }
//        return dictionaryFullPath;
    }

    public static void resetDictionary(){
        md_Dictionary.clear();
        Document.documentsCollection.clear();
    }

    /* SAVES DICTIONARY TO FILE INSIDE POSTINGS FOLDER*/
    public static void saveDictionary(String folderPath){

        try {
            FileWriter fileWriter = new FileWriter(getDictionaryPath(folderPath));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            TreeMap<String, DictionaryEntry> sortedMap = new TreeMap<>(md_Dictionary);
            Set set = sortedMap.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                bufferedWriter.write(mentry.getKey()+"#"+mentry.getValue().toString());
                if (iterator.hasNext()) bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*Loads dictionary file from a chosen path into the program's memory and data structures*/
    public static void loadDictionary(String filePath){
        String line;
        try {
            dictionaryFullPath = filePath;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            HashMap<String, DictionaryEntry> newDictionary = new HashMap<>();
            while((line=reader.readLine())!=null){
                String[] parts = line.split("#|:");
                String key = parts[0];
                newDictionary.put(key,new DictionaryEntry(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Double.parseDouble(parts[3]),Integer.parseInt(parts[4])));
            }
            reader.close();

            md_Dictionary.clear();
            md_Dictionary = newDictionary;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void loadPreDictionary(String filePath){
        String line;
        try {
            dictionaryFullPath = filePath;
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            HashMap<String, DictionaryEntry> newDictionary = new HashMap<>();
            while((line=reader.readLine())!=null){
                String[] parts = line.split("#|:");
                String key = parts[0];
                newDictionary.put(key,new DictionaryEntry(Integer.parseInt(parts[1]),Integer.parseInt(parts[2]),Double.parseDouble(parts[3]),Integer.parseInt(parts[4])));
            }
            reader.close();

            preDictionary.clear();
            preDictionary = newDictionary;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getWordIDF(String word){
        DictionaryEntry dEntry = md_Dictionary.get(word);
        if (dEntry==null) return Math.log10(Parse.numberOfDocuments)/Math.log10(2);
        return dEntry.idf;
    }

    public static double getWordPreIDF(String word){
        DictionaryEntry dEntry = preDictionary.get(word);

        //for words in the posting but arent in the dictionary
        if (dEntry==null) return Math.log10(Parse.numberOfDocuments)/Math.log10(2);

        return dEntry.idf;
    }



    //    public static void serializationDictionary(){
//        try
//        {
//            FileOutputStream fos =
//                    new FileOutputStream(path+ "\\"+ "hashmap.ser");
//            ObjectOutputStream oos = new ObjectOutputStream(fos);
//            oos.writeObject(Dictionary.md_Dictionary);
//            oos.close();
//            fos.close();
//        }catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
//    }
//
//    public static void deSerializationDictionary(){
//        HashMap<String, DictionaryEntry > map = null;
//        try
//        {
//            FileInputStream fis = new FileInputStream(path+ "\\"+ "hashmap.ser");
//            ObjectInputStream ois = new ObjectInputStream(fis);
//            map = (HashMap) ois.readObject();
//            ois.close();
//            fis.close();
//        }catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//            return;
//        }catch(ClassNotFoundException c)
//        {
//            System.out.println("Class not found");
//            c.printStackTrace();
//            return;
//        }
//
//        Set set = map.entrySet();
//        Iterator iterator = set.iterator();
//        while(iterator.hasNext()) {
//            Map.Entry mentry = (Map.Entry)iterator.next();
//            System.out.print("key: "+ mentry.getKey() + " & Value: ");
//            System.out.println(mentry.getValue().toString());
//
//        }
//        System.out.println("WITH TOTAL TERMS:");
//        System.out.println(map.size());
//    }

}


