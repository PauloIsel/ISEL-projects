package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.expressions.Expr;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.model.Cell;

import java.util.HashMap;
import java.util.Map;

public class RemoveColCommand implements Command{
    private final Map<Integer, Expr> removedCells = new HashMap<>();
    private final CalcSheet sheet;

    public RemoveColCommand(CalcSheet sheet) {
        this.sheet = sheet;
    }

    public static Command create(CalcSheet sheet) {
        return new RemoveColCommand(sheet);
    }

    @Override
    public boolean execute() {
        int colCount = sheet.getColumnCount();
        int colIndexToRemove = colCount - 1;

        for (int row = 1; row < sheet.getRowCount(); row++) {
            Cell cell = sheet.getCellAt(row, colIndexToRemove);
            removedCells.put(row, cell.getExpr());
        }

        sheet.removeColumn();
        sheet.fireTableStructureChanged();
        return sheet.getColumnCount() == colCount - 1;
    }

    @Override
    public void undo() {
        sheet.addColumn();

        int colIndex = sheet.getColumnCount() - 1;
        for (Map.Entry<Integer, Expr> entry : removedCells.entrySet()) {
            int row = entry.getKey();
            Expr expr = entry.getValue();
            sheet.setCellAt(row, colIndex, expr);
        }

        sheet.fireTableStructureChanged();
    }
}
