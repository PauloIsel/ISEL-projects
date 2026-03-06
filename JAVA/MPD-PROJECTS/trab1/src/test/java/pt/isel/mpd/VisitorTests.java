package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.spreadsheet0.expressions.Memo;
import pt.isel.mpd.spreadsheet0.parser.ParserExpr;
import pt.isel.mpd.spreadsheet0.visitors.ExprPosfixVisitor;

import java.io.IOException;

public class VisitorTests {
    @Test
    public void showExprWithAVisitor() throws IOException {
        StringBuilder sb = new StringBuilder();
        var parser = new ParserExpr();

        var expr = parser.parse("2*(3+5)");
        var memo = new Memo(expr, "2*(3+5)");

        var visitor = new ExprPosfixVisitor(sb);
        memo.accept(visitor);
        System.out.println(sb);
    }

    @Test
    public void showExprWithAVisitor2() throws IOException {
        StringBuilder sb = new StringBuilder();
        var parser = new ParserExpr();

        var expr = parser.parse("3*(4+23)");
        var memo = new Memo(expr, "3*(4+23)");

        var visitor = new ExprPosfixVisitor(sb);
        memo.accept(visitor);
        System.out.println(sb);
    }
}
