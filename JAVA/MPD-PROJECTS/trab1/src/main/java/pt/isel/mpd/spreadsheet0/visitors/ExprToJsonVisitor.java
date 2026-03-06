package pt.isel.mpd.spreadsheet0.visitors;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.expressions.BinExpr;
import pt.isel.mpd.spreadsheet0.expressions.Const;
import pt.isel.mpd.spreadsheet0.model.CellRef;

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