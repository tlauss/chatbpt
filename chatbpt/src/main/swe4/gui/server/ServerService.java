package swe4.gui.server;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;
import swe4.gui.observer.ClientObserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerService extends Remote {
    void addUser(String username, String shortname, String password) throws RemoteException;
    User getUser(String username) throws RemoteException;
    boolean userExists(String user) throws RemoteException;
    boolean passwordCorrect(String user, String password) throws RemoteException;
    List<User> getUsers() throws RemoteException;
    void addChatroom(Chatroom chatroom) throws RemoteException;
    void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException;
    boolean chatroomExists(String roomName) throws RemoteException;
    void addMessage(String selectedChat, Message message) throws RemoteException;
    Chatroom getChatroom(String roomName) throws RemoteException;
    List<Chatroom> getChatrooms() throws RemoteException;
    List<User> getChatRoomUsers(String roomName) throws RemoteException;
    List<Message> getMessages(String chatroom) throws RemoteException;
    void addUserToChatroom(User user, String roomName) throws RemoteException;
    void removeChatRoom(Chatroom chatroom) throws RemoteException;
    void removeUserFromChatroom(User loggedInUser, String selectedChat) throws RemoteException;
    void banUserFromChatroom(User user, String roomName) throws RemoteException;
    void unbanUserFromChatroom(User user, String roomName) throws RemoteException;
    void registerClient(ClientObserver client, User user) throws RemoteException;
    void unregisterClient(ClientObserver client, User user) throws RemoteException;
    List<User> getLoggedInUsers() throws RemoteException;
}
