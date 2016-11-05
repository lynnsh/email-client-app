package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.properties.PropertiesManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import jodd.mail.EmailAddress;
import org.slf4j.LoggerFactory;

public class ConfigFormController implements Initializable {
    @FXML
    private PasswordField emailPassword;
    @FXML
    private TextField fromEmail;
    @FXML
    private TextField imapPort;
    @FXML
    private TextField imapUrl;
    @FXML
    private TextField mysqlDbName;
    @FXML
    private PasswordField mysqlPassword;
    @FXML
    private TextField mysqlPort;
    @FXML
    private TextField mysqlUrl;
    @FXML
    private TextField mysqlUser;
    @FXML
    private TextField smtpPort;
    @FXML
    private TextField smtpUrl;
    @FXML
    private Button cancel;
    
    private MainApp mainApp;
    private UserConfigBean user;
    private PropertiesManager pm;
    private boolean second;
    private static String PROPERTIES_PATH;
    private ResourceBundle bundle;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public ConfigFormController() {}
    
    public void setUserInfo(UserConfigBean user) {
        this.user = user;
        Bindings.bindBidirectional(emailPassword.textProperty(), user.emailPassword());
        Bindings.bindBidirectional(fromEmail.textProperty(), user.fromEmail());
        Bindings.bindBidirectional(imapPort.textProperty(), user.imapPort());
        Bindings.bindBidirectional(imapUrl.textProperty(), user.imapUrl());
        Bindings.bindBidirectional(mysqlDbName.textProperty(), user.mysqlDbName());
        Bindings.bindBidirectional(mysqlPassword.textProperty(), user.mysqlPassword());
        Bindings.bindBidirectional(mysqlPort.textProperty(), user.mysqlPort());
        Bindings.bindBidirectional(mysqlUrl.textProperty(), user.mysqlUrl());
        Bindings.bindBidirectional(mysqlUser.textProperty(), user.mysqlUser());
        Bindings.bindBidirectional(smtpPort.textProperty(), user.smtpPort());
        Bindings.bindBidirectional(smtpUrl.textProperty(), user.smtpUrl());
    }
    
    public void setIsSecondWindow(boolean second) {
        this.second = second;
    }
    
    @FXML
    private void onSubmit(ActionEvent event) {      
        TextField[] array = {emailPassword, fromEmail, imapUrl, mysqlDbName, 
                             mysqlPassword, mysqlUrl, mysqlUser, smtpUrl};
        boolean allValid = validate(array);
        
        if(allValid) {
            try {
                pm.writeTextProperties(PROPERTIES_PATH, "data", user);
                if(! second) {
                    Stage stage = new Stage();
                    mainApp.displayEmailApp(stage, user);
                }
                ((Node)(event.getSource())).getScene().getWindow().hide();
                System.out.println("all correct!");
            } 
            catch (IOException ex) {
                log.error("Error saving properties file: ", ex.getMessage());
                Platform.exit();
            }
        }
    }
    
    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
    }

/**
     * Is called by the main application to give a reference back to itself.
     * 
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }   
    
    public void setProperties(PropertiesManager pm, String path) {
        this.pm = pm;
        PROPERTIES_PATH = path;
    }
    
    private void displayError(String message) {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("" + bundle.getString("alertTitle") + "!");
        //dialog.setHeaderText(message);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    private boolean validate(TextField[] array) {
        for(TextField tf : array) {
            if(tf.getText().isEmpty()) {
                displayError("" + bundle.getString("noValueErr") + " " + tf.getId());
                return false;
            }
        }
        final String digits = "^[1-9][0-9]*$";
        TextField[] nums = {mysqlPort, imapPort, smtpPort};
        for(TextField num : nums) {
            if(!Pattern.matches(digits, num.getText())) {
                displayError("" + bundle.getString("invalidPortErr") + " "+
                        num.getText()+" " + bundle.getString("for") + " " + num.getId());
                return false;
            }
            int port = Integer.parseInt(num.getText());
            if(port < 0 || port > 65536) {
                displayError("" + bundle.getString("invalidPortErr") + " "+
                        num.getText()+" " + bundle.getString("for") + " " + num.getId());
                return false;
            }
        }
        if (!(new EmailAddress(fromEmail.getText())).isValid()) {
            displayError("" + bundle.getString("invalidEmailErr") + " " + fromEmail.getText());
            return false;
        }
        return true; 
    }

    
}
