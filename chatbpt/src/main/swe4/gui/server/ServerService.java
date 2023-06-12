package swe4.gui.server;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;
import swe4.gui.observer.ClientObserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface ServerService extends Remote {
    void addUser(String username, String shortname, String password) throws RemoteException;
    User getUser(String username) throws RemoteException;
    boolean userExists(String user) throws RemoteException;
    boolean passwordCorrect(String user, String password) throws RemoteException;
    void addChatroom(Chatroom chatroom, String userName) throws RemoteException;
    void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException;
    boolean chatroomExists(String roomName) throws RemoteException;
    void addMessage(String selectedChat, Message message) throws RemoteException;
    Chatroom getChatroom(String roomName) throws RemoteException;
    ArrayList<Chatroom> getChatrooms() throws RemoteException;
    ArrayList<User> getChatroomUsers(String roomName) throws RemoteException;
    ArrayList<Message> getMessages(String chatroom) throws RemoteException;
    void addUserToChatroom(User user, String roomName) throws RemoteException;
    void removeChatroom(Chatroom chatroom) throws RemoteException;
    void banUserFromChatroom(User user, String roomName) throws RemoteException;
    void unbanUserFromChatroom(User user, String roomName) throws RemoteException;
    ArrayList<User> getBannedUsersFromChatroom(String roomName) throws RemoteException;
    void registerClient(ClientObserver client, User user) throws RemoteException;
    void unregisterClient(ClientObserver client, User user) throws RemoteException;
    ArrayList<User> getLoggedInUsers() throws RemoteException;
}
