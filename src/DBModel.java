import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;


public class DBModel {

    private static DBModel instance;

    public static DBModel GetInstance() {
        if (instance == null) {
            instance = new DBModel();
            return instance;
        } else {
            return instance;
        }
    }

//wurst

    private Connection connection = null;
    private String salt = "nosalt";

    DBModel() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:passwort.db");
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
        }
    }



    public int checkLogin(String username, String unencryptedPassword) {
        String sql = "SELECT `id` FROM `users` WHERE `username`=? AND `master_password`=?";
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, Password.EncryptSHA256(unencryptedPassword));
            ResultSet rs = statement.executeQuery();

            int userId = 0;
            while (rs.next()) {
                userId = rs.getInt("id");
            }
            rs.close();
            statement.close();
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
            return 0;
        }

    }

    public void storeMasterPassword(int userId, String unencryptedPassword) {
        String sql = "UPDATE `users` SET `master_password`=? WHERE `id`=?";
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setString(1, Password.EncryptSHA256(unencryptedPassword));
            statement.setInt(2, userId);
            statement.executeUpdate();
            statement.close();
            PasswortApp.ShowMessage("Masterpasswort geändert in \"" + unencryptedPassword + "\".");
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
        }
    }

    public boolean addPassword(Password passObj, String unencryptedPassword) {
        String sql = "INSERT INTO `passwords` (`user_id`, `password`, `url`) VALUES (?,?,?)";
        //"???" Avoiding SQL Injection
        try {
            Crypto cipher = new Crypto(unencryptedPassword, salt);
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setInt(1, passObj.userId);
            System.out.println(cipher.encrypt(passObj.password));
            statement.setString(2, cipher.encrypt(passObj.password));
            statement.setString(3, passObj.url);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
            return false;
        }
        return true;
    }

    public void removePassword(int passwordId) {
        String sql = "DELETE FROM `passwords` WHERE `id`=?";
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setInt(1, passwordId);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
        }
    }

    public void updatePassword(Password passObj, String unencryptedPassword) {
        Crypto cipher = new Crypto(unencryptedPassword, salt);
        // Alles, was wir brauchen, befindet sich im selben Password objekt
        String sql = "UPDATE `passwords` SET `password`=?, `url`=? WHERE `id`=?";
        try {
            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setString(1, cipher.encrypt(passObj.password));
            statement.setString(2, passObj.url);
            statement.setInt(3, passObj.id);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
        }
    }

    public ArrayList<Password> getPasswords(int userId, String unencryptedPassword) {
        ArrayList<Password> passwords = new ArrayList<Password>();
        String sql = "SELECT * FROM `passwords` WHERE `user_id`=?";
        try {

            Crypto cipher = new Crypto(unencryptedPassword, salt);

            PreparedStatement statement = this.connection.prepareStatement(sql);
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Password passObj = new Password();
                passObj.id       = rs.getInt("id");
                passObj.userId   = rs.getInt("user_id");
                System.out.println(cipher.decrypt(passObj.password));
                passObj.password = new Crypto(unencryptedPassword, salt).decrypt(rs.getString("password"));
                passObj.url      = rs.getString("url");
                passwords.add(passObj);
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            PasswortApp.ShowError(e.getMessage());
        }
        return passwords;
    }

}
