package Model;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.ArrayList;

public class ReadFile {
    public static boolean debug = true;

    public static String path = null;
    public static String postingsPath = null;
    public static ArrayList<DocPair> docBuffer = new ArrayList<>();
    public static long globalTime = System.currentTimeMillis();
    public static String currentFileName = null;

    /*Starts the process given a corpus path and a posting folder path*/
    public static void start() {
        int cnt = 0;
        File[] dirs = new File(path).listFiles(File::isDirectory);
        ArrayList<File> files = new ArrayList<>();

        for (int i = 0; i < dirs.length; i++) {
            File[] subfiles = dirs[i].listFiles(File::isFile);
            for (int j = 0; j < subfiles.length; j++) files.add(subfiles[j]);
        }

        for (int i = 0; i < files.size(); i++) {
            try {
                currentFileName = files.get(i).getName();
                byte[] lines = Files.readAllBytes(files.get(i).toPath());
                String text = new String(lines);

                String[] res = text.split("<DOC>");
                for (int j = 0; j < res.length; j++) {
                    if (!res[j].contains("<TEXT>")) continue; //if no <TEXT> tag - ignore
                    pushDoc(res[j]);
                }

                if ((i+1) % 10 == 0 || i == files.size() - 1) {
                    flushDocumentsChunk();
                    if (ReadFile.debug) System.out.println("Processed file chunk number " + cnt + " in: " + (System.currentTimeMillis() - ReadFile.globalTime) + " ms");
                    ReadFile.globalTime = System.currentTimeMillis();
                    cnt++;
                }

            } catch (IOException ioException) {
                System.out.println("Exception thrown!");
            }
        }

        flushDocumentsChunk();


        System.out.println("Number of files: " + files.size());
        System.out.println("Number of documents: " + Document.getNumberOfDocuments());
    }


    private static void pushDoc(String doc) {
        docBuffer.add(new DocPair(doc,currentFileName));
    }

    private static void flushDocumentsChunk() {
        if (docBuffer.isEmpty()) return;

        Parse.parse();

        docBuffer.clear();
    }

    public static void setCorpusPath(String f){
        path = new String(f);
    }

    public static void setPostingsPath(String f){
        postingsPath = new String(f);
    }


}
