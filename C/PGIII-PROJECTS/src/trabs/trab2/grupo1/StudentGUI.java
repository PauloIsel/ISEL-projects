package trabs.trab2.grupo1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;

public class StudentGUI extends JFrame {
    public StudentGUI() {
        super("Students");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel filePanel = new JPanel(new BorderLayout());

        JTextField fileField = getJTextField(filePanel, "pathname", BorderLayout.CENTER, true);
        JTextField classField = getJTextField(filePanel, "class", BorderLayout.EAST, true);
        getContentPane().add(filePanel, BorderLayout.NORTH);

        JTextArea outputTextArea = getJTextArea();
        getContentPane().add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        JTextField infoField = getJTextField(buttonPanel, "information", BorderLayout.CENTER, false);

        JButton listButton = getJButton("List", actionList(fileField, classField, infoField, outputTextArea));
        buttonPanel.add(listButton, BorderLayout.EAST);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    private static JTextField getJTextField(JPanel panel, String title, String positionPanel, boolean editable) {
        JTextField textField = new JTextField(20);
        textField.setBorder(BorderFactory.createTitledBorder(title));
        textField.setEditable(editable);
        panel.add(textField, positionPanel);
        return textField;
    }

    private static JTextArea getJTextArea() {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBorder(BorderFactory.createTitledBorder(""));
        textArea.setEditable(false);
        return textArea;
    }

    private static JButton getJButton(String title, ActionListener listener) {
        JButton button = new JButton(title);
        button.addActionListener(listener);
        return button;
    }

    private static ActionListener actionList(JTextField fileField, JTextField classField, JTextField infoField, JTextArea outputTextArea) {
        return e -> {
            try (BufferedReader br = new BufferedReader(new FileReader(fileField.getText()));
                 StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw)) {

                int c = Student.copyStudents(br, pw, classField.getText());
                infoField.setText("The class " + classField.getText() + " has " + c + " students");

                outputTextArea.setBorder(BorderFactory.createTitledBorder(classField.getText()));
                outputTextArea.setText(sw.toString());
            } catch (IOException ex) {
                infoField.setText("Error: " + ex.getMessage());
            }
        };
    }

    public static void main(String[] args) {
        StudentGUI window = new StudentGUI();
        window.setVisible(true);
    }
}
