package trabs.trab2.grupo2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DocumentsSimilarity extends JFrame {
    private Document doc;

    private final JTextField pathname1Field;
    private final JTextField pathname2Field;
    private final JTextField infoField;
    private final JTextField kField;
    private final JTextArea outputTextArea;

    public DocumentsSimilarity() {
        super("Documents Similarity");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel pathnamePanel = new JPanel(new GridLayout(1, 2));

        pathname1Field = getJTextField(pathnamePanel, "pathname1", BorderLayout.CENTER, 20, true);
        pathname2Field = getJTextField(pathnamePanel, "pathname2", BorderLayout.CENTER, 20, true);
        topPanel.add(pathnamePanel, BorderLayout.CENTER);

        kField = getJTextField(topPanel, "k", BorderLayout.EAST, 3, true);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        outputTextArea = getJTextArea();
        getContentPane().add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        infoField = getJTextField(buttonPanel, "information", BorderLayout.CENTER, 20, false);

        JButton listButton = getJButton("Read", actionRead());
        buttonPanel.add(listButton, BorderLayout.EAST);

        setJMenuBar(createJMenuBar());

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    private JTextField getJTextField(JPanel panel, String title, String positionPanel, int size, boolean editable) {
        JTextField textField = new JTextField(size);
        textField.setBorder(BorderFactory.createTitledBorder(title));
        textField.setEditable(editable);
        panel.add(textField, positionPanel);
        return textField;
    }

    private JTextArea getJTextArea() {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBorder(BorderFactory.createTitledBorder(""));
        textArea.setEditable(false);
        return textArea;
    }

    private JButton getJButton(String title, ActionListener listener) {
        JButton button = new JButton(title);
        button.addActionListener(listener);
        return button;
    }

    private JMenuBar createJMenuBar() {
        JMenu menu = new JMenu("words");

        JMenuItem item = new JMenuItem("all");
        item.addActionListener(actionAll());
        menu.add(item);

        item = new JMenuItem("same ocurrences");
        item.addActionListener(actionSameOcurrences());
        menu.add(item);

        JMenu subMenu = new JMenu("only");

        item = new JMenuItem("file 1");
        item.addActionListener(actionOnlyFile(1));
        subMenu.add(item);

        item = new JMenuItem("file 2");
        item.addActionListener(actionOnlyFile(2));
        subMenu.add(item);

        item = new JMenuItem("top 20");
        item.addActionListener(actionTop20());
        menu.add(item);

        menu.add(subMenu);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        return menuBar;
    }

    private ActionListener actionRead() {
        return e -> {
            doc = new Document(pathname1Field.getText(), pathname2Field.getText(), infoField);

            int similarity = doc.calculateSimilarity();
            infoField.setText("Similarity: " + similarity);
        };
    }

    private ActionListener actionAll() {
        return e -> {
            if (doc == null) return;

            outputTextArea.setText("All words:\n");
            doc.getAllWords((oc) -> outputTextArea.append(oc.getWord() + ": " + oc.getTotal() + "\n"));
        };
    }

    private ActionListener actionTop20() {
        return e -> {
            if (doc == null) return;

            outputTextArea.setText("Top 20 words:\n");
            int start = outputTextArea.getText().length();
            doc.getTop20Words((oc) -> outputTextArea.insert(oc.getWord() + ": " + oc.getTotal() + "\n", start));
        };
    }

    private ActionListener actionSameOcurrences() {
        return e -> {
            if (doc == null) return;

            try {
                int k = Integer.parseInt(kField.getText());
                outputTextArea.setText("Words with " + k + " ocurrences in both files:\n");
                doc.getSameOcurrencesWords((word) -> outputTextArea.append(word + "\n"), k);
            } catch (NumberFormatException ex) {
                infoField.setText("Invalid k value");
            }
        };
    }

    private ActionListener actionOnlyFile(int fileN) {
        return e -> {
            if (doc == null) return;

            outputTextArea.setText("Words only in file " + fileN + ":\n");
            doc.getOnlyInOneFileWords((word) -> outputTextArea.append(word + "\n"), fileN);
        };
    }

    public static void main(String[] args) {
        DocumentsSimilarity window = new DocumentsSimilarity();
        window.setVisible(true);
    }
}
