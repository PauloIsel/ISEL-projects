package pt.isel.mpd.spreadsheet1.visitors;

import pt.isel.mpd.spreadsheet1.expressions.BinExpr;
import pt.isel.mpd.spreadsheet1.expressions.Const;
import pt.isel.mpd.spreadsheet1.expressions.MemoExpr;
import pt.isel.mpd.spreadsheet1.expressions.NullExpr;
import pt.isel.mpd.spreadsheet1.model.Cell;

public abstract class DependencyBaseVisitor implements ExprVisitor {
    private final Cell cell;

    public DependencyBaseVisitor(Cell cell) {
        this.cell = cell;
    }

    @Override
    public void visit(BinExpr expr) {
        expr.left.accept(this);
        expr.right.accept(this);
    }

    @Override
    public void visit(Const expr) {
        // No dependencies to subscribe
    }

    @Override
    public void visit(NullExpr n) {
        // No dependencies to subscribe
    }

    @Override
    public void visit(MemoExpr expr) {
        expr.expr.accept(this);
    }

    protected Cell getCell() {
        return cell;
    }
}
