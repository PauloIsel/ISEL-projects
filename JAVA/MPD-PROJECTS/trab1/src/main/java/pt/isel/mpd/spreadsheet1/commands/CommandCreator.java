package pt.isel.mpd.spreadsheet1.commands;

import pt.isel.mpd.spreadsheet1.model.CalcSheet;

public interface CommandCreator {
    Command create(CalcSheet sheet);
}
