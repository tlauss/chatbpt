package swe4.gui.controller;

import javafx.stage.Stage;
import swe4.gui.model.User;
import swe4.gui.observer.ClientObserver;
import swe4.gui.server.ChatServer;
import swe4.gui.server.ServerService;
import swe4.gui.view.ClientView;
import swe4.gui.view.LoginView;
import swe4.gui.view.RegisterView;

import java.rmi.RemoteException;

public class ProgramController {
    private final LoginView loginView;
    private final RegisterView registerView;
    private final ClientView clientView;
    private final ClientObserver clientObserver;
    private final ServerService server;
    private User loggedInUser;

    public ProgramController(LoginView loginView, RegisterView registerView, ClientView clientView,
                             ServerService server, ClientObserver clientObserver) {
        this.loginView = loginView;
        this.registerView = registerView;
        this.clientView = clientView;
        this.server = server;
        this.clientObserver = clientObserver;
    }

    public void run() {
        if (registerView.getPrimaryStage() != null) {
            registerView.end();
        }
        loginView.start(new Stage());
        loginView.handleLoginButtonClicked(this::handleLogin);
        loginView.handleRegisterButtonClicked(this::openRegister);
    }

    public void openRegister() {
        try {
            registerView.start(new Stage());
            registerView.handleBackToLoginButtonClicked(this::run);
            registerView.handleRegisterButtonClicked(this::handleRegister);
        } catch (Exception e) {
            System.err.println("Error starting register view: " + e.getMessage());
        }
        loginView.end();
    }

    public void handleLogin() {
        try {
            if (loginView.getUsername().isEmpty() || loginView.getPassword().isEmpty()) {
                loginView.showErrorMessage("Please enter username and password");
            } else {
                if (server.userExists(loginView.getUsername())) {
                    if (server.passwordCorrect(loginView.getUsername(), loginView.getPassword())) {
                        if (server.getLoggedInUsers().contains(server.getUser(loginView.getUsername()))) {
                            loginView.showErrorMessage("User already logged in");
                            return;
                        }
                        loggedInUser = server.getUser(loginView.getUsername());

                        clientView.setProgramController(this);
                        server.registerClient(clientObserver, loggedInUser);
                        openClient();
                    } else {
                        loginView.showErrorMessage("Wrong password");
                    }
                } else {
                    loginView.showErrorMessage("User does not exist");
                }
            }
        } catch (RemoteException e) {
            System.err.println("Error logging in: " + e.getMessage());
        }
    }

    public void handleRegister() {
        try {
            if (registerView.getUsername().isEmpty() || registerView.getShortName().isEmpty() || registerView.getPassword().isEmpty()) {
                registerView.showErrorMessage("Please enter username, shortname and password");
            } else if (!registerView.getPassword().equals(registerView.getRepeatPassword())) {
                registerView.showErrorMessage("Passwords do not match");
            } else {
                if (server.userExists(registerView.getUsername())) {
                    registerView.showErrorMessage("User already exists");
                } else {
                    server.addUser(registerView.getUsername(), registerView.getPassword(), registerView.getShortName());
                    loggedInUser = server.getUser(registerView.getUsername());

                    clientView.setProgramController(this);
                    server.registerClient(clientObserver, loggedInUser);
                    openClient();
                }
            }
        } catch (Exception e) {
            System.err.println("Error registering: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void openClient() {
        try {
            clientView.start(new Stage());
        } catch (Exception e) {
            System.err.println("Error starting client view: " + e.getMessage());
            e.printStackTrace();
        }
        if (registerView.getPrimaryStage() != null) {
            registerView.end();
        } else if (loginView.getPrimaryStage() != null) {
            loginView.end();
        }
    }

    public void logout() {
        try {
            server.unregisterClient(clientObserver, loggedInUser);
        } catch (RemoteException e) {
            System.err.println("Error unregistering client: " + e.getMessage());
        }
        loggedInUser = null;
    }

    public ServerService getServer() {
        return server;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
