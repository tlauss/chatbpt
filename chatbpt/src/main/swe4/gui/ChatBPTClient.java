package swe4.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import swe4.gui.controller.ProgramController;
import swe4.gui.observer.ClientObserverImpl;
import swe4.gui.server.ServerService;
import swe4.gui.view.ClientView;
import swe4.gui.view.LoginView;
import swe4.gui.view.RegisterView;

import java.rmi.server.UnicastRemoteObject;

public class ChatBPTClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        LoginView loginView = new LoginView();
        RegisterView registerView = new RegisterView();
        ClientObserverImpl clientObserver = new ClientObserverImpl();
        ClientView clientView = new ClientView();
        clientObserver.setView(clientView);
        ServerService server = null;

        String hostAndPort = "localhost";
        String serviceUrl = "rmi://" + hostAndPort + "/ChatServer";

        System.out.println("Connecting to server at " + serviceUrl);

        try {
            server = (ServerService) java.rmi.Naming.lookup(serviceUrl);
        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }

        UnicastRemoteObject.exportObject(clientObserver, 0);

        ProgramController programController = new ProgramController(loginView, registerView, clientView,
                server, clientObserver);

        programController.run();
    }
     
    public static void main(String[] args) {
        launch(args);
    }
}
