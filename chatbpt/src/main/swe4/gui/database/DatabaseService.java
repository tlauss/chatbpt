package swe4.gui.database;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;

import java.util.ArrayList;

public interface DatabaseService {
    void addUser(String user, String password, String shortname);
    User getUser(String user);
    User getUser(int id);
    boolean userExists(String user);
    boolean passwordCorrect(String user, String password);
    ArrayList<Chatroom> getChatrooms();
    void addChatroom(Chatroom chatroom, String user);
    Chatroom getChatroom(String chatroom);
    ArrayList<User> getChatroomUsers(String chatroom);
    void removeChatroom(Chatroom chatroom);
    boolean chatroomExists(String chatroom);
    void addUserToChatroom(User user, String chatroom);
    void addMessage(String selectedChat, Message message);
    void removeUserFromChatroom(User loggedInUser, String chatroom);
    void banUserFromChatroom(User user, String chatroom);
    void unbanUserFromChatroom(User user, String chatroom);
    ArrayList<User> getBannedUsersFromChatroom(String chatroom);
    ArrayList<Message> getMessages(String chatroom);
}
