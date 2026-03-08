package pt.isel.mpd.spreadsheet0.model;

import org.json.JSONArray;
import org.json.JSONObject;
import pt.isel.mpd.spreadsheet0.exceptions.InvalidCoordinatesException;
import pt.isel.mpd.spreadsheet0.expressions.*;
import pt.isel.mpd.spreadsheet0.visitors.ExprToJsonVisitor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalcSheet {
    private static final int MAX_COLS = 26;
    private final List<List<Cell>> cells;
    private int rows, cols;
    
    public  record  CellCoords(int row, int col){ }
    
    boolean validCoords(int row, int col) {
        return row>= 0 && row < rows && col >= 0 && col < cols;
    }
    
    /**
     * get thw coordinates (row,col) given the name (ex: B51) of a CellRef
     * @param name
     * @return
     */
    public CellCoords coordsFromName(String name) {
        int col=-1, row=-1;
        try {
            name = name.toUpperCase();
            col = name.charAt(0) - 'A';
            row = Integer.parseInt(name.substring(1))-1;
        }
        catch(NumberFormatException ignored) { }
        if (!validCoords(row,col)) throw new InvalidCoordinatesException();
        return new CellCoords(row,col);
    }
    
    public CalcSheet(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        this.cells = new ArrayList<>();
        for(int row = 0; row < rows; row++) {
            List<Cell> cellRow = new ArrayList<>();
            for(int col = 0; col < cols; col++) {
                cellRow.add(new Cell(this, row, col));
            }
            cells.add(cellRow);
        }
    }

    public Cell getCellAt(int row, int col) {
       if (!validCoords(row,col)) {
           throw new InvalidCoordinatesException();
       }
       return cells.get(row).get(col);
    }

    public Expr getExprAt(int row, int col) {
        return getCellAt(row,col).getExpr();
    }
    
    public String getFormulaAt(int row, int col) {
        return getCellAt(row,col).getFormula();
    }
    
    public void setCellAt(int row, int col, Expr expr)  {
        if (!validCoords(row,col)) {
            throw new InvalidCoordinatesException();
        }
        getCellAt(row,col).setValue(expr);
    }

    public void addRow() {
        List<Cell> newRow = new ArrayList<>();
        for (int col = 0; col < cols; col++) {
            newRow.add(new Cell(this, rows, col));
        }
        cells.add(newRow);
        rows++;
    }

    public void removeRow() {
        if (rows > 0) {
            cells.remove(rows - 1);
            rows--;
        }
    }

    public void addColumn() {
        if (cols >= MAX_COLS) {
            throw new IllegalArgumentException("Cannot add more than " + MAX_COLS + " columns");
        }

        for (int row = 0; row < rows; row++) {
            cells.get(row).add(new Cell(this, row, cols));
        }
        cols++;
    }

    public void removeColumn() {
        if (cols > 0) {
            for (int row = 0; row < rows; row++) {
                cells.get(row).remove(cols - 1);
            }
            cols--;
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("rows", rows);
        json.put("cols", cols);
        JSONArray jsonCells = new JSONArray();

        for (List<Cell> row : cells) {
            for (Cell cell : row) {
                if (cell.getExpr() != null) {
                    JSONObject cellJson = new JSONObject();
                    cellJson.put("row", cell.row);
                    cellJson.put("col", cell.col);
                    cellJson.put("expr", exprToJson(cell.getExpr()));
                    jsonCells.put(cellJson);
                }
            }
        }

        json.put("cells", jsonCells);
        return json;
    }

    public static CalcSheet fromJson(JSONObject json) {
        int rows = json.getInt("rows");
        int cols = json.getInt("cols");
        CalcSheet sheet = new CalcSheet(rows, cols);

        JSONArray jsonCells = json.getJSONArray("cells");
        for (int i = 0; i < jsonCells.length(); i++) {
            JSONObject cellJson = jsonCells.getJSONObject(i);
            int row = cellJson.getInt("row");
            int col = cellJson.getInt("col");
            Expr expr = Expr.convertFromJson(cellJson.getJSONObject("expr"), sheet);
            sheet.setCellAt(row, col, expr);
        }
        return sheet;
    }

    public void saveToFile(String filePath) throws IOException {
        try (Writer writer = new FileWriter(filePath)) {
            writer.write(toJson().toString(2));
        }
    }

    public static CalcSheet loadFromFile(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
            return fromJson(new JSONObject(sb.toString()));
        }
    }

    private static JSONObject exprToJson(Expr expr) {
        ExprToJsonVisitor visitor = new ExprToJsonVisitor();
        expr.accept(visitor);
        return visitor.getJson();
    }

}
