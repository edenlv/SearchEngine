package Model;

/**
 * Created by levye on 15/12/2017.
 */
public class DictionaryEntry{
    public int df;
    public int sumTF;
    public Double idf;
    public int postingLine;
    public boolean isCached;

    public DictionaryEntry(int df, int sumTF, int postingLine){
        this.df = df;
        this.sumTF = sumTF;
        this.postingLine = postingLine;
        isCached = false;
        this.idf = Math.log10(Parse.numberOfDocuments/df)/Math.log10(2);
    }


    public DictionaryEntry(int df, int sumTF, double idf, int postingLine){
        this.df = df;
        this.sumTF = sumTF;
        this.postingLine = postingLine;
        isCached = false;
        this.idf = idf;
    }

    @Override
    public String toString(){
        return (this.df +":" + this.sumTF + ":" + this.idf + ":" + this.postingLine);
    }

}