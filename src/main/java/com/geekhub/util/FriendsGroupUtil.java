package com.geekhub.util;

import com.geekhub.entity.FriendsGroup;
import com.geekhub.entity.User;
import com.geekhub.service.FriendsGroupService;
import com.geekhub.service.FriendsGroupServiceImpl;
import com.geekhub.service.UserService;
import com.sun.javafx.logging.JFRInputEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FriendsGroupUtil {

    @Autowired
    private FriendsGroupService friendsGroupService;

    @Autowired
    private UserService userService;

    public FriendsGroup createDefaultGroup() {
        FriendsGroup friendsGroup = new FriendsGroup();
        friendsGroup.setName("friends");
        friendsGroupService.save(friendsGroup);
        return friendsGroup;
    }

    public Long createFriendsGroup(Long userId, String name) {
        User user = userService.getById(userId);
        FriendsGroup group = new FriendsGroup(user, name);
        return friendsGroupService.save(group);
    }
}
