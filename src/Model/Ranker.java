package Model;

public class Ranker {

    public static double getQueryVectorSize(String query){
        int queryLength = query.split(" ").length;
        double vectorSize = Math.sqrt(queryLength);
        return vectorSize;
    }

//    public static double getDocum

}
