package pt.isel.mpd.spreadsheet1.expressions;


import org.json.JSONObject;
import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;


public class MemoExpr implements Expr {
    public final Expr expr;
    public final String formula;

    @ParametersNames("formula;expr")
    public MemoExpr(String formula, Expr expr) {
        this.formula = formula;
        this.expr = expr;
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
    public String toString() {
        return getEvalText();
    }

    @ParametersNames("visitor")
    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }

    @ParametersNames("json;sheet")
    public static MemoExpr fromJson(JSONObject json, CalcSheet sheet) {
        Expr expr = ExprFactory.convertFromJson(json.getJSONObject("expr"), sheet);
        String formula = json.getString("formula");
        return new MemoExpr(formula, expr);
    }
}
