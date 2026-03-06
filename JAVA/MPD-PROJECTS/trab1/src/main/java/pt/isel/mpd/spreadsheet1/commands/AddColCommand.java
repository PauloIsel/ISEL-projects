package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public class AddColCommand implements Command {
    private final CalcSheet sheet;

    public AddColCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new AddColCommand(sheet);
    }

    @Override
    public boolean execute() {
        int colCount = sheet.getColumnCount();
        sheet.addColumn();
        sheet.fireTableStructureChanged();
        return sheet.getColumnCount() == colCount + 1;
    }

    @Override
    public void undo() {
        sheet.removeColumn();
        sheet.fireTableStructureChanged();
    }

}
