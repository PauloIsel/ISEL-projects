package pt.isel.mpd.spreadsheet1.model;

import org.json.JSONArray;
import org.json.JSONObject;
import pt.isel.mpd.spreadsheet1.commands.CommandHistory;
import pt.isel.mpd.spreadsheet1.expressions.ExprFactory;
import pt.isel.mpd.spreadsheet1.expressions.NullExpr;
import pt.isel.mpd.spreadsheet1.visitors.ExprToJsonVisitor;
import pt.isel.mpd.spreadsheet1.expressions.Expr;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalcSheet extends AbstractCalcSheet  {
    private final CommandHistory history = new CommandHistory();
    private final List<List<Cell>> cells;

    public CommandHistory getSheetHistory() {
        return history;
    }

    public CalcSheet(int rows, int cols) {
        super(rows, cols);
        this.cells = new ArrayList<>();
        for(int row = 0; row < rows; row++) {
            List<Cell> cellRow = new ArrayList<>();
            for(int col = 0; col < cols; col++) {
                cellRow.add(new Cell(this, row, col));
            }
            cells.add(cellRow);
        }
    }

    @Override
    public Cell getCellAt(int row, int col) {
        checkCoords(row, col);
        return cells.get(row - 1).get(col - 1);
    }

    @Override
    public void setCellAt(int row, int col, Expr expr)  {
        checkCoords(row, col);
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
        if (rows >= 2) {
            cells.remove(rows - 1);
            rows--;
        } else {
            throw new IllegalArgumentException("Cannot remove the only existing row");
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
        if (cols >= 2) {
            for (int row = 0; row < rows; row++) {
                cells.get(row).remove(cols - 1);
            }
            cols--;
        } else {
            throw new IllegalArgumentException("Cannot remove the only existing column");
        }
    }

    public List<List<Cell>> getTableCells() {
        return cells;
    }

    public void setTableCells(List<List<Cell>> cells) {
        this.cells.clear();
        this.cells.addAll(cells);
        this.rows = cells.size();
        this.cols = cells.getFirst().size();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("rows", rows);
        json.put("cols", cols);
        JSONArray jsonCells = new JSONArray();

        for (List<Cell> row : cells) {
            for (Cell cell : row) {
                if (!(cell.getExpr() instanceof NullExpr)) {
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
            Expr expr = ExprFactory.convertFromJson(cellJson.getJSONObject("expr"), sheet);
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
