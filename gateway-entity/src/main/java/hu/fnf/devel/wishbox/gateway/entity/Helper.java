package hu.fnf.devel.wishbox.gateway.entity;

import hu.fnf.devel.wishbox.model.entity.Event;
import hu.fnf.devel.wishbox.model.entity.api.IEvent;
import hu.fnf.devel.wishbox.model.entity.api.IWish;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Helper {
    public List<Event> getEventList(List<IWish> wishs) {
        List<Event> events = new ArrayList<>();
        for (IWish wish : wishs) {
            List<IEvent> eventList = wish.getEvents();
            for (IEvent e : eventList) {
                events.add(new Event(e));
            }
        }
        return events;
    }
}
