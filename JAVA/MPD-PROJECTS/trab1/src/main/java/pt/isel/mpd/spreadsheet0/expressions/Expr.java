package pt.isel.mpd.spreadsheet0.expressions;
import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;
import pt.isel.mpd.spreadsheet0.visitors.ExprVisitor;

public interface Expr {
    double eval();
    String getFormula();
    void accept(ExprVisitor visitor);

    static Expr convertFromJson(JSONObject json, CalcSheet sheet) {
        return BinExpr.fromJson(json, sheet);
    }
}
