package pt.isel.mpd.spreadsheet0.visitors;

import pt.isel.mpd.spreadsheet0.expressions.BinExpr;
import pt.isel.mpd.spreadsheet0.expressions.Const;
import pt.isel.mpd.spreadsheet0.model.Cell;

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

    protected Cell getCell() {
        return cell;
    }
}
