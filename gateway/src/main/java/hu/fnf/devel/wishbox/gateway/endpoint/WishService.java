/*
 *   WishService.java is part of the "wishbox ( gateway )" project
 *   Copyright (C)  2015  author:  johnnym
 *
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package hu.fnf.devel.wishbox.gateway.endpoint;

import hu.fnf.devel.wishbox.Gateway;
import hu.fnf.devel.wishbox.model.entity.Event;
import hu.fnf.devel.wishbox.model.entity.Priority;
import hu.fnf.devel.wishbox.model.entity.Wish;
import hu.fnf.devel.wishbox.model.entity.api.IEvent;
import hu.fnf.devel.wishbox.model.entity.api.IWish;
import hu.fnf.devel.wishbox.model.entity.mongo.EventMongo;
import hu.fnf.devel.wishbox.model.entity.mongo.NotificationMongo;
import hu.fnf.devel.wishbox.model.entity.mongo.UserMongo;
import hu.fnf.devel.wishbox.model.entity.mongo.WishMongo;
import hu.fnf.devel.wishbox.model.repository.mongo.EventMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.NotificationMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.UserMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.WishMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(Gateway.ROOT + "/wish")
public class WishService {

    @Autowired
    private UserMongoRepository userRepository;
    @Autowired
    private WishMongoRepository wishRepository;
    @Autowired
    private NotificationMongoRepository notificationRepository;
    @Autowired
    private EventMongoRepository eventRepository;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Wish> getWishList(HttpSession session) {
        String sid = (String) session.getAttribute(Gateway.SUBJECT_ID);
        List<Wish> wishs = new ArrayList<>();
        for (IWish wish : userRepository.findOne(sid).getWishes()) {
            wishs.add(new Wish(wish));
        }
        return wishs;
    }

    @RequestMapping(value = "/{id}/event", method = RequestMethod.GET)
    @ResponseBody
    public List<Event> getEventList(@PathVariable("id") String id, HttpSession session) {
        String uid = (String) session.getAttribute(Gateway.SUBJECT_ID);
        // TODO: optimalize!
        for (IWish w : userRepository.findOne(uid).getWishes()) {
            if (w.getId().equals(id)) {
                List<Event> events = new ArrayList<>();
                for (IEvent event : w.getEvents()) {
                    events.add(new Event(event));
                }
                return events;
            }
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void addWish(@RequestBody Wish w, HttpSession session) {
        WishMongo wish = new WishMongo(w);
        NotificationMongo notification = new NotificationMongo();
        notification.setText("New Wish: " + "\"" + wish.getLabel() + "\"");
        notification.setPriority(Priority.INFO);
        notificationRepository.save(notification);

        EventMongo event = new EventMongo();
        event.setTitle(wish.getLabel());
        event.setText("New Wish has been made with label \"" + wish.getLabel() +
                "\". The label automatically has been added as a search keyword. You can add relevant information to the Wish at any time.");
        event.setPriority(Priority.INFO);
        event.setIcon("magic");
        eventRepository.save(event);

        wish.addEvent(event);
        wishRepository.save(wish);

        String uid = (String) session.getAttribute(Gateway.SUBJECT_ID);
        UserMongo user = userRepository.findOne(uid);
        user.addNotification(notification);
        user.addWish(wish);
        userRepository.save(user);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteWish(@PathVariable("id") String id, HttpSession session) {
        String uid = (String) session.getAttribute(Gateway.SUBJECT_ID);
        UserMongo user = userRepository.findOne(uid);
        for (IWish wish : user.getWishes()) {
            if (wish.getId().equals(id)) {
                user.removeWish(wish);
                wishRepository.delete(id);
                NotificationMongo notification = new NotificationMongo();
                notification.setText("Deleted Wish: " + "\"" + wish.getLabel() + "\"");
                notification.setPriority(Priority.WARNING);
                notificationRepository.save(notification);

                user.addNotification(notification);
                userRepository.save(user);
                return;
            }
        }
    }
}
