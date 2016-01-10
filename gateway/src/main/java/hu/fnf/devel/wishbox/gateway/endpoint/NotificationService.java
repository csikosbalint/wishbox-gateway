/*
 *   NotificationService.java is part of the "wishbox ( gateway )" project
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
import hu.fnf.devel.wishbox.model.entity.Notification;
import hu.fnf.devel.wishbox.model.entity.User;
import hu.fnf.devel.wishbox.model.entity.Wish;
import hu.fnf.devel.wishbox.model.entity.api.INotification;
import hu.fnf.devel.wishbox.model.entity.api.IWish;
import hu.fnf.devel.wishbox.model.entity.mongo.UserMongo;
import hu.fnf.devel.wishbox.model.entity.mongo.WishMongo;
import hu.fnf.devel.wishbox.model.repository.mongo.NotificationMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.UserMongoRepository;
import hu.fnf.devel.wishbox.model.repository.mongo.WishMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(Gateway.ROOT + "/notification")
public class NotificationService {

    @Autowired
    private UserMongoRepository userRepository;
    @Autowired
    private WishMongoRepository wishRepository;
    @Autowired
    private NotificationMongoRepository notificationRepository;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Notification> getNotificationList(HttpSession session) {
        String id = (String) session.getAttribute( Gateway.SUBJECT_ID );
        UserMongo user = userRepository.findOne(id);
        List<Notification> notifications = new ArrayList<>();
        for (INotification notification : user.getNotifications() ) {
            notifications.add(new Notification(notification));
        }
        for (IWish wish : user.getWishes()) {
            for (INotification notification : wish.getNotifications() ) {
                notifications.add(new Notification(notification));
            }
        }
        return notifications;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteNotification(@PathVariable("id") String id, HttpSession session) {
        String uid = (String) session.getAttribute( Gateway.SUBJECT_ID );
        UserMongo user = userRepository.findOne(uid);
        for (INotification notification : user.getNotifications()) {
            if (notification.getId().equals(id)) {
                UserMongo u = userRepository.findOne(user.getId());
                u.removeNotification(notification);
                userRepository.save(u);
                notificationRepository.delete(id);
                return;
            }
        }
        for (IWish wish : user.getWishes()) {
            for (INotification notification : wish.getNotifications()) {
                if (notification.getId().equals(id)) {
                    WishMongo w = wishRepository.findOne(wish.getId());
                    w.removeNotification(notification);
                    wishRepository.save(w);
                    notificationRepository.delete(id);
                    return;
                }
            }
        }
    }
}
