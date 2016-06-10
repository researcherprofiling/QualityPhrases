import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by Kangyan Zhou on 2016/4/7.
 */
public class ReadFile {
    private static final String VOCABFILENAME = "data/vocabs.txt";
    private static final String INTERCHANGEFILE = "data/interchangle_words.txt";
    private static final String TRIGRAMFILE = "data/trigrams.txt";
    private static final String DISTRIBUTIONFILE = "data/topicWordDistribution.txt";
    private static final String VOCABINDEXFILE = "data/vocabIndex.txt";
    private static final String TESTFILEPATH = "data/TestSet/";

    private static final String QUALITY_FILE_PREFIX = "data/QualityPhrases/Topic";
    private static final String QUALITY_FILE_SUFFIX = "/ranking.csv";
    private static final String QUALITY_FILE_ALL = "data/allPhrases.csv";

    private static String[] exceptions2 = {"open source", "algorithm computing", "performance computing", "mining frequent"};
    private static Set<String> meaninglessPhrases = new HashSet<String>(Arrays.asList(exceptions2));

    public static Set<String> vocabs = ReadVocab();
    public static Map<String, String> interChangle = ReadInterchangable();
    public static ArrayList<Map<String, Float>> qualityPhrases = ReadQualityPhrasesEachTopic();
    public static Map<String, Integer> vocabIndex = ReadIndexWord();

//    public static ArrayList<ArrayList<Float>> topicWordDistribution = ReadTopicWordDistribution();

    private static float[][] topicWordDistribution = ReadTopicWordDistribution();
//    public static INDArray topicWordDistribution = Nd4j.create(temp);
    /*
    * Read the vocabularies generated from 100000 titles
    * */
    public static Set<String> ReadVocab(){
        Set<String> vocabs = new HashSet<String>();
        String word = "";
        try{
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(VOCABFILENAME));
            while ((word = br.readLine()) != null) {
                vocabs.add(word);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return vocabs;
    }

    /*
    * Read the interchangle vocabularies generated from 100000 titles
    * */
    public static Map<String, String> ReadInterchangable(){
        Map<String, String> interChangables = new HashMap<String, String>();

        String line = "";
        String[] splitted = new String[2];
        try{
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(INTERCHANGEFILE));
            while ((line = br.readLine()) != null) {
                splitted = line.split(":");
                interChangables.put(splitted[0], splitted[1]);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return interChangables;
    }

    /*
    * Read the meaningful trigrams generated from Liu's Algorithm
    * */
     public static Set<String> ReadTrigram(){
        Set<String> vocabs = new HashSet<String>();
        String word = "";
        try{
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(TRIGRAMFILE));
            while ((word = br.readLine()) != null) {
                vocabs.add(word);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return vocabs;
    }

    /*
    * Read quality phrases for each topic
    * */
    public static ArrayList<Map<String, Float>> ReadQualityPhrasesEachTopic(){
        ArrayList<Map<String, Float>> ret = new ArrayList<Map<String, Float>>();
        int i = 0;
        String line = "";
        String[] splitted = new String[2];
        for(i =0; i< 15; i++){
            Map<String, Float> phrases = new HashMap<String, Float>();
            String filename = QUALITY_FILE_PREFIX + i + QUALITY_FILE_SUFFIX;
            try{
                // open input stream test.txt for reading purpose.
                BufferedReader br = new BufferedReader(new FileReader(filename));
                while ((line = br.readLine()) != null) {
                    splitted = line.split(",");
                    if(meaninglessPhrases.contains(splitted[0]))
                        continue;
                    float score = Float.parseFloat(splitted[1]);
                    if(score < 0.5)
                        break;

                    phrases.put(splitted[0], score);
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            ret.add(phrases);
        }

        return ret;
    }


    /*
    * Read quality phrases for each topic
    * */
    public static Map<String, Float> ReadAllQualityPhrases(){
        Map<String, Float> ret = new HashMap<String, Float>();
        int i = 0;
        String line = "";
        String[] splitted = new String[2];
        try{
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(QUALITY_FILE_ALL));
            while ((line = br.readLine()) != null) {
                splitted = line.split(",");
                if(meaninglessPhrases.contains(splitted[0]))
                    continue;
                float score = Float.parseFloat(splitted[1]);
                if(score < 0.4)
                    break;

                ret.put(splitted[0], score);
            }
        }catch(Exception e){
            e.printStackTrace();
        }



        return ret;
    }

    /*
    * Read word-topic distribution file
    * */
    public static float[][] ReadTopicWordDistribution(){
        float[][] ret = new float[15][];
        int i = 0;
        int j = 0;
        String line = "";
        try{
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(DISTRIBUTIONFILE));
            while ((line = br.readLine()) != null) {
                String[] prob = line.split("\\s+");
                ret[i] = new float[prob.length];
                for(String num: prob){
                    ret[i][j] = Float.parseFloat(num);
                    j = j + 1;
                }
                i = i + 1;
                j = 0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return ret;
    }

    /*
    * Read word-topic distribution file
    * */
    public static Map<String, Integer> ReadIndexWord(){
        Map<String, Integer> interChangables = new HashMap<String, Integer>();

        String line = "";
        String[] splitted = new String[2];
        try{
            // open input stream test.txt for reading purpose.
            BufferedReader br = new BufferedReader(new FileReader(VOCABINDEXFILE));
            while ((line = br.readLine()) != null) {
                splitted = line.split(":");
                interChangables.put(splitted[0], Integer.parseInt(splitted[1]));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return interChangables;
    }

    /*
    *  Functions for test
    * */
    public static Map<String, ArrayList<String>> ReadTestFiles(){
        Map<String, ArrayList<String>> testInput = new HashMap<String, ArrayList<String>>();
        File[] testFiles = new File(TESTFILEPATH).listFiles();

        String line = "";
        for(File f : testFiles){
            ArrayList<String> titles = new ArrayList<String>();
            try{
                // open input stream test.txt for reading purpose.
                BufferedReader br = new BufferedReader(new FileReader(f));
                while ((line = br.readLine()) != null) {
                    titles.add(line);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            testInput.put(f.getName(), titles);
        }

        return testInput;
    }

    public static void main(String[] args) throws Exception {
        System.out.print(vocabs.size() + "\n");
        System.out.print(interChangle.size() + "\n");
        System.out.print(qualityPhrases.size() + "\n");
        System.out.print(vocabIndex.get("parallel") + "\n");
//        System.out.print(topicWordDistribution[0].toString() + "\n");
    }
}
