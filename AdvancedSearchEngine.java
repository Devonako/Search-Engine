import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AdvancedSearchEngine {

    private Map<String, Map<String, Integer>> index;
    private Set<String> stopWords;
    private Map<String, Set<String>> synonyms;

    public AdvancedSearchEngine() {
        index = new HashMap<>();
        stopWords = new HashSet<>(Arrays.asList("a", "an", "the", "is", "are", "was", "were", "and", "or", "in", "on", "at", "to", "for", "of", "by"));
        synonyms = new HashMap<>();
        // Add synonyms (example)
        synonyms.put("happy", new HashSet<>(Arrays.asList("joyful", "cheerful")));
        synonyms.put("sad", new HashSet<>(Arrays.asList("unhappy", "depressed")));
    }

    public void indexDocument(String documentPath) throws IOException {
        File document = new File(documentPath);
        BufferedReader reader = new BufferedReader(new FileReader(document));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] words = line.toLowerCase().split("\\W+");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (!stopWords.contains(word)) {
                    String stemmedWord = stem(word);
                    if (!index.containsKey(stemmedWord)) {
                        index.put(stemmedWord, new HashMap<>());
                    }
                    Map<String, Integer> documentFrequency = index.get(stemmedWord);
                    documentFrequency.put(documentPath, documentFrequency.getOrDefault(documentPath, 0) + 1);

                    // Add phrase indexing (consider bi-grams for now)
                    if (i < words.length - 1) {
                        String nextWord = words[i + 1];
                        if (!stopWords.contains(nextWord)) {
                            String phrase = stemmedWord + " " + stem(nextWord);
                            if (!index.containsKey(phrase)) {
                                index.put(phrase, new HashMap<>());
                            }
                            index.get(phrase).put(documentPath, index.get(phrase).getOrDefault(documentPath, 0) + 1);
                        }
                    }
                }
            }
        }
        reader.close();
    }

    public List<String> search(String query, String rankingAlgorithm) {
        Map<String, Double> scores = new HashMap<>();
        String[] queryWords = query.toLowerCase().split("\\W+");
        for (int i = 0; i < queryWords.length; i++) {
            String word = queryWords[i];
            if (!stopWords.contains(word)) {
                String stemmedWord = stem(word);
                processWord(scores, stemmedWord);

                // Handle synonyms
                if (synonyms.containsKey(word)) {
                    for (String synonym : synonyms.get(word)) {
                        processWord(scores, stem(synonym));
                    }
                }

                // Handle phrases
                if (i < queryWords.length - 1) {
                    String nextWord = queryWords[i + 1];
                    if (!stopWords.contains(nextWord)) {
                        String phrase = stemmedWord + " " + stem(nextWord);
                        processWord(scores, phrase);
                    }
                }
            }
        }

        // Apply ranking algorithm
        List<Map.Entry<String, Double>> sortedScores;
        switch (rankingAlgorithm) {
            case "tf-idf":
                sortedScores = rankByTFIDF(scores);
                break;
            case "bm25":
                sortedScores = rankByBM25(scores);
                break;
            default:
                sortedScores = rankByFrequency(scores);
                break;
        }

        List<String> results = new ArrayList<>();
        for (Map.Entry<String, Double> entry : sortedScores) {
            results.add(entry.getKey() + " (score: " + entry.getValue() + ")");
        }
        return results;
    }

    private void processWord(Map<String, Double> scores, String word) {
        if (index.containsKey(word)) {
            for (Map.Entry<String, Integer> entry : index.get(word).entrySet()) {
                scores.put(entry.getKey(), scores.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
            }
        }
    }

    private List<Map.Entry<String, Double>> rankByFrequency(Map<String, Double> scores) {
        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        return sortedScores;
    }

    // Implement TF-IDF ranking
    private List<Map.Entry<String, Double>> rankByTFIDF(Map<String, Double> scores) {
        int totalDocuments = index.get(index.keySet().iterator().next()).size(); // Get total number of documents from any term in the index
        Map<String, Double> tfidfScores = new HashMap<>();

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String document = entry.getKey();
            double tf = entry.getValue(); // Term frequency (already calculated in scores)
            double idf = Math.log(totalDocuments / (double) index.get(stem(entry.getKey())).size()); // Inverse document frequency
            tfidfScores.put(document, tf * idf);
        }

        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(tfidfScores.entrySet());
        sortedScores.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        return sortedScores;
    }

    // Implement BM25 ranking
    private List<Map.Entry<String, Double>> rankByBM25(Map<String, Double> scores) {
        int totalDocuments = index.get(index.keySet().iterator().next()).size();
        double avgdl = calculateAverageDocumentLength(); // Calculate average document length
        double k1 = 1.2; // Tuning parameter
        double b = 0.75; // Tuning parameter

        Map<String, Double> bm25Scores = new HashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            String document = entry.getKey();
            double tf = entry.getValue();
            int dl = getDocumentLength(document); // Get length of the document
            double idf = Math.log((totalDocuments - index.get(stem(entry.getKey())).size() + 0.5) / (index.get(stem(entry.getKey())).size() + 0.5));
            double bm25 = idf * (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * dl / avgdl));
            bm25Scores.put(document, bm25);
        }

        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(bm25Scores.entrySet());
        sortedScores.sort(Map.Entry.<String, Double>comparingByValue().reversed());
        return sortedScores;
    }

    private double calculateAverageDocumentLength() {
        int totalLength = 0;
        for (Map<String, Integer> documentFrequency : index.values()) {
            for (int frequency : documentFrequency.values()) {
                totalLength += frequency;
            }
        }
        return (double) totalLength / index.get(index.keySet().iterator().next()).size();
    }

    private int getDocumentLength(String document) {
        int length = 0;
        for (Map<String, Integer> documentFrequency : index.values()) {
            length += documentFrequency.getOrDefault(document, 0);
        }
        return length;
    }

    // Stemming using Porter Stemmer
    private String stem(String word) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public static void main(String[] args) throws IOException {
        AdvancedSearchEngine engine = new AdvancedSearchEngine();
        engine.indexDocument("document1.txt");
        engine.indexDocument("document2.txt");

        System.out.println("Frequency Ranking:");
        List<String> results = engine.search("example query", "frequency");
        for (String result : results) {
            System.out.println(result);
        }

        System.out.println("\nTF-IDF Ranking:");
        results = engine.search("example query", "tf-idf");
        for (String result : results) {
            System.out.println(result);
        }

        System.out.println("\nBM25 Ranking:");
        results = engine.search("example query", "bm25");
        for (String result : results) {
            System.out.println(result);
        }
    }
}