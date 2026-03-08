package pt.isel.mpd.spreadsheet1.visitors;

import pt.isel.mpd.spreadsheet1.exceptions.InvalidCoordinatesException;
import pt.isel.mpd.spreadsheet1.expressions.RangeExpr;
import pt.isel.mpd.spreadsheet1.model.Cell;
import pt.isel.mpd.spreadsheet1.model.CellRef;

public class DependencyUnsubscriberVisitor extends DependencyBaseVisitor {
    public DependencyUnsubscriberVisitor(Cell cell) {
        super(cell);
    }

    @Override
    public void visit(CellRef cellRef) {
        try {
            Cell referencedCell = this.getCell().sheet.getCellAt(cellRef.getRow(), cellRef.getCol());
            referencedCell.unSubscribe(this.getCell());
        } catch (InvalidCoordinatesException ignored) {}
    }

    @Override
    public void visit(RangeExpr n) {
        for (int row = n.getCoordsStart().row(); row <= n.getCoordsEnd().row(); row++) {
            for (int col = n.getCoordsStart().col(); col <= n.getCoordsEnd().col(); col++) {
                try {
                    Cell cell = n.getSheet().getCellAt(row, col);
                    if (cell != null) {
                        cell.unSubscribe(this.getCell());
                    }
                } catch (InvalidCoordinatesException ignored) {}
            }
        }
    }
}