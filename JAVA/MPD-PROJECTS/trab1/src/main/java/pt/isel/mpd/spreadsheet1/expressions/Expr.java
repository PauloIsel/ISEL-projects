package pt.isel.mpd.spreadsheet1.expressions;
import pt.isel.mpd.part3.ParametersNames;
import pt.isel.mpd.spreadsheet1.exceptions.InvalidCoordinatesException;
import pt.isel.mpd.spreadsheet1.visitors.ExprVisitor;

public interface Expr {
    double eval();
    String getFormula();
    @ParametersNames("visitor")
    void accept(ExprVisitor visitor);
    
    /**
     * a new default methos, eventually useful to produce
     * the representation of an expression to show on SheetView
     * @return
     */

    default String getEvalText() {
        try {
            var value = eval();
            return Double.isNaN(value) ? "#REF!" : Double.toString(value);
        } catch (InvalidCoordinatesException e) {
            return "#REF!";
        }
    }
}
