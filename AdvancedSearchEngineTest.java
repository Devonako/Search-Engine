import org.junit.jupiter.api.Test;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedSearchEngineTest {

    private AdvancedSearchEngine engine;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws IOException {
        engine = new AdvancedSearchEngine();
        engine.indexDocument("test1.txt");
        engine.indexDocument("test2.txt");
    }

    @Test
    void testStem() {
        assertEquals("run", engine.stem("running"));
        assertEquals("jump", engine.stem("jumps"));
        assertEquals("happ", engine.stem("happy"));
    }

    @Test
    void testIndexDocument() {
        Map<String, Map<String, Integer>> index = engine.index;
        assertTrue(index.containsKey("exampl"));
        assertTrue(index.containsKey("queri"));
        assertTrue(index.containsKey("exampl queri")); // Phrase
        assertTrue(index.get("exampl").containsKey("test1.txt"));
    }

    @Test
    void testSearchFrequencyRanking() {
        List<String> results = engine.search("example query", "frequency");
        assertTrue(results.contains("test1.txt (score: 3.0)")); // Assuming "example" appears 2 times and "query" 1 time in test1.txt
        assertTrue(results.contains("test2.txt (score: 1.0)")); // Assuming "query" appears 1 time in test2.txt
    }

    @Test
    void testSearchTFIDFRanking() {
        List<String> results = engine.search("example query", "tf-idf");
        // Add assertions based on expected TF-IDF scores
    }

    @Test
    void testSearchBM25Ranking() {
        List<String> results = engine.search("example query", "bm25");
        // Add assertions based on expected BM25 scores
    }

    @Test
    void testSearchWithSynonyms() {
        List<String> results = engine.search("happy query", "frequency");
        // Assuming "joyful" (synonym of happy) is in test2.txt
        assertTrue(results.contains("test2.txt"));
    }

    @Test
    void testSearchWithPhrase() {
        List<String> results = engine.search("example query", "frequency");
        // Assuming "example query" phrase is in test1.txt
        assertTrue(results.contains("test1.txt"));
    }
