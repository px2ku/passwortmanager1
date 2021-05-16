import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class LoginController implements Initializable {

    @FXML
    private Button submitButton;

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    /**
     * takes the username and password, if they are correct, the user gets
     * redirected to the login page
     */
    @FXML
    public void send() {
        String enteredUsername = username.getText().toString();
        String enteredPassword = password.getText().toString();

        int userId = DBModel.GetInstance().checkLogin(enteredUsername, enteredPassword);
        if (userId != 0) {
            this.gotoManagerPanel(userId);
        } else { // incorrect login
            PasswortApp.ShowError("Falscher benutzername oder falsches passwort!");
        }

    }   

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Initialization code can go here.
        // The parameters url and resources can be omitted if they are not needed
    }



    private void gotoManagerPanel(int userId) {
        try {

            Stage stage = (Stage)this.submitButton.getScene().getWindow();
            // stage.close();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(PasswortApp.class.getResource("manager-panel.fxml"));


            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            ManagerController controller = loader.getController();
            controller.setUserId(userId);
            ArrayList<Password> passwords = DBModel.GetInstance().getPasswords(userId, password.getText());
            controller.addPasswords(passwords);
            // Passwörter anzeigen in der tabelle

        } catch (Exception e) {
            e.printStackTrace();
            PasswortApp.ShowError("Manager-panel kann nicht geladen werden: " + e.getMessage());
        }
    }



}
