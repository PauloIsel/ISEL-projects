package pt.isel.mpd.spreadsheet1.model;

import pt.isel.mpd.spreadsheet1.expressions.Expr;
import pt.isel.mpd.spreadsheet1.expressions.NullExpr;
import pt.isel.mpd.spreadsheet1.pub_sub.Publisher;
import pt.isel.mpd.spreadsheet1.pub_sub.PublisherImpl;
import pt.isel.mpd.spreadsheet1.pub_sub.Subscriber;
import pt.isel.mpd.spreadsheet1.visitors.DependencySubscriberVisitor;
import pt.isel.mpd.spreadsheet1.visitors.DependencyUnsubscriberVisitor;

import java.util.Optional;

public class Cell extends PublisherImpl implements Subscriber {
    public final CalcSheet sheet;
    public final int col, row;
    private Expr expr;
    private Optional<Double> value;

    public Cell(CalcSheet sheet, int row, int col) {
        this.sheet = sheet;
        this.col = col + 1;
        this.row = row + 1;
        value = Optional.empty();
        expr = new NullExpr();
    }

    public Expr getExpr() {
        return expr;
    }

    public double eval() {
        value = value.or(() -> Optional.of(expr.eval()));
        return value.get();
    }

    public String getFormula() {
        return expr.getFormula();
    }

    public void setValue(Expr expr) {
        Expr oldexpr = this.expr;

        this.expr.accept(new DependencyUnsubscriberVisitor(this));
        this.expr = expr;
        value = Optional.empty();

        try {
            expr.eval();
            this.expr.accept(new DependencySubscriberVisitor(this));
            this.valueChanged();

            sheet.fireTableCellUpdated(row, col);
        } catch (StackOverflowError e) {
            this.expr = oldexpr;
            this.expr.accept(new DependencySubscriberVisitor(this));
            throw new RuntimeException("Circular reference detected");
        }
    }

    @Override
    public void onValueChanged(Publisher src) {
        value = Optional.empty();
        sheet.fireTableCellUpdated(row, col);
    }

}
