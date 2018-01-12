package Model;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

public class Document implements Serializable{
    private static final long serialVersionUID = -8178667157127751118L;
    public static HashMap<String, Document> documentsCollection = new HashMap<>();
    public static double avgDocSize = -1;

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
            MyPair firstTermPair = hMap.get(firstTerm);
            if (firstTermPair==null){
                hMap.put(longTerm,new MyPair(val,this.documentLength++));
            } else {
                hMap.put(longTerm,new MyPair(val,firstTermPair.getIdxInDoc()));
            }
//            hMap.put(longTerm, new MyPair(val, hMap.get(firstTerm).getIdxInDoc()));
//               this.uniqueTermsCounter++;
        }
        if (val > mostFrequentTermValue) {
            mostFrequentTermValue = val;
            mostFrequentTerm = longTerm;
        }
        Parse.uppercaseLongTerm.setLength(0);
    }

    public static long getNumberOfDocuments(){
        return documentsCollection.size();
    }

    public static void reset(){
        documentsCollection.clear();
        avgDocSize = -1;
    }

//    public void computeVectorSize(){
//        double sumOfSquaredWeights = 0;
//        Iterator<String> it = hMap.keySet().iterator();
//
//        while (it.hasNext()){
//            sumOfSquaredWeights+=Math.pow(getWordWeight(it.next()),2);
//        }
//
//        this.docVectorSize = Math.sqrt(sumOfSquaredWeights);
//        this.hMap.clear();
//        this.hMap = null;
//    }

//    public double getWordWeight(String word){
//        double idf = Dictionary.getWordPreIDF(word);
//        double normalTF = hMap.get(word).getTf()/this.mostFrequentTermValue;
//        return normalTF*idf;
//    }


//    public static void writeCollectionToFile(){
//
//        String filePath = ReadFile.postingsPath+"\\DocumentsCollection" + ((Parse.toStem) ? "_WithStem" : "") + ".txt";
//        try {
//            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
//            writer.writeObject(documentsCollection);
//            writer.flush();
//            writer.close();
//        } catch (IOException e){ e.printStackTrace(); }
//    }
//
//    public static void computeAllDocVectorSizes(){
//        Iterator<Document> it = documentsCollection.values().iterator();
//        while (it.hasNext()){
//            Document doc = it.next();
//            doc.computeVectorSize();
//        }
//    }

    public static boolean loadDocumentsFile(){
        try {
            String filePath = ReadFile.postingsPath+"\\DocumentsCollection" + ((Parse.toStem) ? "_WithStem" : "") + ".txt";
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath));
            Document.documentsCollection = (HashMap<String, Document>) input.readObject();
            input.close();
        } catch (Exception e) {return false;}

        return true;
    }

    public static void loadDocumentsCollection(String path){
        try {
            String filePath = path;
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath));
            Document.documentsCollection = (HashMap<String, Document>) input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static double getAvgDocSize(){
        if (avgDocSize==-1) {
            double ans = 0;
            Iterator it = Document.documentsCollection.values().iterator();
            while (it.hasNext()) {
                Document doc = (Document) it.next();
                ans += doc.documentLength;
            }
            ans = ans / Document.documentsCollection.size();

            avgDocSize = ans;
        }

        return avgDocSize;
    }

}
