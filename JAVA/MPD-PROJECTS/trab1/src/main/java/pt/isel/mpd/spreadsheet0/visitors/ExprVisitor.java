package pt.isel.mpd.spreadsheet0.visitors;
import pt.isel.mpd.spreadsheet0.expressions.BinExpr;
import pt.isel.mpd.spreadsheet0.expressions.Const;
import pt.isel.mpd.spreadsheet0.model.CellRef;

public interface ExprVisitor {
    void visit(BinExpr expr);
    void visit(Const expr);
    void visit(CellRef expr);
}
