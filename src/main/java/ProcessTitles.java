import java.util.*;

/**
 * Created by Kangyan Zhou on 4/11/2016.
 */
public class ProcessTitles {
    private static String[] exceptions = {"seed", "based", "red", "mining", "computing", "series"};
    private static Set<String> notSpecialForm = new HashSet<String>(Arrays.asList(exceptions));

    public Set<String> vocabs = null;
    public Map<String, String> interChangle = null;
    public Map<String, Float> qualityPhrasesAll = null;
    public ArrayList<Map<String, Float>> qualityPhrasesEachTopic = null;
    public Map<String, Integer> vocabIndex = null;
    public Set<String> trigram = null;
    public float[][] topicWordDistribution = null;

    public ProcessTitles(){
        vocabs = ReadFile.ReadVocab();
        interChangle = ReadFile.ReadInterchangable();
        qualityPhrasesEachTopic = ReadFile.ReadQualityPhrasesEachTopic();
        vocabIndex = ReadFile.ReadIndexWord();
        qualityPhrasesAll = ReadFile.ReadAllQualityPhrases();
        trigram = ReadFile.ReadTrigram();
        topicWordDistribution = ReadFile.ReadTopicWordDistribution();
    }

    /*
    *  Structure to store the statistics for one researcher
    * */
    private class ResearcherStats{
        ArrayList<String> newTitles = null;
        Map<String, Integer> wordCount = null;
        Map<String, Integer> bigramCount = null;

        public ResearcherStats(ArrayList<String> newTitles, Map<String, Integer> wordCount, Map<String, Integer> bigramCount){
            this.newTitles = newTitles;
            this.wordCount = wordCount;
            this.bigramCount = bigramCount;
        }
    }

    /*
     *  Preprocess every title for one researcher by reducing every word to its base form
     *  Return the statistical count for the researcher and preprocessd titles
     * */
    public ResearcherStats PreprocessTitles(ArrayList<String> rawTitles){
        ArrayList<String> newTitles = new ArrayList<String>();
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        Map<String, Integer> bigramCount = new HashMap<String, Integer>();

        String title = "";
        String baseForm = "";
        String prev1 = "";
        String prev2 = "";
        String bigram = "";
        String trigram = "";
        for(String rawTitle: rawTitles){
            for(String word : rawTitle.split("\\s+")){
                baseForm = word;
                if(!ReadFile.vocabs.contains(word))
                    baseForm = ChangeForm(word);

                if(!baseForm.isEmpty()){
                    if(!wordCount.containsKey(baseForm)){
                        wordCount.put(baseForm, 0);
                    }

                    wordCount.put(baseForm, wordCount.get(baseForm) + 1);

                    if(!prev1.isEmpty()){
                        bigram = prev1 + " " + baseForm;
                        if(!bigramCount.containsKey(bigram)){
                            bigramCount.put(bigram, 0);
                        }

                        bigramCount.put(bigram, bigramCount.get(bigram) + 1);
                    }

                    if(!prev2.isEmpty()){
                        trigram = prev2 + " " + prev1 + " " + baseForm;
                        if(trigram.contains(trigram)){
                            if(!bigramCount.containsKey(trigram)){
                                bigramCount.put(trigram, 0);
                            }

                            bigramCount.put(trigram, bigramCount.get(trigram) + 1);
                        }
                    }

                    prev2 = prev1;
                    prev1 = baseForm;
                }
                title = title + baseForm + " ";
            }

            newTitles.add(title);
            title = "";
            prev1 = "";
            prev2 = "";
        }

        return new ResearcherStats(newTitles, wordCount, bigramCount);
    }

    /*
     *  Change a word to its base form. If not exist, return empty string
     * */
    private String ChangeForm(String word){
        String ret = "";
        if(word.endsWith("ly")) {
            ret = word.substring(0, word.length() - 2);
            if (ReadFile.vocabs.contains(ret))
                return ret;
        }

        if(word.endsWith("s")){
            if(word.endsWith("ies") && !notSpecialForm.contains(word)){
                ret = word.replace("ies", "y");
                if (ReadFile.vocabs.contains(ret))
                    return ret;
            }
        }
        else{
            ret = word.substring(0, word.length() - 1);
            if (ReadFile.vocabs.contains(ret))
                return ret;
        }

        if(word.endsWith("ed") && !notSpecialForm.contains(word)){
            ret = word.substring(0, word.length() - 1);
            if (ReadFile.vocabs.contains(ret))
                return ret;

            ret = word.substring(0, word.length() - 2);
            if (ReadFile.vocabs.contains(ret))
                return ret;
        }

        if(word.endsWith("ing") && !notSpecialForm.contains(word)){
            ret = word.substring(0, word.length() - 4);

            if (ReadFile.vocabs.contains(ret))
                return ret;

            ret = word.substring(0, word.length() - 3);
            if (ReadFile.vocabs.contains(ret))
                return ret;

            ret = ret + "e";
            if (ReadFile.vocabs.contains(ret))
                return ret;
        }

        ret = "";
        return ret;
    }

    /*
     *  return every bigram in order in a list of titles and its count
     * */
    public Set<Integer> returnHighestThreeTopic(ResearcherStats stats){
        Map<String, Integer> wordCount = stats.wordCount;

        Map<Integer, Float> topicProb = new HashMap<Integer, Float>();
        Map.Entry<Integer, Float> min = null;

        for(int i = 0; i < 15; i++){
            float probEachTopic = 0;
            for(Map.Entry<String, Integer> pair : wordCount.entrySet()){
                //System.out.print(pair.getKey() + ":" + topicWordDistribution[i][vocabIndex.get(pair.getKey())] + "\n");
                probEachTopic += Math.log(pair.getValue()) + Math.log(topicWordDistribution[i][vocabIndex.get(pair.getKey())]);
            }
            if(topicProb.size() < 3)
                topicProb.put(i, probEachTopic);
            else
                if(min != null && min.getValue() < probEachTopic){
                    topicProb.remove(min.getKey());
                    topicProb.put(i, probEachTopic);
                    min = null;
                }

//            System.out.print(i + ":" + probEachTopic + "\n");

            // update topic with minimum prob
            for (Map.Entry<Integer, Float> entry : topicProb.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    min = entry;
                }
//                System.out.print("min:" + min.getKey() + "\n");
            }

//            System.out.print("size:" + topicProb.size() + "\n");
        }

        return topicProb.keySet();
    }

    public Set<String> generateRepresentativePhrases(ResearcherStats stats, Set<Integer> mostLikelyTopics){
        HashMap<String, Float> candidatePhrases = new LinkedHashMap<String, Float>();
        Map<String, Integer> bigramCount = new HashMap<String, Integer>(stats.bigramCount);

        //merge all quality phrases into one map
        Map<String, Float> currQualityPhrases = null;
        Map<String, Float> qualityPhrasesFromTopic = new HashMap<String, Float>();
        for(int topicIndex : mostLikelyTopics){
            currQualityPhrases = this.qualityPhrasesEachTopic.get(topicIndex);
            for(Map.Entry<String, Float> entry : currQualityPhrases.entrySet()){
                if(!qualityPhrasesFromTopic.containsKey(entry.getKey()))
                    qualityPhrasesFromTopic.put(entry.getKey(), entry.getValue());
                else if(qualityPhrasesFromTopic.get(entry.getKey()) < entry.getValue())
                    qualityPhrasesFromTopic.put(entry.getKey(), entry.getValue());
            }
        }

        Set<String> intersection = bigramCount.keySet();
        intersection.retainAll(qualityPhrasesFromTopic.keySet());

        Map.Entry<String, Float> min = null;
        float score = 0;
        for(String phrase : intersection){
            score = bigramCount.get(phrase) * qualityPhrasesFromTopic.get(phrase);
//            System.out.print(phrase + ":" + score + "\n");
            if(candidatePhrases.size() < 5)
                candidatePhrases.put(phrase, score);
            else
                if(min != null && min.getValue() < score){
                    candidatePhrases.remove(min.getKey());
                    candidatePhrases.put(phrase, score);
                    min = null;
                }

            // update topic with minimum prob
            for (Map.Entry<String, Float> entry : candidatePhrases.entrySet()) {
                if (min == null || min.getValue() > entry.getValue()) {
                    min = entry;
                }
            }
        }

        ArrayList<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, Float> entry1 : candidatePhrases.entrySet()) {
            for (Map.Entry<String, Float> entry2 : candidatePhrases.entrySet()) {
                if (entry2.getKey().contains(entry1.getKey()) && !entry2.getKey().equals(entry1.getKey())){
                    toRemove.add(entry1.getKey());
                }
            }
        }

        for(String key : toRemove){
            candidatePhrases.remove(key);
        }

        // use global matching to fit the minimum number of return result
        Map.Entry<String, Integer> anotherMin = null;
        if(candidatePhrases.size() < 5){
            HashMap<String, Integer> temp = new HashMap<String, Integer>();
            for(Map.Entry<String, Integer> entry : stats.bigramCount.entrySet()){
                String phrase = entry.getKey();
                Integer count = entry.getValue();
                if(candidatePhrases.containsKey(phrase) || toRemove.contains(phrase) || count < 3)
                    continue;

                if(temp.size() < 5 - candidatePhrases.size())
                    temp.put(phrase, count);
                else if(anotherMin != null && anotherMin.getValue() < count){
                    temp.remove(anotherMin.getKey());
                    temp.put(phrase, count);
                    anotherMin = null;
                }

                // update topic with minimum prob
                for (Map.Entry<String, Integer> entry2 : temp.entrySet()) {
                    if (anotherMin == null || anotherMin.getValue() > entry2.getValue()) {
                        anotherMin = entry2;
                    }
                }
            }

            for (Map.Entry<String, Integer> entry : temp.entrySet()) {
                candidatePhrases.put(entry.getKey(), (float)entry.getValue());
            }
        }

        if(candidatePhrases.size() < 5){
            intersection = stats.bigramCount.keySet();
            intersection.retainAll(qualityPhrasesAll.keySet());
            String max = null;
            float currScore = 0;
            if(intersection.size() > 0){
                for(String phrase : intersection){
                    if(candidatePhrases.containsKey(phrase) || toRemove.contains(phrase))
                        continue;

                    score = stats.bigramCount.get(phrase) * qualityPhrasesAll.get(phrase);
                    if (max == null || currScore < score) {
                        max = phrase;
                        currScore = score;
                    }
                }
            }

            if(max != null)
                candidatePhrases.put(max, currScore);
        }

        return candidatePhrases.keySet();
    }

    public static void main(String[] args) {
        ProcessTitles p = new ProcessTitles();
        Map<String, ArrayList<String>> testInput = ReadFile.ReadTestFiles();
        for(Map.Entry<String, ArrayList<String>> entry : testInput.entrySet()){
            System.out.print(entry.getKey() + "\n");
            ResearcherStats stats = p.PreprocessTitles(entry.getValue());
            Set<Integer> topics = p.returnHighestThreeTopic(stats);
            Set<String> phrases = p.generateRepresentativePhrases(stats, topics);
            System.out.print(phrases + "\n");
        }
    }
}
