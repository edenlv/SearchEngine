package Model;

import java.io.Serializable;

public class MyPair implements Serializable {
    public int tf;
    public int idxInDoc;

    public MyPair(){
        this.tf = 0;
        this.idxInDoc = 0;
    }

    public MyPair(int tf, int idxInDoc) {
        this.tf = tf;
        this.idxInDoc = idxInDoc;
    }

    public int getTf() {
        return tf;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public int getIdxInDoc() {
        return idxInDoc;
    }

    public void setIdxInDoc(int idxInDoc) {
        this.idxInDoc = idxInDoc;
    }

    public void incrementTF(){
        this.tf += 1;
    }
}
