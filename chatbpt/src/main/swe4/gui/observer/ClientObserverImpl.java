package swe4.gui.observer;

import swe4.gui.model.Chatroom;
import swe4.gui.model.Message;
import swe4.gui.model.User;
import swe4.gui.view.ClientView;

import java.rmi.RemoteException;

public class ClientObserverImpl implements ClientObserver {

    transient private ClientView view;

    @Override
    public void updateMessagesOnNewMessage(Chatroom chatroom, Message message) {
        view.updateMessagesOnNewMessage(chatroom, message);
    }

    @Override
    public void updateChatRoomsOnJoin(Chatroom chatroom, User user) throws RemoteException {
        view.updateChatRoomsOnJoin(chatroom, user);
    }

    @Override
    public void updateChatroomsOnBan(Chatroom chatroom, User user) throws RemoteException {
        view.updateChatroomsOnBan(chatroom, user);
    }

    @Override
    public void updateChatroomsOnUnban(Chatroom chatroom, User user) throws RemoteException {
        view.updateChatroomsOnUnban(chatroom, user);
    }

    @Override
    public void updateChatroomsOnLeave(Chatroom chatroom, User user) throws RemoteException {
        view.updateChatroomsOnLeave(chatroom, user);
    }

    @Override
    public void updateChatroomsOnRemove(Chatroom chatroom) throws RemoteException {
        view.updateChatroomsOnRemove(chatroom);
    }

    @Override
    public void updateChatListAndMessageArea(String userName, String chatroomName) throws RemoteException {
        view.updateChatListAndMessageArea(userName, chatroomName);
    }

    public void setView(ClientView view) {
        this.view = view;
    }
}
