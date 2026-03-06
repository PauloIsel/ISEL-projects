package pt.isel.mpd.spreadsheet0.expressions;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.exceptions.DivByZeroException;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;

public class Div extends BinExpr {
    
    public Div(Expr left, Expr right) {
        super(left, right);
    }
    
    @Override
    public String getOperator() {
        return "/";
    }
    
    @Override
    public double eval() {
        var r = right.eval();
        if (r == 0) {
            throw new DivByZeroException();
        }
        return left.eval() / r;
    }

    public static Div fromJson(JSONObject json, CalcSheet sheet) {
        Expr left = Expr.convertFromJson(json.getJSONObject("left"), sheet);
        Expr right = Expr.convertFromJson(json.getJSONObject("right"), sheet);
        return new Div(left, right);
    }
}
