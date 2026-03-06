package pt.isel.mpd.spreadsheet0.expressions;
import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.visitors.ExprVisitor;

public class Const implements Expr {
    public final double value;
    
    public Const(double value) {
        this.value = value;
    }
    
    @Override
    public double eval() {
        return value;
    }
    
    @Override
    public String getFormula() {
        return Double.valueOf(value).toString();
    }

    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }

    public static Const fromJson(JSONObject json) {
        return new Const(json.getDouble("value"));
    }
}
