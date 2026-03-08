package pt.isel.mpd.spreadsheet0.model;

import pt.isel.mpd.spreadsheet0.expressions.Const;
import pt.isel.mpd.spreadsheet0.expressions.Expr;
import pt.isel.mpd.spreadsheet0.pub_sub.Publisher;
import pt.isel.mpd.spreadsheet0.pub_sub.PublisherImpl;
import pt.isel.mpd.spreadsheet0.pub_sub.Subscriber;
import pt.isel.mpd.spreadsheet0.visitors.DependencySubscriberVisitor;
import pt.isel.mpd.spreadsheet0.visitors.DependencyUnsubscriberVisitor;

import java.util.Optional;

public class Cell extends PublisherImpl implements Subscriber {
    public final CalcSheet sheet;
    public final int col, row;
    private Expr expr;
    private Optional<Double> value;
    
    public Cell(CalcSheet sheet, int row, int col) {
        this.sheet = sheet;
        this.col = col;
        this.row = row;
        value = Optional.empty();
        expr = new Const(0);
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
        expr.accept(new DependencyUnsubscriberVisitor(this));
        this.expr = expr;
        value = Optional.empty();

        expr.accept(new DependencySubscriberVisitor(this));
        this.valueChanged();
    }

    @Override
    public void onValueChanged(Publisher src) {
        value = Optional.empty();
    }

    @Override
    public void onCellDeleted(Publisher src) {

    }
}
