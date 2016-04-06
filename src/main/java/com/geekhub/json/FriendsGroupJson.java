package com.geekhub.json;

import com.geekhub.entity.User;

public class FriendsGroupJson {

    private long id;
    private String name;
    private User[] friends;

    public FriendsGroupJson(long id, String name, User[] friends) {
        this.id = id;
        this.name = name;
        this.friends = friends;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User[] getFriends() {
        return friends;
    }

    public void setFriends(User[] friends) {
        this.friends = friends;
    }
}
