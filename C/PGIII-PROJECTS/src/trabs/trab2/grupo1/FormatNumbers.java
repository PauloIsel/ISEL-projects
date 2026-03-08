package trabs.trab2.grupo1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class FormatNumbers extends JFrame {
    public FormatNumbers() {
        super("Format Numbers");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel textPanel = new JPanel(new GridLayout(1, 2));

        JTextArea inputTextArea = getJTextArea(textPanel, "Input", true);
        JTextArea outputTextArea = getJTextArea(textPanel, "Output", false);
        outputTextArea.setEditable(false);
        getContentPane().add(textPanel, BorderLayout.CENTER);

        JButton formatButton = getJButton("Format", actionFormat(inputTextArea, outputTextArea));
        getContentPane().add(formatButton, BorderLayout.SOUTH);

        pack();
    }

    private static JTextArea getJTextArea(JPanel textPanel, String title, boolean editable) {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBorder(BorderFactory.createTitledBorder(title));
        textArea.setEditable(editable);
        textPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return textArea;
    }

    private static JButton getJButton(String title, ActionListener listener) {
        JButton button = new JButton(title);
        button.addActionListener(listener);
        return button;
    }

    private static ActionListener actionFormat(JTextArea inputTextArea, JTextArea outputTextArea) {
        return e -> {
            try {
                StringWriter writer = new StringWriter();
                StreamUtils.copyWithFormat(new StringReader(inputTextArea.getText()), writer);
                outputTextArea.setText(writer.toString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        };
    }

    public static void main(String[] args) {
        FormatNumbers window = new FormatNumbers();
        window.setVisible( true );
    }
}
