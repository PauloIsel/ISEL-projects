package pt.isel.mpd.spreadsheet0.visitors;
import pt.isel.mpd.spreadsheet0.expressions.BinExpr;
import pt.isel.mpd.spreadsheet0.expressions.Const;
import pt.isel.mpd.spreadsheet0.model.CellRef;

public class ExprPosfixVisitor implements ExprVisitor {
    private StringBuilder sb;

    public ExprPosfixVisitor(StringBuilder stringBuilder) {
        this.sb = stringBuilder;
    }

    @Override
    public void visit(BinExpr expr) {
        expr.left.accept(this);
        expr.right.accept(this);
        sb.append(expr.getOperator()).append(" ");
    }

    @Override
    public void visit(Const expr) {
        sb.append(expr.getFormula()).append(" ");
    }

    @Override
    public void visit(CellRef expr) {
        sb.append(expr.getFormula()).append(" ");
    }
}
