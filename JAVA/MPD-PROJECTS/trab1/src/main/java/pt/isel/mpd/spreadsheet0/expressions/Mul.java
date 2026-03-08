package pt.isel.mpd.spreadsheet0.expressions;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;

public class Mul extends BinExpr {
    
    public Mul(Expr left, Expr right) {
        super(left, right);
    }
    
    @Override
    public String getOperator() {
        return "*";
    }
    
    @Override
    public double eval() {
        return left.eval()*right.eval();
    }

    public static Mul fromJson(JSONObject json, CalcSheet sheet) {
        Expr left = Expr.convertFromJson(json.getJSONObject("left"), sheet);
        Expr right = Expr.convertFromJson(json.getJSONObject("right"), sheet);
        return new Mul(left, right);
    }
}
