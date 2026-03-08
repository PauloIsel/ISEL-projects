package pt.isel.mpd.spreadsheet1.pub_sub;

import java.util.ArrayList;
import java.util.List;

public class PublisherImpl implements Publisher {
    
    private final List<Subscriber> subscribers = new ArrayList<>();
    @Override
    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }
    
    @Override
    public void unSubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void valueChanged() {
        for(var s : subscribers) {
            s.onValueChanged(this);
        }
    }
}
