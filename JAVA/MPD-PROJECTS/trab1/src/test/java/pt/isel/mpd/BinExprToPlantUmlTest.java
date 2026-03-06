package pt.isel.mpd;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.part3.PlantUmlBuilder;
import pt.isel.mpd.spreadsheet1.expressions.*;

public class BinExprToPlantUmlTest {

    @Test
    public void BinExprPlantUmlTest() {
        var builder = new
                PlantUmlBuilder("expressions.puml");
        var classes = new Class<?>[] {
                Add.class, Sub.class, Mul.class,
                Div.class, MemoExpr.class, Const.class
        };

        builder
                .addTypes(classes)
                .start()
                .emmitTypes()
                .emmitImplAssociations()
                .emmitInheritanceAssociations()
                .emmitFieldAssociations()
                .end();
    }
}
