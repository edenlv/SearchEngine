package Model;

import java.io.Serializable;

public class MyPair implements Serializable {
    public int df;
    public int idxInDoc;

    public MyPair(){
        this.df = 0;
        this.idxInDoc = 0;
    }

    public MyPair(int df, int idxInDoc) {
        this.df = df;
        this.idxInDoc = idxInDoc;
    }

    public int getDf() {
        return df;
    }

    public void setDf(int df) {
        this.df = df;
    }

    public int getIdxInDoc() {
        return idxInDoc;
    }

    public void setIdxInDoc(int idxInDoc) {
        this.idxInDoc = idxInDoc;
    }

    public void incrementDF(){
        this.df += 1;
    }
}
