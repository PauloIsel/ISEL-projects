package pt.isel.mpd.spreadsheet0.expressions;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;
import pt.isel.mpd.spreadsheet0.visitors.ExprVisitor;

public class Memo implements Expr {
    private final String formula;
    private final Expr expr;
    
    public Memo(Expr expr, String formula) {
        this.expr = expr;
        this.formula = formula;
    }
    
    @Override
    public double eval() {
        return expr.eval();
    }
    
    @Override
    public String getFormula() {
        return formula;
    }

    @Override
    public void accept(ExprVisitor visitor) {
        expr.accept(visitor);
    }

    public static Memo fromJson(JSONObject json, CalcSheet sheet) {
        Expr expr = Expr.convertFromJson(json.getJSONObject("expr"), sheet);
        String formula = json.getString("formula");
        return new Memo(expr, formula);
    }
}
