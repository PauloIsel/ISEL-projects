package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public class UndoCommand implements Command {
    private final CommandHistory history;

    public UndoCommand(CalcSheet sheet) {
        this.history = sheet.getSheetHistory();
    }

    public static Command create(CalcSheet sheet) {
        return new UndoCommand(sheet);
    }

    @Override
    public boolean execute() {
        if(history.isEmpty()) throw new IllegalStateException("No commands to undo");
        Command cmd = history.pop();
        if (cmd != null) cmd.undo();
        return false;
    }

    @Override
    public void undo() {}
}
