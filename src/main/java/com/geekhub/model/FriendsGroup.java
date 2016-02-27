package com.geekhub.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
public class FriendsGroup {
    @Id
    @GeneratedValue
    @Column
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "userToGroupRelation",
            joinColumns = {
                    @JoinColumn(name = "groupId")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "userId")
            }
    )
    private Set<User> friendsSet;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getFriendsSet() {
        return friendsSet;
    }

    public void setFriendsSet(Set<User> friendsSet) {
        this.friendsSet = friendsSet;
    }

    @Override
    public String toString() {
        return "FriendsGroup{" +
                "id=" + id +
                '}';
    }
}
