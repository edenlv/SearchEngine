package Model;

import View.Controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.util.*;

public class Parse {

    public static ArrayList<Document> parsedDocs = new ArrayList<>();
    public static boolean toStem = false;
    public static boolean queryParsing = true;
    public static final Stemmer STEMMER = new Stemmer();
    public static HashSet<Character> whiteSpaces = _initWhiteSpaces();
    public static final HashSet<String> stopWords = _initStopwordsTable();
    public static final Hashtable<String, String> md_Months = _initMonthsTable();
    public static StringBuilder uppercaseLongTerm = new StringBuilder();
    public static int consecutiveCapitals = 0;
    public static Hashtable<String, String> stemCache = new Hashtable<>();
    public static int numberOfDocuments = 0;

    public static enum ContentType { DOUBLE, INTEGER, FIRSTUPPER, DAYTH, PERCENT, AB, NONE }
    public static ContentType currentType = ContentType.AB;


    public static void parse(){
        parsedDocs.clear();
        ArrayList<DocPair> documents = ReadFile.docBuffer;

        for (int r=0; r<documents.size(); r++){

            String unparsedDoc = documents.get(r).unparsedText;

            Document docObj = createDoc(documents.get(r));
            String unparsedText = unparsedDoc;
            if (!queryParsing) {
                unparsedText = unparsedDoc.split("<TEXT>")[1].split("</TEXT>")[0].trim();
            }
            unparsedText = unparsedText.replaceAll("\\<[^>]*>","");

            if (unparsedText.equals("")) continue;
            numberOfDocuments++;
            String[] unparsedWords = unparsedText.split("\\s+|-");

            consecutiveCapitals = 0;

            for (int i=0; i<unparsedWords.length; i++){
                String word = unparsedWords[i];
                String cleanWord = cleanWord(word);
                String lowercase_cleanWord = cleanWord.toLowerCase();

                if (cleanWord=="" || cleanWord.length()==0 || stopWords.contains(lowercase_cleanWord)){
                    flushUppercaseString(docObj);
                    continue;
                }

                String term = cleanWord;//in case no rule applies

                while (true) {

                    ContentType firstWordType = currentType;
                    if (firstWordType==ContentType.NONE){
                        break;
                    }
                    if (firstWordType==ContentType.PERCENT){
                        String num = cleanWord.substring(0,cleanWord.length()-1);
                        if (num.indexOf(".")!=-1) num = round(num);
                        term = num + " percent";
                        break;
                    }

                    if (firstWordType==ContentType.DOUBLE){
                        String num = round(cleanWord);

                        //6.21 percent
                        //6.21 percentage
                        if (i+1<unparsedWords.length){
                            String secondCleanWord = cleanWord(unparsedWords[i+1]);

                            if (secondCleanWord.intern()=="percent".intern() || secondCleanWord.intern()=="percentage".intern()){
                                term = num + " percent";
                                i++;
                                break;
                            }

                            //235.231 meters -> 235.23m
                            if (secondCleanWord.intern()=="meters".intern()){
                                term = num + "m";
                                i++;
                                break;
                            }
                            //2526.765 kilometers -> 25260.77m
                            if (secondCleanWord.intern()=="kilometers".intern()){
                                num = multiplyDoubleBy1000(num);
                                term = num + "m"; //multiplies by 10
                                i++;
                                break;
                            }

                        }

                        //6.21 - regular double
                        term = num;
                        break;
                    }

                    if (firstWordType==ContentType.INTEGER){
                        //6 percent
                        //6 percentage
                        if (i+1<unparsedWords.length){
                            String secondCleanWord = cleanWord(unparsedWords[i+1]);

                            if (secondCleanWord.intern()=="percent".intern() || secondCleanWord.intern()=="percentage".intern()){
                                term = cleanWord + " percent";
                                i++;
                                break;
                            }

                            //346 meters -> 346m
                            if (secondCleanWord.intern()=="meters".intern()){
                                term = cleanWord + "m";
                                i++;
                                break;
                            }
                            //2526 kilometers -> 25260m
                            if (secondCleanWord.intern()=="kilometers".intern()){
                                term = cleanWord + "000m"; //multiplies by 10
                                i++;
                                break;
                            }

                            //12 June
                            //12 June 2013
                            if (cleanWord.length()<3) {
                                String month = md_Months.get(secondCleanWord);
                                if (month != null) {
                                    if (i + 2 < unparsedWords.length) {
                                        String thirdCleanWord = cleanWord(unparsedWords[i + 2]);

                                        if (Parse.currentType == ContentType.INTEGER && (thirdCleanWord.length() == 4 || thirdCleanWord.length() == 2)) {
                                            if (thirdCleanWord.length()==2){
                                                if (thirdCleanWord.charAt(0) < '3') thirdCleanWord = "19"+thirdCleanWord;
                                                thirdCleanWord = "20"+thirdCleanWord;
                                            }
                                            if (cleanWord.length()<2) cleanWord = "0"+cleanWord;
                                            term = cleanWord+"/"+month+"/"+thirdCleanWord;
                                            i+=2;
                                            break;
                                        }
                                    }
                                    if (cleanWord.length()<2) cleanWord="0"+cleanWord;
                                    term = cleanWord+"/"+month;
                                    i++;
                                    break;
                                }
                            }
                        }

                        //92345 - regular number
                        term = cleanWord;
                        break;
                    }

                    if (firstWordType==ContentType.DAYTH){
                        if (i+2<unparsedWords.length){
                            String secondCleanWord = cleanWord(unparsedWords[i+1]);

//                            if (secondCleanWord=="" || secondCleanWord.length()==0 || stopWords.contains(secondCleanWord.toLowerCase()))
//                                continue;

                            String month = md_Months.get(secondCleanWord);
                            if (Parse.currentType==ContentType.FIRSTUPPER && month!=null){
                                String thirdCleanWord = cleanWord(unparsedWords[i+2]);

                                if (Parse.currentType==ContentType.INTEGER && thirdCleanWord.length()==4){
                                    term = cleanWord.substring(0,cleanWord.length()-2) + "/" + month + "/" + thirdCleanWord;
                                    i+=2;
                                    break;
                                }
                            }
                        }
                    }

                    if (firstWordType==ContentType.FIRSTUPPER){
                        String month = md_Months.get(cleanWord);
                        if (month!=null) {
                            if (i+1<unparsedWords.length){
                                String secondCleanWord = cleanWord(unparsedWords[i+1]);

//                                if (secondCleanWord=="" || secondCleanWord.length()==0 || stopWords.contains(secondCleanWord.toLowerCase()))
//                                    continue;

                                if (Parse.currentType==ContentType.INTEGER){
                                    if (secondCleanWord.length()!=4){
                                        if (i+2<unparsedWords.length){
                                            String thirdCleanWord = cleanWord(unparsedWords[i+2]);

//                                            if (thirdCleanWord=="" || thirdCleanWord.length()==0 || stopWords.contains(thirdCleanWord.toLowerCase())) {
//                                                i++;
//                                                break;
//                                            }

                                            if (Parse.currentType==ContentType.INTEGER && thirdCleanWord.length()==4){
                                                //March 14 2002 -> 14/03/2002
                                                if (secondCleanWord.length()==1) secondCleanWord = "0" + secondCleanWord;
                                                term = secondCleanWord+"/"+month+"/"+thirdCleanWord;
                                                i+=2;
                                                break;
                                            }
                                        }

                                        //January 7
                                        if (secondCleanWord.length()==1) secondCleanWord = "0" + secondCleanWord;
                                        term = secondCleanWord+"/"+month;
                                        i+=2;
                                        break;

                                    } else {
                                        //April 2003 -> 04/2003
                                        term = month+"/"+secondCleanWord;
                                        i++;
                                        break;
                                    }
                                }
                            }
                            //ex: "we did it in March.\eof"
                            term = cleanWord;
                            break;
                        } else { //first uppercase letter & its not a month...
                            term = cleanWord;
                            consecutiveCapitals++;
                            if (uppercaseLongTerm.length()>0) uppercaseLongTerm.append(" ");
                            uppercaseLongTerm.append(cleanWord);
                            break;
                        }
                    }

                    break;
                }


                if ((!Character.isUpperCase(term.charAt(0)) && consecutiveCapitals>1)
                        || (i+1>=unparsedWords.length && uppercaseLongTerm.length()>0 && consecutiveCapitals>1)){
                    flushUppercaseString(docObj);
                }
                docObj.addToDic(term);
            }

            parsedDocs.add(docObj);
        }


        if (!queryParsing) Indexer.createDicFromParsedDocs();
    }


    public static void flushUppercaseString(Document docObj){
        if (consecutiveCapitals<2 || uppercaseLongTerm.length()==0) return;
        docObj.addLongTermToDic(uppercaseLongTerm.toString());
        uppercaseLongTerm.setLength(0);
        consecutiveCapitals=0;
    }

    public static Document createDoc(DocPair dPair){
        Document doc = new Document();
        if (!queryParsing) {
            String[] aux = dPair.unparsedText.split("<DOCNO>")[1].split("</DOCNO>");
            doc.folderName = dPair.docFolderName;
            String docID = aux[0].trim();

            doc.setDocID(docID);

            if (docID.startsWith("LA")) {
                String hlText = dPair.unparsedText.split("<HEADLINE>")[1].split("</HEADLINE>")[0];
                hlText = hlText.replaceAll("<P>|</P>", "");

            } else if (docID.startsWith("FT")) {
                String hlText = dPair.unparsedText.split("<HEADLINE>")[1].split("</HEADLINE>")[0];
                hlText = hlText.replace("FT", "");

            } else if (docID.startsWith("FB")) {
                String hlText = dPair.unparsedText.split("<TI>")[1].split("</TI>")[0];
            }
        }
        return doc;
    }

    public static String cleanWord(String word){
        if (word.startsWith("<")) return "";

        StringBuilder stringBuilder = new StringBuilder();
        boolean isNumber = true;
        boolean isAB = true;
        boolean firstUpper = false;
        int numOfDots = 0;

        for (int i=0; i<word.length(); i++){
            char c = word.charAt(i);
            if (whiteSpaces.contains(c) || (i==word.length()-1 && c == '.')) continue;
            if (isAB && Character.isDigit(c)) isAB = false;
            if (c=='.') numOfDots++;
            if (isNumber && (numOfDots>1 || (!Character.isDigit(c) && c!= '.'))) isNumber = false;
            if (!firstUpper && stringBuilder.length()==0 && Character.isUpperCase(c)) firstUpper = true;
            stringBuilder.append(c);
        }

        String cleanResult = stringBuilder.toString();
        if (cleanResult.length()==1){
            return "".intern();
        }

        if (isNumber) {
            if (numOfDots>0) currentType = ContentType.DOUBLE;
            else currentType = ContentType.INTEGER;
        } else if (cleanResult.endsWith("%") && numOfDots<2){
            currentType = ContentType.PERCENT;
        } else if (!isNumber && !isAB) {
            if (cleanResult.endsWith("th") || cleanResult.endsWith("st") || cleanResult.endsWith("nd") || cleanResult.endsWith("rd")){
                currentType = ContentType.DAYTH;
            } else if (firstUpper) {
                currentType = ContentType.FIRSTUPPER;
            } else currentType = ContentType.NONE;
        } else {
            if (firstUpper) currentType = ContentType.FIRSTUPPER;
            else currentType = ContentType.AB;
        }

        if (numOfDots>0 && currentType!=ContentType.DOUBLE && currentType!=ContentType.PERCENT){
            cleanResult = cleanResult.replace(".", "");
        }
        return cleanResult;
    }

    public static String multiplyDoubleBy1000(String num){
        if (null==num) return "";
        int dotIndex = num.indexOf(".");
        if (dotIndex==-1) return "";

        if (dotIndex==num.length()-3){
            return num.replace(".", "")+"0";
        }
        else return num.replace(".","")+"00";
    }


    public static String StemWord(String word){
        if (toStem){
            String stemFromCache = stemCache.get(word);
            if (stemFromCache!=null) return stemFromCache;
            String newStem = STEMMER.stripAffixes(word);
            stemCache.put(word, newStem);
            return newStem;
        }
        return word;
    }

    //aux method for isNumber - rounds to 2 decimals
    public static String round(String strVal) {
        try {
            double value = Double.parseDouble(strVal);
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return String.valueOf(bd.doubleValue());
        } catch (NumberFormatException e) {
            return strVal;
        }
    }

    public static Hashtable<String, String> _initMonthsTable() {
        Hashtable<String,String> myTable = new Hashtable<String,String>();
        myTable.put("January", "01");
        myTable.put("JANUARY", "01");
        myTable.put("Jan", "01");
        myTable.put("JAN", "01");
        myTable.put("February", "02");
        myTable.put("FEBRUARY", "02");
        myTable.put("Feb", "02");
        myTable.put("FEB", "02");
        myTable.put("March", "03");
        myTable.put("MARCH", "03");
        myTable.put("Mar", "03");
        myTable.put("MAR", "03");
        myTable.put("April", "04");
        myTable.put("APRIL", "04");
        myTable.put("Apr", "04");
        myTable.put("APR", "04");
        myTable.put("May", "05");
        myTable.put("MAY", "05");
        myTable.put("June", "06");
        myTable.put("JUNE", "06");
        myTable.put("Jun", "06");
        myTable.put("JUN", "06");
        myTable.put("July", "07");
        myTable.put("JULY", "07");
        myTable.put("Jul", "07");
        myTable.put("JUL", "07");
        myTable.put("August", "08");
        myTable.put("AUGUST", "08");
        myTable.put("Aug", "08");
        myTable.put("AUG", "08");
        myTable.put("September", "09");
        myTable.put("SEPTEMBER", "09");
        myTable.put("Sep", "09");
        myTable.put("SEP", "09");
        myTable.put("October", "10");
        myTable.put("OCTOBER", "10");
        myTable.put("Oct", "10");
        myTable.put("OCT", "10");
        myTable.put("November", "11");
        myTable.put("NOVEMBER", "11");
        myTable.put("Nov", "11");
        myTable.put("NOV", "11");
        myTable.put("December", "12");
        myTable.put("DECEMBER", "12");
        myTable.put("Dec", "12");
        myTable.put("DEC", "12");
        return myTable;
    }


    public static HashSet<String> _initStopwordsTable(){
        HashSet<String> stopWords = new HashSet<>();
        long start = System.currentTimeMillis();
        File swFile = new File(ReadFile.path+"\\stop_words.txt");
        if (swFile != null && swFile.exists() && swFile.isFile()){
            try{
                String fileContent = new String(Files.readAllBytes(swFile.toPath()));
                String[] sw = fileContent.split("\r\n");
                for (int i=0; i<sw.length; i++)
                    sw[i] = sw[i].replace("'", "");

                stopWords.addAll(Arrays.asList(sw));
            } catch (IOException ioException){
                System.out.println("exception");
            }
        } else {
            if (!queryParsing) View.Controller.noStopwordsFile();
        }
        System.out.println("Loaded stop words into HashSet in " + (System.currentTimeMillis()-start) +"[ms]");
        return stopWords;
    }


    public static HashSet<Character> _initWhiteSpaces(){
        Character[] whiteSpaces = { '\'', '�', '_', ' ', '*', ',', ':', '"', ';', '(', ')', '[', ']', '{', '}','<','>',
                '!', '?', '-', '`', '|', '/', '\\', '#', '&', '+', '=', '~', '$', '^', '@'
        };
        HashSet<Character> hashSet = new HashSet<Character>();
        for (Character c : whiteSpaces)
            hashSet.add(c);

        return hashSet;
    }

    public static void setStem(boolean bool){
        toStem = bool;
    }

    public static int parseInt(String val){
        try{
            int x = Integer.parseInt(val);
            return x;
        } catch (NumberFormatException e){
            e.printStackTrace();
        }

        return -1;
    }
}
