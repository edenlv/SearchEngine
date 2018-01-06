package Model;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class Document implements Serializable{
    public static HashMap<String, Document> documentsCollection = new HashMap<>();

    public HashMap<String, MyPair> hMap;
    public String docID;
    public String mostFrequentTerm;
    public int documentLength;
    public int uniqueTermsCounter;
    public int mostFrequentTermValue;
    public double docVectorSize;
    public String folderName;
    public String title;

    public Document(){
        this.hMap = new HashMap<String, MyPair>();
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
            hMap.get(term).incrementTF();
        } else {
            hMap.put(term, new MyPair(val,this.documentLength));
            this.uniqueTermsCounter++;
        }

        if (val > mostFrequentTermValue) {
            mostFrequentTermValue = val;
            mostFrequentTerm = term;
        }

        this.documentLength++;
    }

    public void addLongTermToDic(String longTerm){
        longTerm = longTerm.toLowerCase();
        longTerm = Parse.StemWord(longTerm);
        int aux = longTerm.indexOf(" ");
        String firstTerm = longTerm.substring(0,aux);

        int val = 1;

        if (hMap.containsKey(longTerm)) {
            hMap.get(longTerm).incrementTF();
        } else {
            hMap.put(longTerm, new MyPair(val,hMap.get(firstTerm).getIdxInDoc()));
            this.uniqueTermsCounter++;
        }

        if (val > mostFrequentTermValue) {
            mostFrequentTermValue = val;
            mostFrequentTerm = longTerm;
        }
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
        this.hMap.clear();
        this.hMap = null;
    }

    public double getWordWeight(String word){
        double idf = Dictionary.getWordPreIDF(word);
        double normalTF = hMap.get(word).getTf()/this.mostFrequentTermValue;
        return normalTF*idf;
    }


    public static void writeCollectionToFile(){

        String filePath = ReadFile.postingsPath+"\\DocumentsCollection" + ((Parse.toStem) ? "_WithStem" : "") + ".txt";
        try {
            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
            writer.writeObject(documentsCollection);
            writer.flush();
            writer.close();
        } catch (IOException e){ e.printStackTrace(); }
    }

    public static void computeAllDocVectorSizes(){
        Iterator<Document> it = documentsCollection.values().iterator();
        while (it.hasNext()){
            Document doc = it.next();
            doc.computeVectorSize();
        }
    }

    public static boolean loadDocumentsFile(){
        try {
            String filePath = ReadFile.postingsPath+"\\DocumentsCollection" + ((Parse.toStem) ? "_WithStem" : "") + ".txt";
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath));
            Document.documentsCollection = (HashMap<String, Document>) input.readObject();
        } catch (Exception e) {return false;}

        return true;
    }

    public static Document get(String docID){return documentsCollection.get(docID);}

    public String getDocID() {
        return docID;
    }

    public String getMostFrequentTerm() {
        return mostFrequentTerm;
    }

    public void setMostFrequentTerm(String mostFrequentTerm) {
        this.mostFrequentTerm = mostFrequentTerm;
    }

    public HashMap<String, MyPair> gethMap() {
        return hMap;
    }

    public void sethMap(HashMap<String, MyPair> hMap) {
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
