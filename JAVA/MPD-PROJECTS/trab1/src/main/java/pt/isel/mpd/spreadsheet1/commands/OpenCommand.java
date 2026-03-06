package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OpenCommand implements Command {
    private final CalcSheet sheet;

    public OpenCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new OpenCommand(sheet);
    }

    @Override
    public boolean execute() {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter("Json","json");
        fileChooser.addChoosableFileFilter(filter);
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                CalcSheet loadedSheet = CalcSheet.loadFromFile(filePath);

                sheet.getSheetHistory().clear();
                sheet.setTableCells(loadedSheet.getTableCells());
                sheet.fireTableStructureChanged();
                sheet.fireTableDataChanged();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error opening file: " + e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void undo() {}
}
