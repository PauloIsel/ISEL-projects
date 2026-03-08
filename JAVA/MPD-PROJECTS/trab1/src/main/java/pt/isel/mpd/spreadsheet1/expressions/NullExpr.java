package pt.isel.mpd.spreadsheet1.expressions;

import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;

public class NullExpr implements Expr {
    public NullExpr() {}

    @Override
    public double eval()  {
        return 0.0;
    }

    @Override
    public String getFormula() {
        return "";
    }

    @Override
    public void accept(ExprVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "";
    }
}
