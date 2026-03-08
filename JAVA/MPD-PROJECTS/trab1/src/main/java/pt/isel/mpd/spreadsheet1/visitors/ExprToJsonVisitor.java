package pt.isel.mpd.spreadsheet1.visitors;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.expressions.*;
import pt.isel.mpd.spreadsheet1.model.CellRef;

public class ExprToJsonVisitor implements ExprVisitor {
    private JSONObject json;

    public JSONObject getJson() {
        return json;
    }

    @Override
    public void visit(Const expr) {
        json = new JSONObject();
        json.put("type", expr.getClass().getSimpleName());
        json.put("value", expr.eval());
    }

    @Override
    public void visit(MemoExpr expr) {
        expr.expr.accept(this);
    }

    @Override
    public void visit(NullExpr n) {

    }

    @Override
    public void visit(RangeExpr n) {
        json = new JSONObject();
        json.put("type", n.getClass().getSimpleName());
        json.put("startRange", n.getSheet().
                nameFromCoords(n.getCoordsStart().row(), n.getCoordsStart().col()));
        json.put("endRange", n.getSheet().
                nameFromCoords(n.getCoordsEnd().row(), n.getCoordsEnd().col()));
        json.put("value", n.eval());
    }

    @Override
    public void visit(BinExpr expr) {
        json = new JSONObject();
        json.put("type", expr.getClass().getSimpleName());
        ExprToJsonVisitor leftVisitor = new ExprToJsonVisitor();
        expr.left.accept(leftVisitor);
        json.put("left", leftVisitor.getJson());
        ExprToJsonVisitor rightVisitor = new ExprToJsonVisitor();
        expr.right.accept(rightVisitor);
        json.put("right", rightVisitor.getJson());
    }

    @Override
    public void visit(CellRef expr) {
        json = new JSONObject();
        json.put("type", expr.getClass().getSimpleName());
        json.put("ref", expr.getFormula());
        json.put("value", expr.eval());
    }
}