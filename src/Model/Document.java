package Model;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Document implements Serializable{
    public static HashMap<String, Document> documentsCollection = new HashMap<>();

    public String docID;
    public String mostFrequentTerm;
    public HashMap<String, Integer> hMap;
    public int documentLength;
    public int uniqueTermsCounter;
    public int mostFrequentTermValue;
    public double docVectorSize;

    public Document(){
        this.hMap = new HashMap<String,Integer>();
        mostFrequentTermValue = 0;
        documentLength = 0;
        uniqueTermsCounter = 0;
    }

    public void setDocID(String docID){
        this.docID = docID;
    }

    //adds the term to the document's own dictionary
    public void addToDic(String term) {
        term = term.toLowerCase();
        if (term == "" || term.length() == 0 || Parse.stopWords.contains(term)) return;

        term = Parse.StemWord(term);
        int val = 1;

        if (hMap.containsKey(term)) {
            val = hMap.get(term) + 1;
            hMap.put(term, val);
        } else {
            hMap.put(term, val);
            this.uniqueTermsCounter++;
        }

        if (val > mostFrequentTermValue) {
            mostFrequentTermValue = val;
            mostFrequentTerm = term;
        }

        this.documentLength++;
    }

    public static long getNumberOfDocuments(){
        return documentsCollection.size();
    }

    public void computeVectorSize(){
        double sumOfSquaredWeights = 0;
        Iterator<String> it = hMap.keySet().iterator();

        while (it.hasNext()){
            sumOfSquaredWeights+=Math.pow(getWordWeight(it.next()),2);
        }

        this.docVectorSize = Math.sqrt(sumOfSquaredWeights);
    }

    public double getWordWeight(String word){
        double idf = Dictionary.getWordIDF(word);
        double normalTF = hMap.get(word)/this.mostFrequentTermValue;
        return normalTF*idf;
    }


    public static void writeCollectionToFile(){

        String filePath = ReadFile.postingsPath+"\\DocumentsCollection" + ((Parse.toStem) ? "_WithStem" : "") + ".txt";
        try {
            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
            writer.writeObject(documentsCollection);
        } catch (IOException e){ e.printStackTrace(); }
    }

    public static void computeAllDocVectorSizes(){
        Iterator<Document> it = documentsCollection.values().iterator();
        while (it.hasNext()){
            Document doc = it.next();
            doc.computeVectorSize();
        }
    }

    public String getDocID() {
        return docID;
    }

    public String getMostFrequentTerm() {
        return mostFrequentTerm;
    }

    public void setMostFrequentTerm(String mostFrequentTerm) {
        this.mostFrequentTerm = mostFrequentTerm;
    }

    public HashMap<String, Integer> gethMap() {
        return hMap;
    }

    public void sethMap(HashMap<String, Integer> hMap) {
        this.hMap = hMap;
    }

    public int getDocumentLength() {
        return documentLength;
    }

    public void setDocumentLength(int documentLength) {
        this.documentLength = documentLength;
    }

    public int getUniqueTermsCounter() {
        return uniqueTermsCounter;
    }

    public void setUniqueTermsCounter(int uniqueTermsCounter) {
        this.uniqueTermsCounter = uniqueTermsCounter;
    }

    public int getMostFrequentTermValue() {
        return mostFrequentTermValue;
    }

    public void setMostFrequentTermValue(int mostFrequentTermValue) {
        this.mostFrequentTermValue = mostFrequentTermValue;
    }

    public double getDocVectorSize() {
        return docVectorSize;
    }

    public void setDocVectorSize(double docVectorSize) {
        this.docVectorSize = docVectorSize;
    }
}
