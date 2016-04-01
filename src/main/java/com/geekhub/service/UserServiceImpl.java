package com.geekhub.service;

import com.geekhub.dao.UserDao;
import com.geekhub.entity.FriendsGroup;
import com.geekhub.entity.Message;
import com.geekhub.entity.User;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FriendsGroupService friendsGroupService;

    @Override
    public List<User> getAll(String orderParameter) {
        return userDao.getAll(orderParameter);
    }

    @Override
    public User getById(Long id) {
        return userDao.getById(id);
    }

    @Override
    public User get(String propertyName, Object value) {
        return userDao.get(propertyName, value);
    }

    @Override
    public Long save(User entity) {
        return userDao.save(entity);
    }

    @Override
    public Long createUser(String firstName, String lastName, String login, String password) {
        User user = new User(firstName, lastName, password, login);
        return userDao.save(user);
    }

    @Override
    public void update(User entity) {
        userDao.update(entity);
    }

    @Override
    public void delete(User entity) {
        userDao.delete(entity);
    }

    @Override
    public void delete(Long entityId) {
        userDao.delete(entityId);
    }

    @Override
    public User getByLogin(String login) {
        return userDao.get("login", login);
    }

    @Override
    public void addMessage(Long userId, Message message) {
        User user = userDao.getById(userId);
        user.getMessageSet().add(message);
        userDao.update(user);
    }

    @Override
    public void deleteMessage(Long userId, Long messageId) {
        Message message = messageService.getById(messageId);
        User user = userDao.getById(userId);
        user.getMessageSet().remove(message);
        userDao.update(user);
    }

    @Override
    public Set<User> getFriends(Long userId) {
        User user = userDao.getById(userId);
        return user.getFriends();
    }

    @Override
    public FriendsGroup getFriendsGroupByName(Long ownerId, String groupName) {
        User owner = userDao.getById(ownerId);
        return friendsGroupService.getFriendsGroups(owner, "name", groupName).get(0);
    }

    @Override
    public List<FriendsGroup> getAllFriendsGroups(Long ownerId) {
        User owner = userDao.getById(ownerId);
        return owner.getFriendsGroups().stream().collect(Collectors.toList());
    }

    @Override
    public List<FriendsGroup> getGroupsByOwnerAndFriend(Long ownerId, User friend) {
        User owner = userDao.getById(ownerId);
        return friendsGroupService.getByOwnerAndFriend(owner, friend);
    }

    @Override
    public void addFriendsGroup(Long ownerId, FriendsGroup group) {
        User user = userDao.getById(ownerId);
        if (user.getFriendsGroups().stream().noneMatch(fg -> fg.getName().equals(group.getName()))) {
            user.getFriendsGroups().add(group);
        } else {
            throw new HibernateException("Friends Group with such name already exist");
        }
        userDao.update(user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = userDao.getById(userId);
        User friend = userDao.getById(friendId);
        user.getFriends().add(friend);
        userDao.update(user);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        User user = userDao.getById(userId);
        User friend = userDao.getById(friendId);
        user.getFriends().remove(friend);
        userDao.update(user);
    }

    @Override
    public List<User> getAllWithoutCurrentUser(Long userId) {
        return userDao.getAll("id").stream()
                .filter(u -> !u.getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean areFriends(Long userId, User friend) {
        Set<User> friends = userDao.getById(userId).getFriends();
        return friends.contains(friend);
    }

    @Override
    public Map<User, List<FriendsGroup>> getFriendsGroupsMap(Long ownerId) {
        Set<User> friends = userDao.getById(ownerId).getFriends();
        Map<User, List<FriendsGroup>> friendsGroupsMap = new HashMap<>();
        friends.forEach(friend -> friendsGroupsMap.put(friend, getGroupsByOwnerAndFriend(ownerId, friend)));
        return friendsGroupsMap;
    }
}