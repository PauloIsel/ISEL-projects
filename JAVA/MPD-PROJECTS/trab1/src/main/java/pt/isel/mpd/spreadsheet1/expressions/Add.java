package pt.isel.mpd.spreadsheet1.expressions;

import org.json.JSONObject;
import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;

/**
 *
 */
public class Add extends BinExpr {
    
    /**
     *
     * @param left
     * @param right
     */
    @ParametersNames("left;right")
    public Add(Expr left, Expr right) {
        super(left, right);
    }
    
    @Override
    public String getOperator() {
        return "+";
    }
    
    @Override
    public double eval() {
        return left.eval() + right.eval();
    }

    @ParametersNames("json;sheet")
    public static Add fromJson(JSONObject json, CalcSheet sheet) {
        Expr left = ExprFactory.convertFromJson(json.getJSONObject("left"), sheet);
        Expr right = ExprFactory.convertFromJson(json.getJSONObject("right"), sheet);
        return new Add(left, right);
    }
}
