package Model;

public class CacheEntry {
    public String term;
    public int postingLine;
    public String data;

    public CacheEntry(String term, int postingLine, String data){
        this.term = term;
        this.postingLine = postingLine;
        this.data = data;
    }

    public CacheEntry(String term, int postingLine){
        this.term = term;
        this.postingLine = postingLine;
    }

    public long getSize(){
        return term.length()+4+data.length();
    }

    public String toString(){
        return this.term + "#" + this.postingLine + "#" + this.data;
    }



}
