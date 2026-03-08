package pt.isel.mpd.spreadsheet1.expressions;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.model.CellRef;

import java.util.Map;

public class ExprFactory {

    static Map<String, ExprCreator> getType() {
        return Map.of(
                "Const", Const::fromJson,
                "CellRef", CellRef::fromJson,
                "Add", Add::fromJson,
                "Sub", Sub::fromJson,
                "Mul", Mul::fromJson,
                "Div", Div::fromJson,
                "Memo", MemoExpr::fromJson,
                "Max", Max::fromJson,
                "Sum", Sum::fromJson
        );
    }

    public static Expr convertFromJson(JSONObject json, CalcSheet sheet) {
        ExprCreator creator = getType().get(json.getString("type"));
        return creator == null ? null : creator.fromJson(json, sheet);
    }
}
