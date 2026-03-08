package pt.isel.mpd.spreadsheet0.pub_sub;

public interface Subscriber {
    void onValueChanged(Publisher src);
    void onCellDeleted(Publisher src);
}
