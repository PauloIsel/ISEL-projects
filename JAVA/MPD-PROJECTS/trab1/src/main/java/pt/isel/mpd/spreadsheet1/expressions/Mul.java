package pt.isel.mpd.spreadsheet1.expressions;


import org.json.JSONObject;
import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public class Mul extends BinExpr {
    @ParametersNames("left;right")
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

    @ParametersNames("json;sheet")
    public static Mul fromJson(JSONObject json, CalcSheet sheet) {
        Expr left = ExprFactory.convertFromJson(json.getJSONObject("left"), sheet);
        Expr right = ExprFactory.convertFromJson(json.getJSONObject("right"), sheet);
        return new Mul(left, right);
    }
}
