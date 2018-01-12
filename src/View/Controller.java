package View;

import Model.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static Model.Document.*;
import static Model.Parse.*;


public class Controller {

    public static boolean extendedQuery = false;
    public static boolean docSummary = false;
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
    public Button btn_qryBrowse;
    public TextField queryFileInput;
    private String queryFilePath;
    public Button btn_saveQueryFileResults;
    public Button btn_saveResultsSingle;
    public CheckBox cb_extendedQuery;
    public CheckBox cb_docSummary;
    public Button btn_qRun;
    public Button btn_qFileRun;

    public static LinkedList<String> singleQueryTREC = null;
    public static LinkedList<String> queryFileTREC = null;
    public static String lastSavedResultsFile = null;
    public static LinkedList<Ranker> lastRankers = null;

    public TextField input_query;
    public TextField txt_cos;
    public TextField txt_bm;

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
        boolean isSelected = ((CheckBox)event.getSource()).isSelected();

        if (event.getSource()==cb_stem){
            Parse.setStem(isSelected);
        } else if (event.getSource()==cb_docSummary){
            Controller.docSummary = isSelected;
        } else if (event.getSource() == cb_extendedQuery){
            Controller.extendedQuery = isSelected;
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

//    public void onResetProgram(ActionEvent event){
//        showWaitMessage(true);
//
//        Dictionary.resetDictionary();
//        Cache.resetCache();
//        if (ReadFile.postingsPath!=null) {
//            File postingDir = new File(ReadFile.postingsPath);
//            String[] entries = postingDir.list();
//            for (String s : entries) {
//                File currentFile = new File(postingDir.getPath(), s);
//                currentFile.delete();
//            }
//            postingDir.delete();
//        }
//
//        showWaitMessage(false);
//    }

//    public void onOpenFile(ActionEvent actionEvent) {
//        if (actionEvent.getSource()==btn_showDictionary) {
//            File file = new File(Dictionary.getDictionaryPath(ReadFile.postingsPath));
//            if (!file.exists()) Dictionary.saveDictionary(ReadFile.postingsPath);
//
//            if (Desktop.isDesktopSupported()) {
//                try {
//                    Desktop.getDesktop().open(file);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            } else {
//                System.out.println("Desktop is not supported!");
//            }
//        } else if (actionEvent.getSource() == btn_showCache) {
//            File file = new File(Cache.getCacheFullPath(ReadFile.postingsPath));
//            if (!file.exists()) Cache.saveCache(ReadFile.postingsPath);
//
//            if (Desktop.isDesktopSupported()) {
//                try {
//                    Desktop.getDesktop().open(file);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            } else {
//                System.out.println("Desktop is not supported!");
//            }
//        }
//    }

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

//    public void onLoadDictionary(ActionEvent event){
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Choose dictionary file");
//
//        File f = fileChooser.showOpenDialog(null);
//        if (null!=f && f.exists()){
//            if (event.getSource()==btn_loadPreDictionary){
//                Dictionary.loadPreDictionary(f.getAbsolutePath());
//            } else {
//                Dictionary.loadDictionary(f.getAbsolutePath());
//            }
//        }
//    }
//
//    public void onLoadCache(ActionEvent event){
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Choose dictionary file");
//
//        File f = fileChooser.showOpenDialog(null);
//        if (null!=f && f.exists()){
//            Cache.loadCache(f.getAbsolutePath());
//        }
//    }

    public void showWaitMessage(boolean toShow){
        if (toShow) {
            waitAlert = new Alert(Alert.AlertType.INFORMATION);
            waitAlert.setTitle("Wait... Program is running!");
            waitAlert.setHeaderText("Wait... Program is running!");
            waitAlert.setContentText("Please wait...");
            if (!waitAlert.isShowing()) waitAlert.show();
        } else {
            if (waitAlert.isShowing()) waitAlert.close();
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
            showWaitMessage(true);
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

            System.out.println("Success: Loaded dictionary, cache and documents collection!");

            if (!foundDocCollection || !foundDic){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Couldn't find files! They must be named according to your stemming checkbox and start with \"Dictionary\" or \"DocumentsCollection\"!");
                alert.show();
            }
        }

    }

    public void chooseQueryFilePath(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose queries file");

        File f = fileChooser.showOpenDialog(null);
        if (null!=f && f.exists()){
            queryFileInput.setText(f.getAbsolutePath());
            queryFilePath = f.getAbsolutePath();
        }
    }

    public void resetProgram(ActionEvent event){
        Dictionary.resetDictionary();
        Document.reset();
        Searcher.postingLinesCache.clear();
        Searcher.Queries.clear();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Reset program successfully");
        alert.show();
    }

    public void run(ActionEvent event){
        boolean singleQuery = event.getSource()==btn_qRun;

        boolean isOK = checkIntegrity(singleQuery);
        if (isOK){
            long start = System.currentTimeMillis();
            showWaitMessage(true);

            LinkedList<PreQuery> preQueries = new LinkedList<PreQuery>();
            if (singleQuery){
                PreQuery preQuery = new PreQuery(input_query.getText(), 0, "");
                preQueries.add(preQuery);
            } else {
                preQueries.addAll(parseQueryFile(queryFilePath));
            }

            LinkedList<Ranker> rankers = Searcher.setQueries(preQueries);
            LinkedList<String> resultLines = new LinkedList<>();

            for (int i=0; i<rankers.size(); i++){
                rankers.get(i).runRanking();
                resultLines.addAll(rankers.get(i).toArrayString());
            }
            long end = System.currentTimeMillis();

            long runTime = end - start;
            lastRankers = rankers;

            onQueryFinished(rankers, runTime);

            System.out.println("Success: queries complete!");
            showWaitMessage(false);
        }
    }


    public boolean checkIntegrity(boolean isSingleQuery){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Parse._initParser();
        HashSet<String> sw = _initStopwordsTable();
        if (sw == null) noStopwordsFile();

        String corpusPath = ReadFile.path;
        if (corpusPath == null && docSummary) {
            alert.setContentText("You must choose a corpus path if you want document summary!");
            alert.show();
            return false;
        }

        if (docSummary && extendedQuery){
            alert.setContentText("Can't do extended query and document search at once! Pick one!");
            alert.show();
            return false;
        }

        if (!isSingleQuery && (queryFilePath==null || queryFilePath.equals(""))){
            alert.setContentText("You must choose a query file path before running queries from file");
            alert.show();
            return false;
        }

        if (isSingleQuery && (input_query.getText().equals("")||input_query.getText()==null)){
            alert.setContentText("Empty query!");
            alert.show();
            return false;
        }

        boolean hasFiles = false;
        String postingFilepath = Indexer.getPostingFilePath();
        if (!new File(postingFilepath).exists()) {
            alert.setContentText("Couldn't find posting file! Make sure you have PostingFile_WithStem.txt or PostingFile.txt inside posting folder chosen!");
            alert.show();
            return false;
        }

        if (Dictionary.md_Dictionary.size()==0 || Document.documentsCollection.size()==0){
            alert.setContentText("Load dictionary and documents collection first before running queries!");
            alert.show();
            return false;
        }

        return true;
        }

    public void writeQueryResultsToFile(LinkedList<String> lines) {
        try {
            FileChooser fileChooser = new FileChooser();
            File f = fileChooser.showSaveDialog(null);

            if (f != null) {
                if (f.exists()) f.delete();
                f.createNewFile();

                PrintWriter writer = new PrintWriter(new FileWriter(f));
                for (int i=0; i<lines.size(); i++){
                    writer.println(lines.get(i));
                }
                writer.flush();
                writer.close();

                lastSavedResultsFile = f.getAbsolutePath();

            }

        } catch (IOException e){
            System.err.println("couldnt save results file");
        }
    }

    public void saveResults(ActionEvent event){

        if (lastRankers == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Run a query first! Nothing to save!");
            alert.show();
            return;
        }

        LinkedList<String> lines = new LinkedList<>();
        for (int i=0; i<lastRankers.size(); i++){
            lines.addAll(lastRankers.get(i).toArrayString());
        }

        writeQueryResultsToFile(lines);
    }

    public void onQueryFinished(LinkedList<Ranker> rankers, long time){
        ArrayList<String> lines = new ArrayList<>();
        double minutes = (time/1000)/60;

        lines.add("Total runtime for queries: " + minutes + " minutes.");
        lines.add("-----------------------------------------------------------------");

        for(int i=0; i<rankers.size(); i++){
            Ranker currRanker = rankers.get(i);
            lines.add("Query number: " + currRanker.query.queryNumber);
            lines.add("Number of relevant documents retrieved for query: " + currRanker.getResult50().size());
            lines.add("Results:");
            lines.addAll(currRanker.prettifyResult());
            lines.add("-----------------------------------------------------------------");
        }

        try{
            String filePath = ReadFile.postingsPath+"\\last_results_pretty.txt";
            File f = new File (filePath);
            if (f.exists()) f.delete();
            f.createNewFile();

            PrintWriter writer = new PrintWriter(new FileWriter(f));
            for (int j=0; j<lines.size(); j++){
                writer.println(lines.get(j));
            }
            writer.flush();
            writer.close();

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Desktop is not supported!");
            }

        } catch (Exception e){
            System.err.println("couldnt open query finished file");
        }
    }



    public LinkedList<PreQuery> parseQueryFile(String filePath){
        LinkedList<PreQuery> res = new LinkedList<>();

        File f = new File(filePath);
        byte[] aux = null;
        try {
            aux = Files.readAllBytes(f.toPath());
        } catch (Exception e){e.printStackTrace();}

        String entireFile = new String(aux);

        String[] queries = entireFile.split("<top>");

        for (int i=0; i<queries.length; i++){
            if (queries[i].trim().equals("")) continue;

            PreQuery preQuery = new PreQuery();

            String[] lines = queries[i].split("\n");
            for (int j=0; j<lines.length; j++){
                if (lines[j].contains("<title>")) preQuery.queryString = lines[j].replace("<title>","").trim();
                if (lines[j].contains("<num>")) {
                    String str = lines[j].replace("<num>","").replace("Number:","").trim();
                    try{
                        preQuery.queryNumber = Integer.parseInt(str);
                    } catch (NumberFormatException e) {
                        System.err.println("Couldnt parse query number");
                    }
                }
//                if (lines[j].contains("<description"))
            }

            res.add(preQuery);

        }

        return res;
    }




//    public void writeResultsToFile(List<String> lines, String filePath){
//        try {
//            File f = new File(filePath);
//            if (!f.exists()) f.createNewFile();
//
//            PrintWriter writer = new PrintWriter(new FileWriter(f));
//            for (int i=0; i<lines.size(); i++){
//                writer.println(lines.get(i));
//            }
//            writer.flush();
//            writer.close();
//        } catch (Exception e) {
//            System.err.println("couldnt write results to file");
//        }
//    }


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
