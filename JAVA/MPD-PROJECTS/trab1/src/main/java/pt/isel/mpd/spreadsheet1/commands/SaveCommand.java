package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class SaveCommand implements Command {
    private final CalcSheet sheet;

    public SaveCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new SaveCommand(sheet);
    }

    @Override
    public boolean execute() {
        JFileChooser fileChooser = new JFileChooser();
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if(!filePath.contains(".json")) filePath += ".json";
            try {
                File f = new File(filePath);
                if(f.isFile()) throw new Exception();
                sheet.saveToFile(filePath);
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(null , "Overwrite the file?", "Warning", JOptionPane.YES_NO_OPTION);
                if(JOptionPane.YES_OPTION == 0) {
                    try {
                        sheet.saveToFile(filePath);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage());
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void undo() {}
}
