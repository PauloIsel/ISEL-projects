package pt.isel.mpd;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

import java.io.IOException;

public class JSONToSheetSpreadSheet1 {
    public static void main(String[] args) {
        try {
            String filePath = "test_sheet.json";

            CalcSheet loadedSheet = CalcSheet.loadFromFile(filePath);

            System.out.println("Valores das células carregadas:");
            for (int row = 0; row < loadedSheet.getRowCount(); row++) {
                for (int col = 0; col < loadedSheet.getColumnCount(); col++) {
                    try {
                        System.out.print(loadedSheet.getCellAt(row, col).getExpr().eval() + "\t");
                    } catch (Exception e) {
                        System.out.print("--\t"); // Célula vazia ou inválida
                    }
                }
                System.out.println();
            }

            System.out.println("Sheet recriada com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao carregar a planilha: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
