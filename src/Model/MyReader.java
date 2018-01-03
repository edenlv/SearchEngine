package Model; /**
 * Created by levye on 14/12/2017.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This is essentially a thin wrapper on top of a BufferedReader... which keeps
 * the last line in memory.
 *
 */
final class MyReader {
    public BufferedReader fbr;

    public String cache;
    public String val;
    public String key;
    public String filePath;


    public MyReader(BufferedReader r, String filePath) throws IOException {
        this.fbr = r;
        this.filePath = filePath;
        reload();
    }

    public void close() throws IOException {
        this.fbr.close();
    }

    public boolean empty() {
        return this.cache == null;
    }

    public void reload() throws IOException {
        this.cache = this.fbr.readLine();
        if (this.cache != null) {
            String[] split = this.cache.split("#");
            this.val = split[1].trim();
            this.key = split[0].trim();
        }
    }

    public void deleteFile(){
        try {
            Files.deleteIfExists(Paths.get(filePath));
        }
        catch (IOException e){e.printStackTrace();}
    }



}
