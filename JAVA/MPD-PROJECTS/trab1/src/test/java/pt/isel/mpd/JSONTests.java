package pt.isel.mpd;

import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.expressions.Add;
import pt.isel.mpd.spreadsheet0.expressions.Const;
import pt.isel.mpd.spreadsheet0.expressions.Mul;
import pt.isel.mpd.spreadsheet0.model.CalcSheet;
import pt.isel.mpd.spreadsheet0.model.CellRef;

import java.io.IOException;

public class JSONTests {
    public static void main(String[] args) {
        try {
            CalcSheet sheet = new CalcSheet(5, 5);
            sheet.setCellAt(0, 0, new Const(10));
            sheet.setCellAt(0, 1, new Const(20));
            sheet.setCellAt(0, 2, new Add(sheet.getExprAt(0, 0), sheet.getExprAt(0, 1)));
            sheet.setCellAt(1, 0, new CellRef("A1", sheet));
            sheet.setCellAt(1, 1, new Mul(sheet.getExprAt(1, 0), sheet.getExprAt(0, 1)));

            JSONObject json = sheet.toJson();
            System.out.println("JSON Serializado:");
            System.out.println(json.toString(2));

            String filePath = "test_sheet.json";
            sheet.saveToFile(filePath);
            System.out.println("Arquivo salvo em: " + filePath);

            CalcSheet loadedSheet = CalcSheet.loadFromFile(filePath);
            System.out.println("JSON Desserializado:");
            System.out.println(loadedSheet.toJson().toString(2));

            assert sheet.getExprAt(0, 0).eval() == loadedSheet.getExprAt(0, 0).eval(); // A1 = 10 V
            assert sheet.getExprAt(0, 1).eval() == loadedSheet.getExprAt(0, 1).eval(); // B1 = 20 V
            assert sheet.getExprAt(0, 2).eval() == loadedSheet.getExprAt(0, 2).eval(); // C1 = A1 + B1 = 30 X
            assert sheet.getExprAt(1, 0).eval() == loadedSheet.getExprAt(1, 0).eval(); // A2 = A1 = 10 V
            assert sheet.getExprAt(1, 1).eval() == loadedSheet.getExprAt(1, 1).eval(); // B2 = A2 * B1 = 200 X

            System.out.println("Teste concluído com sucesso!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
