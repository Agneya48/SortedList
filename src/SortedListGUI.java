import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.Collator;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

public class SortedListGUI extends JFrame {
    private SortedList sortedList;
    private JTextArea displayArea;
    private JTextField manualWordField;
    private JButton addManualButton;
    private JComboBox<WordOption> randomWordComboBox;
    private JButton addRandomButton;
    private JTextField searchField;
    private JButton searchButton;
    private JCheckBox liveSearchToggle;
    private JButton clearButton, quitButton;
    private WordSampler sampler;

    public SortedListGUI() {
        super("Sorted Word List Manager");

        //Create Collator for international String comparison
        Collator collator = Collator.getInstance(Locale.JAPANESE); //using some Japanese input for testing
        collator.setStrength(Collator.PRIMARY); // will ignore case and accents

        // List setup, feed Collator into list as a parameter
        sortedList = new SortedList(collator); // Can pass a Collator if needed
        sampler = new WordSampler("filtered_words_minlength3.txt");


        setupUI();
        updateDisplay();
    }

    private void setupUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout()); //top level panel that goes in frame North section
        JPanel formPanel = new JPanel(new GridBagLayout()); //child panel nested in top panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);

        JLabel headerLabel = new JLabel("Sorted Word List Manager", SwingConstants.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(headerLabel, BorderLayout.NORTH);

        Dimension fieldSize = new Dimension(200, 25);  // Field / ComboBox width
        Dimension addButtonSize = new Dimension(100, 25); // Add Button width (smaller)

        // ===== Components ===== //
        JLabel manualLabel = new JLabel("Add Word:");
        manualLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        manualWordField = new JTextField();
        manualWordField.setPreferredSize(fieldSize);
        JButton addManualButton = new JButton("Add");
        addManualButton.setPreferredSize(addButtonSize);

        JLabel randomLabel = new JLabel("Add:");
        randomLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        randomWordComboBox = new JComboBox<>(new WordOption[]{
                new WordOption(5),
                new WordOption(10),
                new WordOption(20),
                new WordOption(50),
                new WordOption(100),
                new WordOption(200),
                new WordOption(500)
        });
        randomWordComboBox.setSelectedItem(new WordOption(20));
        randomWordComboBox.setPreferredSize(fieldSize);
        JButton addRandomButton = new JButton("Add");
        Dimension buttonSize = addManualButton.getPreferredSize();
        addRandomButton.setPreferredSize(buttonSize);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        searchField = new JTextField();
        searchField.setPreferredSize(fieldSize);

        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(100, 25));
        liveSearchToggle = new JCheckBox("Live Search");

        JButton clearButton = new JButton("Clear List");
        JButton quitButton = new JButton("Quit");

        int row = 0;

        // ===== Row 1: Manual Add ===== //
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(manualLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(manualWordField, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(addManualButton, gbc);

        // ===== Row 2: Random Add ===== //
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(randomLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(randomWordComboBox, gbc);

        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(addRandomButton, gbc);

        // ===== Row 3: Search TextField ===== //
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(searchLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(searchField, gbc);

        // No button in column 2 for this row (empty cell)

        // ===== Row 4: Action Buttons ===== //
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 3; // Span across whole row
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionButtonPanel.add(quitButton);
        actionButtonPanel.add(clearButton);
        actionButtonPanel.add(searchButton);
        actionButtonPanel.add(liveSearchToggle);

        formPanel.add(actionButtonPanel, gbc);

        topPanel.add(formPanel, BorderLayout.CENTER);

        // ===== Main Display ===== //
        displayArea = new JTextArea();
        displayArea.setMargin(new Insets(10, 10, 10, 10));
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        //Panel for padding
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Top, Left, Bottom, Right outer margin
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // ===== Button Actions ===== //
        addManualButton.addActionListener(e -> addManualWord());
        addRandomButton.addActionListener(e -> addRandomWords());
        searchButton.addActionListener(e -> performManualSearch());
        clearButton.addActionListener(e -> {
            sortedList.clear();
            updateDisplay();
        });
        quitButton.addActionListener(e -> System.exit(0));

        topPanel.add(actionButtonPanel, BorderLayout.SOUTH);
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);

        // ===== live update if autocomplete is toggled on ===== //
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (liveSearchToggle.isSelected()) performLiveSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (liveSearchToggle.isSelected()) performLiveSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                if (liveSearchToggle.isSelected()) performLiveSearch(); }
        });

        setVisible(true);

    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        List<String> words = sortedList.getList();
        for (int i = 0; i < words.size(); i++) {
            sb.append(i).append(": ").append(words.get(i)).append("\n");
        }
        displayArea.setText(sb.toString());
    }

    private void addManualWord() {
        String word = normalizeInput(manualWordField.getText());
        if (!word.isEmpty()) {
            sortedList.add(word);
            manualWordField.setText("");
            updateDisplay();
        }
    }

    /**
     * Used to speed up list creation, especially for testing. Will randomly add
     * a given amount of words to the sorteList instance, referencing a
     * file of 20k common words (with stop words pre-filtered)
     */
    private void addRandomWords() {
        try {
            WordOption option = (WordOption) randomWordComboBox.getSelectedItem();
            if (option != null) {
                int count = option.getCount();
                List<String> randomWords = sampler.getRandomWords(count);
                for (String word : randomWords) {
                    String normalizedWord = normalizeInput(word);
                    //Check if already exists before adding
                    if (!sortedList.contains(normalizedWord)) {
                        sortedList.add(normalizedWord);
                    }
                }
                updateDisplay();
            } else {
                showError("No random word option selected");
            }

        } catch (IOException e) {
            showError("IO Error loading random words: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Error loading random words: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called when the search button is clicked. Attempts to find an exact match using binary
     * search, otherwise defaults to the closest lexographic match using a upper-bound biased
     * binary search fallback method.
     */
    private void performManualSearch() {
        String query = normalizeInput(searchField.getText());
        if (query.isEmpty()) {
            updateDisplay();
            return;
        }

        int index = sortedList.binarySearch(query);
        if (index >= 0) {
            displayArea.setText("(Exact Match)\n" + index + ": " + sortedList.getList().get(index));
        } else {
            // Use smart closestMatch() for better suggestion
            String closest = sortedList.closestMatch(query);
            if (closest != null) {
                int closestIndex = sortedList.getList().indexOf(closest);
                displayArea.setText("(Closest Match)\n" + closestIndex + ": " + closest);
            } else {
                displayArea.setText("No match found.");
            }
        }
    }

    /**
     * If the live search toggle is on, this will return prefix matches
     * to a given substring entered in the search field, and automatically update
     * the display to show them.
     */
    private void performLiveSearch() {
        if (!liveSearchToggle.isSelected()) {
            return; // Live search disabled
        }

        String query = normalizeInput(searchField.getText());
        if (query.isEmpty()) {
            updateDisplay();
            return;
        }

        List<String> matches = sortedList.prefixMatches(query);
        if (!matches.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("(Live Matches)\n");
            for (String match : matches) {
                int index = sortedList.getList().indexOf(match);
                sb.append(index).append(": ").append(match).append("\n");
            }
            displayArea.setText(sb.toString());
        } else {
            displayArea.setText("No matches found.");
        }
    }

    /**
     * Obsoleted search method, kept for reference and a backup
     */
    private void performSearch() {
        String query = normalizeInput(searchField.getText());
        if (query.isEmpty()) {
            updateDisplay();
            return;
        }

        if (liveSearchToggle.isSelected()) {
            // Live Search Mode: show all possible matches
            List<String> matches = sortedList.prefixMatches(query);
            if (!matches.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("(Live Matches)\n");
                for (String match : matches) {
                    int index = sortedList.getList().indexOf(match);
                    sb.append(index).append(": ").append(match).append("\n");
                }
                displayArea.setText(sb.toString());
            } else {
                displayArea.setText("No matches found.");
            }
        } else {
            // Manual Search Button Mode: Exact or Closest Match
            int index = sortedList.binarySearch(query);
            if (index >= 0) {
                displayArea.setText("(Exact Match)\n" + index + ": " + sortedList.getList().get(index));
            } else {
                String closest = sortedList.closestMatch(query);
                if (closest != null) {
                    int closestIndex = sortedList.getList().indexOf(closest);
                    displayArea.setText("(Closest Match)\n" + closestIndex + ": " + closest);
                } else {
                    displayArea.setText("No match found.");
                }
            }
        }
    }

    private String normalizeInput(String rawInput) {
        if (rawInput == null) return "";
        String cleaned = rawInput.trim();
        cleaned = Normalizer.normalize(cleaned, Normalizer.Form.NFKC);
        return cleaned;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Helper class for the JComboBox, so the display can be customized
     * with the toString method without affecting the chosen 'count' value
     */
    private static class WordOption {
        private final int count;

        public WordOption(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return count + " words";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            WordOption other = (WordOption) obj;
            return count == other.count;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(count);
        }
    }

}
