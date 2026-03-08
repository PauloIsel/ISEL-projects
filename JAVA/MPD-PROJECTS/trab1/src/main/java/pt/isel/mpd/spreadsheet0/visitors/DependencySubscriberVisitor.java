package pt.isel.mpd.spreadsheet0.visitors;

import pt.isel.mpd.spreadsheet0.model.Cell;
import pt.isel.mpd.spreadsheet0.model.CellRef;

public class DependencySubscriberVisitor extends DependencyBaseVisitor {
    public DependencySubscriberVisitor(Cell cell) {
        super(cell);
    }

    @Override
    public void visit(CellRef cellRef) {
        Cell referencedCell = this.getCell().sheet.getCellAt(cellRef.getRow(), cellRef.getCol());
        referencedCell.subscribe(this.getCell());
    }
}