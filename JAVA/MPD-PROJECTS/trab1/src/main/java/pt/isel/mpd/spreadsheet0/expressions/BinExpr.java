package pt.isel.mpd.spreadsheet0.expressions;


import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;
import pt.isel.mpd.spreadsheet0.model.CellRef;
import pt.isel.mpd.spreadsheet0.visitors.ExprVisitor;

public abstract class BinExpr implements Expr {
    public final Expr left, right;
    
    protected BinExpr(Expr left, Expr right) {
        this.left = left; this.right = right;
    }
    
    /**
     * Returns the formula of the expression. using
     * the Template Method Pattern, where the getOperator method,
     * override by subclasses, is used to get the operator.
     * @return
     */
    public final String getFormula() {
        // parenthesis could be added but in this way we have
        // lots of stupid parenthesis that aren't needed
        //        return "(" + left.getFormula() + ")" +
        //                   getOperator() +
        //               "(" + right.getFormula() + ")";
        return left.getFormula() +
                   getOperator() +
               right.getFormula();
    }
    
    public abstract String getOperator();

    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }

    public static Expr fromJson(JSONObject json, CalcSheet sheet) {
        String type = json.getString("type");
        return switch (type) {
            case "Const" -> Const.fromJson(json);
            case "CellRef" -> CellRef.fromJson(json, sheet);
            case "Add" -> Add.fromJson(json, sheet);
            case "Sub" -> Sub.fromJson(json, sheet);
            case "Mul" -> Mul.fromJson(json, sheet);
            case "Div" -> Div.fromJson(json, sheet);
            case "Memo" -> Memo.fromJson(json, sheet);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

}
