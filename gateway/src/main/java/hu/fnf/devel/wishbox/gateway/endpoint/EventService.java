/*
 *   EventService.java is part of the "wishbox ( gateway )" project
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
import hu.fnf.devel.wishbox.gateway.entity.Helper;
import hu.fnf.devel.wishbox.model.entity.Event;
import hu.fnf.devel.wishbox.model.entity.api.IEvent;
import hu.fnf.devel.wishbox.model.entity.api.IWish;
import hu.fnf.devel.wishbox.model.entity.mongo.UserMongo;
import hu.fnf.devel.wishbox.model.entity.mongo.WishMongo;
import hu.fnf.devel.wishbox.model.repository.mongo.EventMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.UserMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.WishMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping(Gateway.ROOT + "/event")
public class EventService {
    @Autowired
    private UserMongoRepository userRepository;
    @Autowired
    private EventMongoRepository eventRepository;
    @Autowired
    private WishMongoRepository wishRepository;
    @Autowired
    private Helper helper;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Event> getEventList(HttpSession session) {
        String uid = (String) session.getAttribute(Gateway.SUBJECT_ID);
        List<IWish> wishs = userRepository.findOne(uid).getWishes();
        return helper.getEventList(wishs);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteEvent(@PathVariable("id") String id, HttpSession session) {
        String uid = (String) session.getAttribute(Gateway.SUBJECT_ID);
        UserMongo user = userRepository.findOne(uid);
        for (IWish wish : user.getWishes()) {
            for (IEvent event : wish.getEvents()) {
                if (event.getId().equals(id)) {
                    WishMongo w = wishRepository.findOne(wish.getId());
                    w.removeEvent(event);
                    wishRepository.save(w);
                    eventRepository.delete(id);
                    return;
                }
            }
        }
    }
}
