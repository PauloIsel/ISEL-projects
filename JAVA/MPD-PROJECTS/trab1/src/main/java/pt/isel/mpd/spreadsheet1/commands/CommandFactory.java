package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;
import java.util.Map;

public class CommandFactory {

    private static Map<String, CommandCreator> getCommands() {
        return Map.of(
                "Add Line", AddLineCommand::create,
                "Add Col", AddColCommand::create,
                "Remove Line", RemoveLineCommand::create,
                "Remove Col", RemoveColCommand::create,
                "Open", OpenCommand::create,
                "Save", SaveCommand::create,
                "Clear", ClearCommand::create,
                "Undo", UndoCommand::create
        );
    }

    public static Command createCommand(String commandName, CalcSheet sheet) {
        CommandCreator cmd = getCommands().get(commandName);
        return cmd == null ? null : cmd.create(sheet);
    }
}
