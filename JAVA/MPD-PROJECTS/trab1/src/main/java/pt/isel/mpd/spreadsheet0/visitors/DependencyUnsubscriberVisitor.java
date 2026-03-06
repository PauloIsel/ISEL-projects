package pt.isel.mpd.spreadsheet0.visitors;

import pt.isel.mpd.spreadsheet0.model.Cell;
import pt.isel.mpd.spreadsheet0.model.CellRef;

public class DependencyUnsubscriberVisitor extends DependencyBaseVisitor {
    public DependencyUnsubscriberVisitor(Cell cell) {
        super(cell);
    }

    @Override
    public void visit(CellRef cellRef) {
        Cell referencedCell = this.getCell().sheet.getCellAt(cellRef.getRow(), cellRef.getCol());
        referencedCell.unSubscribe(this.getCell());
    }
}