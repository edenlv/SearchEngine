package View;

import Model.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static Model.Document.documentsCollection;


public class Controller {

    public Button corpusPathBtn;
    public Button postingPathBtn;
    public TextField corpusPathInput;
    public TextField postingPathInput;
    public CheckBox cb_stem;
    public Button btn_showCache;
    public Button btn_showDictionary;
    public Button btn_loadPreDictionary;
    public long programRunTimeInSeconds;
    public Alert waitAlert;

    public void onChooseDirectory(ActionEvent event){
        DirectoryChooser dirChooser = new DirectoryChooser();
        String title;
        dirChooser.setTitle("Choose Your Directory");

        File f = dirChooser.showDialog(null);

        if (null!=f) {
            String filePath = f.getAbsolutePath();

            if (event.getSource() == corpusPathBtn) {
                corpusPathInput.setText(filePath);
                ReadFile.setCorpusPath(filePath);
            } else if (event.getSource() == postingPathBtn) {
                postingPathInput.setText(filePath);
                ReadFile.setPostingsPath(filePath);
            }
        }
    }

    public void onToggleStem(ActionEvent event){
        if (event.getSource()==cb_stem){
            boolean isSelected = cb_stem.isSelected();
            Parse.setStem(isSelected);
        }
    }

    public void onStartIndexing(ActionEvent event){

        if (ReadFile.path == null || ReadFile.postingsPath==null){
            Alert alert = new Alert(Alert.AlertType.ERROR,"Please choose directory paths for the corpus and posting files!");
            alert.show();
        } else {
            runProgram();
        }
    }

    public void runProgram(){
        showWaitMessage(true);

        Indexer.fileCounter = 0;
        Parse.queryParsing = false;
        long start = System.currentTimeMillis();

        ReadFile.start();
        Indexer.mergeSort();
        Dictionary.createDictionaryFromPosting(Indexer.postingFilePath);

        showWaitMessage(false);

        long end = System.currentTimeMillis();
        long runtime_ms = end - start;
        long runtime_sec = runtime_ms / 1000L;
        String runtime_minutes = Parse.round(String.valueOf((runtime_ms/1000.0)/60.0));
        System.out.println("Full program runtime: " + (runtime_ms / 1000.0) + "[sec] or " + runtime_ms + "[ms] or " + runtime_minutes + "[minutes]");
        programRunTimeInSeconds = runtime_sec;

        showDoneMessage();

    }

    public void onResetProgram(ActionEvent event){
        showWaitMessage(true);

        Dictionary.resetDictionary();
        Cache.resetCache();
        if (ReadFile.postingsPath!=null) {
            File postingDir = new File(ReadFile.postingsPath);
            String[] entries = postingDir.list();
            for (String s : entries) {
                File currentFile = new File(postingDir.getPath(), s);
                currentFile.delete();
            }
            postingDir.delete();
        }

        showWaitMessage(false);
    }

    public void onOpenFile(ActionEvent actionEvent) {
        if (actionEvent.getSource()==btn_showDictionary) {
            File file = new File(Dictionary.getDictionaryPath(ReadFile.postingsPath));
            if (!file.exists()) Dictionary.saveDictionary(ReadFile.postingsPath);

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Desktop is not supported!");
            }
        } else if (actionEvent.getSource() == btn_showCache) {
            File file = new File(Cache.getCacheFullPath(ReadFile.postingsPath));
            if (!file.exists()) Cache.saveCache(ReadFile.postingsPath);

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Desktop is not supported!");
            }
        }
    }

    public void onSaveDictionaryAndCache(ActionEvent actionEvent){
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose folder directory");

        File f = dirChooser.showDialog(null);

        if (null!=f) {
            String folderPath = f.getAbsolutePath();

            Dictionary.saveDictionary(folderPath);
            Cache.saveCache(folderPath);
        }

    }

    public void onLoadDictionary(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose dictionary file");

        File f = fileChooser.showOpenDialog(null);
        if (null!=f && f.exists()){
            if (event.getSource()==btn_loadPreDictionary){
                Dictionary.loadPreDictionary(f.getAbsolutePath());
            } else {
                Dictionary.loadDictionary(f.getAbsolutePath());
            }
        }
    }

    public void onLoadCache(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose dictionary file");

        File f = fileChooser.showOpenDialog(null);
        if (null!=f && f.exists()){
            Cache.loadCache(f.getAbsolutePath());
        }
    }

    public void showWaitMessage(boolean toShow){
        if (toShow) {
            waitAlert = new Alert(Alert.AlertType.INFORMATION);
            waitAlert.setTitle("Wait... Program is running!");
            waitAlert.setHeaderText("Wait... Program is running!");
            waitAlert.setContentText("Please wait...");
            waitAlert.show();
        } else {
            waitAlert.close();
        }
    }




    public void showDoneMessage(){
        int numberOfDocuments = documentsCollection.size();
        long indexSize = Indexer.getIndexSizeInBytes();
        long cacheSize = Cache.getSizeInBytes();
        long runtime = this.programRunTimeInSeconds;

        String content = String.format("Number of documents indexed: %d\n\nIndex size: %d [bytes]\n\nCache size: %d [bytes]\n\nFull program runtime: %d [sec]",
                numberOfDocuments, indexSize, cacheSize, runtime);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Program finished!");
        alert.setHeaderText("Indexing Program Runtime Information");
        alert.setContentText(content);
        alert.show();
    }

    public void LoadDictionaryAndCacheFromFolder(ActionEvent event){
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose folder directory");

        File f = dirChooser.showDialog(null);

        if (null!=f) {
            File[] subfiles = f.listFiles(File::isFile);
            boolean foundCache = false, foundDic = false, foundDocCollection = false;
            for (int i=0; i<subfiles.length; i++){
                File file = subfiles[i];

                if (file.getName().startsWith("Cache") && ((Parse.toStem && file.getName().contains("Stem")) || (!Parse.toStem && !file.getName().contains("Stem")))) {
                    Cache.loadCache(file.getAbsolutePath());
                    foundCache = true;
                } else if (file.getName().startsWith("Dictionary") && ((Parse.toStem && file.getName().contains("Stem")) || (!Parse.toStem && !file.getName().contains("Stem")))) {
                    Dictionary.loadDictionary(file.getAbsolutePath());
                    foundDic = true;
                } else if (file.getName().startsWith("Documents") && ((Parse.toStem && file.getName().contains("Stem")) || (!Parse.toStem && !file.getName().contains("Stem")))){
                    Document.loadDocumentsCollection(file.getAbsolutePath());
                    foundDocCollection = true;
                }
            }

            System.out.println("success");

            if (!foundDocCollection || !foundDic){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Couldn't find files! They must be named according to your stemming checkbox and start with \"Dictionary\" or \"Cache\"!");
                alert.show();
            }
        }
    }

    public static void noStopwordsFile(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("You must put a file named stop_words.txt inside the corpus path you've chosen!");
        alert.show();
    }

    public void loadDictionaryAndDocCollection(ActionEvent event){
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose folder directory");

        File f = dirChooser.showDialog(null);

        if (null!=f) {
            File[] subfiles = f.listFiles(File::isFile);
            boolean foundDic = false, foundDocCollection = false;
            for (int i=0; i<subfiles.length; i++){
                File file = subfiles[i];
                if (((Parse.toStem && file.getName().contains("Stem")) || (!Parse.toStem && !file.getName().contains("Stem")))) {
                    if (file.getName().startsWith("Dictionary")) {
                        Dictionary.loadDictionary(file.getAbsolutePath());
                        foundDic = true;
                    } else if (file.getName().startsWith("Documents")) {
                        Document.loadDocumentsCollection(file.getAbsolutePath());
                        foundDocCollection = true;
                    }
                }
            }

            System.out.println("success");

            if (!foundDocCollection || !foundDic){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Couldn't find files! They must be named according to your stemming checkbox and start with \"Dictionary\" or \"DocumentsCollection\"!");
                alert.show();
            }
        }
    }



//    public void test(ActionEvent actionEvent){
//        File f = new File(ReadFile.postingsPath+"\\DocumentsCollection.txt");
//        Document doc = new Document();
//        doc.docVectorSize = 5;
//        doc.mostFrequentTermValue = 3;
//        doc.mostFrequentTerm = "cat";
//        doc.docID = "DOC1";
//        doc.documentLength = 234;
//        doc.uniqueTermsCounter = 5;
//        doc.hMap = null;
//
//        documentsCollection.put("DOC1", doc);
//        Document.
//                writeCollectionToFile();
//    }

    public void test2(ActionEvent actionEvent){
        boolean success = Document.loadDocumentsFile();
        System.out.println(success?"succeeded":"failed");
    }

    public void writeDocCollectionToFile(ActionEvent event){
        //Document.computeAllDocVectorSizes();
        Document.writeCollectionToFile();
        System.out.println("success");
    }

    public void test3(ActionEvent event){
        HashMap<String, MyPair> map = Ranker.getPostingData(24653);
        map.keySet().stream().forEach((str)->{
            System.out.printf("DocID: %s - TF: %d - IndexInDoc: %d", str, map.get(str).getTf(), map.get(str).getIdxInDoc());
        });
        System.out.println("Success");
    }

    public void test4(ActionEvent event){
        ArrayList<String> qry = new ArrayList<String>();
        qry.add("dog cat");
        Searcher.setQueries(qry);
    }






    /*
        public void dictionary(ActionEvent actionEvent){
        HashMap<String, DictionaryEntry> dictionary = Dictionary.md_Dictionary;
        List<String> termList = new LinkedList<>();
        dictionary.entrySet().stream()
                .sorted(Map.Entry.<String, Model.DictionaryEntry>comparingByValue((o1, o2) -> Integer.compare(o1.getSumTF(), o2.getSumTF())).reversed())
                .limit(10000)
                .forEach(stringDictionaryEntryEntry -> termList.add(stringDictionaryEntryEntry.getKey()));
//
//        Cache.writeWord(termList);

        System.out.println("finished");
    }

    public void show10MostFreqWords(ActionEvent actionEvent){
        System.out.println("#########################################################");
        System.out.println("Words sorted by SumTF DESCENDING (yored): ");
        HashMap<String, DictionaryEntry> dictionary = Dictionary.md_Dictionary;
        List<String> termList = new LinkedList<>();
        dictionary.entrySet().stream()
                .sorted(Map.Entry.<String, Model.DictionaryEntry>comparingByValue((o1, o2) -> Integer.compare(o1.getSumTF(), o2.getSumTF())).reversed())
                .limit(10)
                .forEach(stringDictionaryEntryEntry -> termList.add(stringDictionaryEntryEntry.getKey()));

        for (String term : termList){
            System.out.println(term);
        }

        System.out.println("#########################################################");

    }

    public void show10LeastFreqWords(ActionEvent actionEvent){
        System.out.println("#########################################################");
        System.out.println("Words sorted by SumTF ASCENDING (ole): ");
        HashMap<String, DictionaryEntry> dictionary = Dictionary.md_Dictionary;
        List<String> termList = new LinkedList<>();
        dictionary.entrySet().stream()
                .sorted(Map.Entry.<String, Model.DictionaryEntry>comparingByValue((o1, o2) -> Integer.compare(o2.getSumTF(), o1.getSumTF())).reversed())
                .limit(10)
                .forEach(stringDictionaryEntryEntry -> termList.add(stringDictionaryEntryEntry.getKey()));

        for (String term : termList){
            System.out.println(term);
        }

        System.out.println("#########################################################");

    }

    public void show10MostDFWords(ActionEvent actionEvent){
        System.out.println("#########################################################");
        System.out.println("Words sorted by DF DESCEDING (yored):");
            HashMap<String, DictionaryEntry> dictionary = Dictionary.md_Dictionary;
            List<String> termList = new LinkedList<>();
            dictionary.entrySet().stream()
                    .sorted(Map.Entry.<String, Model.DictionaryEntry>comparingByValue((o1, o2) -> Integer.compare(o1.getDF(), o2.getDF())).reversed())
                    .limit(10)
                    .forEach(stringDictionaryEntryEntry -> termList.add(stringDictionaryEntryEntry.getKey()));

            for (String term : termList){
                System.out.println(term);
            }

        System.out.println("#########################################################");

    }

    public void show10LeastDFWords(ActionEvent actionEvent){
        System.out.println("#########################################################");
        System.out.println("Words sorted by DF DESCEDING (ole):");
            HashMap<String, DictionaryEntry> dictionary = Dictionary.md_Dictionary;
            List<String> termList = new LinkedList<>();
            dictionary.entrySet().stream()
                    .sorted(Map.Entry.<String, Model.DictionaryEntry>comparingByValue((o1, o2) -> Integer.compare(o2.getDF(), o1.getDF())).reversed())
                    .limit(10)
                    .forEach(stringDictionaryEntryEntry -> termList.add(stringDictionaryEntryEntry.getKey()));

            for (String term : termList){
                System.out.println(term);
            }

        System.out.println("#########################################################");

    }
     */
}
