package pt.isel.mpd.spreadsheet1.pub_sub;

public interface Subscriber {
    void onValueChanged(Publisher src);
}
