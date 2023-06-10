package swe4.gui.observer;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientObserver extends Remote {
    void updateMessagesOnNewMessage(Chatroom chatroom, Message message) throws RemoteException;
    void updateChatRoomsOnJoin(Chatroom chatroom, User user) throws RemoteException;
    void updateChatroomsOnBan(Chatroom chatroom, User user) throws RemoteException;
    void updateChatroomsOnUnban(Chatroom chatroom, User user) throws RemoteException;
    void updateChatroomsOnLeave(Chatroom chatroom, User user) throws RemoteException;
    void updateChatroomsOnRemove(Chatroom chatroom) throws RemoteException;
    void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException;
}
