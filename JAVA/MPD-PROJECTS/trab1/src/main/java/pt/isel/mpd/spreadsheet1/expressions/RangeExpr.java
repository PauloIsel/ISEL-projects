package pt.isel.mpd.spreadsheet1.expressions;

import pt.isel.mpd.spreadsheet1.model.AbstractCalcSheet.CellCoords;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;

public abstract class RangeExpr implements Expr {
    protected final CalcSheet sheet;
    protected final CellCoords coordsStart, coordsEnd;

    public RangeExpr(CalcSheet sheet, String startRange, String endRange) {
        this.sheet = sheet;
        this.coordsStart = sheet.coordsFromName(startRange);
        this.coordsEnd = sheet.coordsFromName(endRange);
    }

    public CellCoords getCoordsStart() {
        return coordsStart;
    }

    public CellCoords getCoordsEnd() {
        return coordsEnd;
    }

    public CalcSheet getSheet() {
        return sheet;
    }

    public abstract String getOperator();

    @Override
    public String getFormula() {
        return getOperator() + "(" +
                sheet.nameFromCoords(coordsStart.row(), coordsStart.col()) + ":"
                + sheet.nameFromCoords(coordsEnd.row(), coordsEnd.col()) + ")";
    }

    @Override
    public String toString() {
        return getEvalText();
    }

    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }
}