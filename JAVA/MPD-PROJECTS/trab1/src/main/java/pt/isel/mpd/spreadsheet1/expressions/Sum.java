package pt.isel.mpd.spreadsheet1.expressions;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public class Sum extends RangeExpr {

    public Sum(CalcSheet sheet, String startRange, String endRange) {
        super(sheet, startRange, endRange);
    }

    @Override
    public double eval() {
        double result = 0.0;
        for (int row = coordsStart.row(); row <= coordsEnd.row(); row++) {
            for (int col = coordsStart.col(); col <= coordsEnd.col(); col++) {
                result += sheet.getCellAt(row, col).eval();
            }
        }
        return result;
    }

    @Override
    public String getOperator() {
        return "SUM";
    }

    public static Sum fromJson(JSONObject json, CalcSheet sheet) {
        String startRange = json.getString("startRange");
        String endRange = json.getString("endRange");
        return new Sum(sheet, startRange, endRange);
    }
}
