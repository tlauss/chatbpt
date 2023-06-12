package swe4.gui.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class Chatroom implements Serializable {
    private final String name;
    private final ArrayList<User> users;
    private final ArrayList<User> bannedUsers;
    private final ArrayList<Message> messages;
    private final User owner;

    @Serial
    private static final long serialVersionUID = -7875509922081022453L;

    public Chatroom(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.users = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public Chatroom(String name, User owner, ArrayList<User> users, ArrayList<User> bannedUsers, ArrayList<Message> messages) {
        this.name = name;
        this.owner = owner;
        this.users = users;
        this.bannedUsers = bannedUsers;
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public User getOwner() {
        return owner;
    }

    @Override
    public String toString() {
            return "Chatroom: " +
                    "name='" + name + '\'' +
                    ", users=" + users +
                    ", bannedUsers=" + bannedUsers +
                    ", messages=" + messages +
                    ", owner=" + owner + '\n' +
                    '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Chatroom chatroom) {
            return chatroom.getName().equals(this.getName());
        }
        return false;
    }
}
