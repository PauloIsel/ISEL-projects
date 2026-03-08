package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.expressions.Expr;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.model.Cell;

import java.util.HashMap;
import java.util.Map;

public class RemoveLineCommand implements Command {
    private final Map<Integer, Expr> removedCells = new HashMap<>();
    private final CalcSheet sheet;

    public RemoveLineCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new RemoveLineCommand(sheet);
    }

    @Override
    public boolean execute() {
        int rowCount = sheet.getRowCount();
        int rowIndexToRemove = rowCount - 1;

        for (int col = 1; col < sheet.getColumnCount(); col++) {
            Cell cell = sheet.getCellAt(rowIndexToRemove, col);
            removedCells.put(col, cell.getExpr());
        }

        sheet.removeRow();
        sheet.fireTableStructureChanged();
        return sheet.getRowCount() == rowCount - 1;
    }

    @Override
    public void undo() {
        sheet.addRow();

        int rowIndex = sheet.getRowCount() - 1;
        for (Map.Entry<Integer, Expr> entry : removedCells.entrySet()) {
            int col = entry.getKey();
            Expr expr = entry.getValue();
            sheet.setCellAt(rowIndex, col, expr);
        }

        sheet.fireTableStructureChanged();
    }
}
