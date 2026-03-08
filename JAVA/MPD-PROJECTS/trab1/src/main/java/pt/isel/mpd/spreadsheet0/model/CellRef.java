package pt.isel.mpd.spreadsheet0.model;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.visitors.ExprVisitor;
import pt.isel.mpd.spreadsheet0.expressions.Expr;

public class CellRef implements Expr {
    private final String name;
    private final CalcSheet sheet;
    private final int row, col;

    public CellRef(String name, CalcSheet sheet) {
        this.name = name;
        this.sheet = sheet;

        var coords = sheet.coordsFromName(name);
        this.row = coords.row();
        this.col = coords.col();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
    
    @Override
    public double eval() {
        return sheet.getExprAt(row, col).eval();
    }
    
    @Override
    public String getFormula() {
        return name;
    }

    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }

    public static CellRef fromJson(JSONObject json, CalcSheet sheet) {
        return new CellRef(json.getString("ref"), sheet);
    }
}
