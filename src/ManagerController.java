import java.net.URL;
import java.util.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class ManagerController implements Initializable {

    private static int userId = -1; // @TODO: pass through at login


    @FXML
    private TextField password;

    @FXML
    private TextArea website;

    @FXML
    private TextField masterPassword;


    /**
     * passwort tabelle
     */
    @FXML
    private TableView<Password> table;

    @FXML
    private TableColumn<Password, String> passwordColumn;

    @FXML
    private TableColumn<Password, String> websiteColumn;

    private List<Password> passwords = new ArrayList();

    public void addPasswords(ArrayList<Password> passwords){
        this.passwords.addAll(passwords);
        this.table.getItems().addAll(passwords);
    }

    @FXML
    public void addItem() {
        Password passObj = new Password();
        passObj.id = table.getItems().size() + 1;
        passObj.userId   = this.userId;
        passObj.password = password.getText().toString();
        passObj.url      = website.getText().toString();
        if (DBModel.GetInstance().addPassword(passObj, masterPassword.getText())) {
            this.table.getItems().add(passObj);
        }
    }

    @FXML
    public void removeItem() {

        String text = "Möchten Sie dieses passwort wirklich löschen?";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            Password selectedPassObj = this.table.getSelectionModel().getSelectedItem();
            DBModel.GetInstance().removePassword(selectedPassObj.id);
            this.table.getItems().remove(selectedPassObj);
        }

    }

    @FXML
    public void editItem() {

        Password selectedPassObj = this.table.getSelectionModel().getSelectedItem();
        selectedPassObj.password = this.password.getText();
        selectedPassObj.url      = this.website.getText();
        DBModel.GetInstance().updatePassword(selectedPassObj, masterPassword.getText());
        this.table.refresh();

    }

    @FXML
    public void setMasterPassword() {

        DBModel.GetInstance().storeMasterPassword(this.userId, masterPassword.getText().toString());

    }

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Auswahlfunktionen
        this.table.setEditable(false);
        this.table.getSelectionModel().selectedItemProperty().addListener((observable, oldItem, newItem) -> {
            // Informationen zur neuen row anzeigen
            if(newItem != null) {
                this.password.setText(newItem.password);
                this.website.setText(newItem.url);
            }else{
                this.password.setText("");
                this.website.setText("");
            }
        });
    }

    public void setUserId(int userId) { this.userId = userId; }

}
