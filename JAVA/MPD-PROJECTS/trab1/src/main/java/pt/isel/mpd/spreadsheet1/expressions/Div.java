package pt.isel.mpd.spreadsheet1.expressions;

import org.json.JSONObject;
import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.exceptions.DivByZeroException;

public class Div extends BinExpr {

    @ParametersNames("left;right")
    public Div(Expr left, Expr right) {
        super(left, right);
        if (right.eval() == 0) {
            throw new ArithmeticException("Div by zero!");
        }
    }
    
    @Override
    public String getOperator() {
        return "/";
    }
    
    @Override
    public double eval() {
        var r = right.eval();
        if (r== 0.0) throw new DivByZeroException();
        return left.eval() / r;
    }

    @ParametersNames("json;sheet")
    public static Div fromJson(JSONObject json, CalcSheet sheet) {
        Expr left = ExprFactory.convertFromJson(json.getJSONObject("left"), sheet);
        Expr right = ExprFactory.convertFromJson(json.getJSONObject("right"), sheet);
        return new Div(left, right);
    }
    
}
