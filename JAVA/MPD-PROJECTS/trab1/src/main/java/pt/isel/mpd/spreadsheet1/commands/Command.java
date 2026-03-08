package pt.isel.mpd.spreadsheet1.commands;

public interface Command {
    boolean execute();
    void undo();
}
