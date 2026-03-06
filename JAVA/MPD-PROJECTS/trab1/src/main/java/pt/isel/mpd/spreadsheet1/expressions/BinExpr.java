package pt.isel.mpd.spreadsheet1.expressions;

import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;

public abstract class BinExpr implements Expr {
    public final Expr left, right;

    @ParametersNames("left;right")
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
        // lots of stupid parenthesis thar aren't needed
        //        return "(" + left.getFormula() + ")" +
        //                   getOperator() +
        //               "(" + right.getFormula() + ")";
        return left.getFormula() +
                   getOperator() +
                   right.getFormula();
    }
    
    public abstract String getOperator();
    
    @Override
    public String toString() {
        return getEvalText();
    }

    @ParametersNames("visitor")
    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }
}
