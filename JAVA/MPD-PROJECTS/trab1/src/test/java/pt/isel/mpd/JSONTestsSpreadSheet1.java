package pt.isel.mpd;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.expressions.Const;
import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import pt.isel.mpd.spreadsheet1.expressions.Max;
import pt.isel.mpd.spreadsheet1.expressions.Sum;

import java.io.IOException;

public class JSONTestsSpreadSheet1 {
    public static void main(String[] args) {
        try {
            CalcSheet sheet = new CalcSheet(5, 5);
            sheet.setCellAt(3, 3, new Max(sheet, "A1", "B1"));
            sheet.setCellAt(1, 1, new Const(10));
            sheet.setCellAt(1, 2, new Const(20));
            sheet.setCellAt(3, 4, new Sum(sheet, "A1", "B1"));


            JSONObject json = sheet.toJson();
            System.out.println("JSON Serializado:");
            System.out.println(json.toString(2));

            String filePath = "test_sheet.json";
            sheet.saveToFile(filePath);
            System.out.println("Arquivo salvo em: " + filePath);

            CalcSheet loadedSheet = CalcSheet.loadFromFile(filePath);
            System.out.println("JSON Desserializado:");
            System.out.println(loadedSheet.toJson().toString(2));

            assert sheet.getCellAt(1, 1).getExpr().eval() == loadedSheet.getCellAt(1, 1).getExpr().eval(); // A1 = 10 V
            assert sheet.getCellAt(1, 2).getExpr().eval() == loadedSheet.getCellAt(1, 2).getExpr().eval(); // B1 = 20 V
            assert sheet.getCellAt(3, 3).getExpr().eval() == loadedSheet.getCellAt(3, 3).getExpr().eval();
            assert sheet.getCellAt(3, 4).getExpr().eval() == loadedSheet.getCellAt(3, 4).getExpr().eval(); // C3 = 30 V

            System.out.println("Teste concluído com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}