package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.expressions.NullExpr;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public class ClearCommand implements Command{
    private final CalcSheet sheet;
    private CalcSheet oldSheet;

    public ClearCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new ClearCommand(sheet);
    }

    @Override
    public boolean execute() {
        oldSheet = new CalcSheet(sheet.getRowCount(), sheet.getColumnCount());
        for (int row = 1; row < sheet.getRowCount(); row++) {
            for (int col = 1; col < sheet.getColumnCount(); col++) {
                oldSheet.setCellAt(row, col, sheet.getCellAt(row, col).getExpr());
            }
        }

        for (int row = 1; row < sheet.getRowCount(); row++) {
            for (int col = 1; col < sheet.getColumnCount(); col++) {
                sheet.setCellAt(row, col, new NullExpr());
            }
        }

        sheet.fireTableDataChanged();
        return true;
    }

    @Override
    public void undo() {
        for (int row = 1; row < sheet.getRowCount(); row++) {
            for (int col = 1; col < sheet.getColumnCount(); col++) {
                sheet.setCellAt(row, col, oldSheet.getCellAt(row, col).getExpr());
            }
        }
        sheet.fireTableStructureChanged();
    }
}
