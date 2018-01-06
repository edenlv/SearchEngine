package Model;

import java.util.ArrayList;
import java.util.Comparator;

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

    public static void main (String[] args){
        ArrayList<Integer> list = new ArrayList<>();
        list.add(5);
        list.add(1);
        list.add(9);
        list.add(4);
        list.add(2);
        list.add(8);

        list.stream().sorted().forEach(System.out::println);
    }


}
