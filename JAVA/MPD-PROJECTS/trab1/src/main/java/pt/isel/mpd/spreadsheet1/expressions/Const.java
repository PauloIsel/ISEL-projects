package pt.isel.mpd.spreadsheet1.expressions;

import org.json.JSONObject;
import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;

public class Const implements Expr {
    public final double value;

    @ParametersNames("value")
    public Const(double value) {
        this.value = value;
        
    }
    public double eval()  {
        return value;
    }
  
    
    @Override
    public String getFormula() {
        return Double.valueOf(value).toString();
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
    public static Const fromJson(JSONObject json, CalcSheet sheet) {
        return new Const(json.getDouble("value"));
    }

}
