package pt.isel.mpd.spreadsheet1.model;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;
import pt.isel.mpd.spreadsheet1.expressions.Expr;

public class CellRef implements Expr {
    public final String name;
    private final CalcSheet sheet;
    private final int row, col;

    public CellRef(String name, CalcSheet sheet) {
        this.name = name;
        this.sheet = sheet;

        var coords = sheet.coordsFromName(name);
        this.row = coords.row();
        this.col = coords.col();
    }

    public CellRef(String name) {
        this(name, null);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Cell getCell() {
        return sheet.getCellAt(row, col);
    }

    @Override
    public double eval() {
        return getCell().eval();
    }

    @Override
    public String getFormula() {
        return getCell().getFormula();
    }

    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }

    public static CellRef fromJson(JSONObject json, CalcSheet sheet) {
        return new CellRef(json.getString("ref"), sheet);
    }
}
