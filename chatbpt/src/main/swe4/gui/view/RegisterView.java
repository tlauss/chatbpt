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

public class RegisterView extends Application {
    private TextField usernameInputField;
    private TextField shortNameInputField;
    private PasswordField passwordInputField;
    private PasswordField repeatPasswordInputField;
    private Button loginLinkButton;
    private Button registerButton;
    private Label messageLabel;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Register");
        primaryStage.setResizable(false);

        Label headingLabel = new Label("ChatBPT");
        headingLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox.setMargin(headingLabel, new Insets(0, 0, 10, 0));

        Label usernameLabel = new Label("Username:");
        Label shortNameLabel = new Label("Short Name:");
        Label passwordLabel = new Label("Password:");
        Label passwordRepeatLabel = new Label("Repeat Password:");
        usernameInputField = new TextField();
        usernameInputField.setPromptText("Username");
        shortNameInputField = new TextField();
        shortNameInputField.setPromptText("Short Name");
        passwordInputField = new PasswordField();
        passwordInputField.setPromptText("Password");
        repeatPasswordInputField = new PasswordField();
        repeatPasswordInputField.setPromptText("Repeat Password");

        loginLinkButton = new Button("Back to Login");
        registerButton = new Button("Register");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        VBox usernameBox = new VBox();
        usernameBox.setSpacing(5);
        usernameBox.getChildren().addAll(usernameLabel, usernameInputField);

        VBox shortNameBox = new VBox();
        shortNameBox.setSpacing(5);
        shortNameBox.getChildren().addAll(shortNameLabel, shortNameInputField);

        VBox passwordBox = new VBox();
        passwordBox.setSpacing(5);
        passwordBox.getChildren().addAll(passwordLabel, passwordInputField);

        VBox repeatPasswordBox = new VBox();
        repeatPasswordBox.setSpacing(5);
        repeatPasswordBox.getChildren().addAll(passwordRepeatLabel, repeatPasswordInputField);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(loginLinkButton, spacer, registerButton);
        root.getChildren().addAll(headingLabel, usernameBox, shortNameBox, passwordBox, repeatPasswordBox, buttonBox, messageLabel);

        Scene scene = new Scene(root, 300, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void end() {
        primaryStage.close();
    }

    public void handleRegisterButtonClicked(Runnable action) {
        registerButton.setOnAction(e -> action.run());
    }

    public void handleBackToLoginButtonClicked(Runnable action) {
        loginLinkButton.setOnAction(e -> action.run());
    }

    public String getUsername() {
        return usernameInputField.getText();
    }

    public String getShortName() {
        return shortNameInputField.getText();
    }

    public String getPassword() {
        return passwordInputField.getText();
    }

    public String getRepeatPassword() {
        return repeatPasswordInputField.getText();
    }
    public void showErrorMessage(String message) {
        messageLabel.setText(message);
    }
}
