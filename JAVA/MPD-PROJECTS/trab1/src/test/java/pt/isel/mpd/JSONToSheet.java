package pt.isel.mpd;

import pt.isel.mpd.spreadsheet0.model.CalcSheet;

import java.io.IOException;

public class JSONToSheet {
    public static void main(String[] args) {
        try {
            String filePath = "test_sheet.json";

            CalcSheet loadedSheet = CalcSheet.loadFromFile(filePath);

            System.out.println("Valores das células carregadas:");
            for (int row = 0; row < loadedSheet.getRows(); row++) {
                for (int col = 0; col < loadedSheet.getCols(); col++) {
                    try {
                        System.out.print(loadedSheet.getExprAt(row, col).eval() + "\t");
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
