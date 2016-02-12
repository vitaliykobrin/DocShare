package com.geekhub.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
public class User extends MappedEntity {
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String password;
    @Column(unique = true)
    private String login;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", targetEntity = Message.class)
    private Set<Message> messageSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", targetEntity = FriendsGroup.class)
    private Set<FriendsGroup> friendsGroupSet;
    @ManyToMany
    private Set<FriendsGroup> consistGroupSet;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Set<Message> getMessageSet() {
        return messageSet;
    }

    public void setMessageSet(Set<Message> messageSet) {
        this.messageSet = messageSet;
    }

    public Set<FriendsGroup> getFriendsGroupSet() {
        return friendsGroupSet;
    }

    public void setFriendsGroupSet(Set<FriendsGroup> friendsGroupSet) {
        this.friendsGroupSet = friendsGroupSet;
    }

    public Set<FriendsGroup> getConsistGroupSet() {
        return consistGroupSet;
    }

    public void setConsistGroupSet(Set<FriendsGroup> consistGroupSet) {
        this.consistGroupSet = consistGroupSet;
    }
}
