package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.expressions.Expr;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.view.SheetPanel;

public class CellCommand implements Command{
    private final CalcSheet sheet;
    private final int row, col;
    private Expr expr;


    public CellCommand(CalcSheet sheet, SheetPanel panel) {
        this.sheet = sheet;
        this.row = panel.getSelectedRow();
        this.col = panel.getSelectedColumn();
    }

    @Override
    public boolean execute() {
        expr = sheet.getCellAt(row, col).getExpr();
        return true;
    }

    @Override
    public void undo() {
        sheet.setValueAt(expr, row, col);
    }
}