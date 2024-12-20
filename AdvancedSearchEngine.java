import org.tartarus.snowball.ext.PorterStemmer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SearchEngineGUI extends JFrame {

    private JTextField searchField;
    private JTextArea resultsArea;
    private JComboBox<String> rankingComboBox;
    private AdvancedSearchEngine searchEngine;

    public SearchEngineGUI() {
        super("Simple Search Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        searchEngine = new AdvancedSearchEngine();

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        JLabel searchLabel = new JLabel("Enter query:");
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        rankingComboBox = new JComboBox<>(new String[]{"frequency", "tf-idf", "bm25"});
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(rankingComboBox);

        // Results panel
        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultsArea);

        // Add panels to the frame
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Search button action listener
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                String rankingAlgorithm = (String) rankingComboBox.getSelectedItem();
                List<String> results = searchEngine.search(query, rankingAlgorithm);
                resultsArea.setText("");
                for (String result : results) {
                    resultsArea.append(result + "\n");
                }
            }
        });

        setVisible(true);
    }

    public void indexDocuments(String[] documentPaths) {
        try {
            for (String path : documentPaths) {
                searchEngine.indexDocument(path);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error indexing documents: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SearchEngineGUI gui = new SearchEngineGUI();
        gui.indexDocuments(new String[]{"document1.txt", "document2.txt"});
    }
}

// AdvancedSearchEngine class (same as before)
// ...