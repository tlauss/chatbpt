package swe4.gui.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView extends Application {
    private TextField usernameInputField;
    private PasswordField passwordInputField;
    private Button loginButton;
    private Button registerButton;
    private Label messageLabel;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);

        Label headingLabel = new Label("ChatBPT");
        headingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox.setMargin(headingLabel, new Insets(0, 0, 10, 0));

        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        usernameInputField = new TextField();
        usernameInputField.setPromptText("Username");
        passwordInputField = new PasswordField();
        passwordInputField.setPromptText("Password");
        loginButton = new Button("Login");
        registerButton = new Button("Go to Register");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        VBox usernameBox = new VBox();
        usernameBox.setSpacing(5);
        usernameBox.getChildren().addAll(usernameLabel, usernameInputField);

        VBox passwordBox = new VBox();
        passwordBox.setSpacing(5);
        passwordBox.getChildren().addAll(passwordLabel, passwordInputField);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(registerButton, spacer, loginButton);
        root.getChildren().addAll(headingLabel, usernameBox, passwordBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    public void end() {
        primaryStage.close();
    }

    public void handleLoginButtonClicked(Runnable action) {
        loginButton.setOnAction(e -> action.run());
    }

    public void handleRegisterButtonClicked(Runnable action) {
        registerButton.setOnAction(e -> action.run());
    }

    public String getUsername() {
        return usernameInputField.getText();
    }

    public String getPassword() {
        return passwordInputField.getText();
    }

    public void showErrorMessage(String message) {
        messageLabel.setText(message);
    }
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}

