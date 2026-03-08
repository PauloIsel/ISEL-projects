package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public class AddLineCommand implements Command {
    private final CalcSheet sheet;

    public AddLineCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new AddLineCommand(sheet);
    }

    @Override
    public boolean execute() {
        int rowCount = sheet.getRowCount();
        sheet.addRow();
        sheet.fireTableStructureChanged();
        return sheet.getRowCount() == rowCount + 1;
    }

    @Override
    public void undo() {
        sheet.removeRow();
        sheet.fireTableStructureChanged();
    }
}
