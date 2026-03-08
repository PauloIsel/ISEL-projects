package pt.isel.mpd.spreadsheet1.expressions;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public interface ExprCreator {
    Expr fromJson(JSONObject json, CalcSheet sheet);
}
