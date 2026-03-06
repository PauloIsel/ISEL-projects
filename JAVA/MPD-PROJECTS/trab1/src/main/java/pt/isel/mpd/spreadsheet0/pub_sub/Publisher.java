package pt.isel.mpd.spreadsheet0.pub_sub;

public interface Publisher {
    void subscribe(Subscriber subscriber);
    void unSubscribe(Subscriber subscriber);
    void valueChanged();
    void cellDeleted();
}
