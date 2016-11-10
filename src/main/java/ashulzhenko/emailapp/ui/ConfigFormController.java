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

/**
 * The controller that is responsible for displaying the stage for
 * the configuration module. 
 * It allows the user to enter or modify
 * the configuration information, such as an email address, password,
 * email and database information.
 *
 * @author Alena Shulzhenko
 * @version 09/11/2016
 * @since 1.8
 */
public class ConfigFormController implements Initializable {
    
    @FXML
    private PasswordField emailPassword, mysqlPassword;
    @FXML
    private TextField fromEmail, imapPort, imapUrl, smtpPort, smtpUrl,
                      mysqlDbName, mysqlPort, mysqlUrl, mysqlUser;
    @FXML
    private Button cancel;
    
    private MainApp mainApp;
    private UserConfigBean user;
    private PropertiesManager pm;
    //if this window is a child of another controller
    private boolean isChild;
    //the path to properties file
    private static String PROPERTIES_PATH;
    private ResourceBundle bundle;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
   /**
    * Instantiates the object.
    */
    public ConfigFormController() {}
    
    /**
     * Called to initialize a controller after its root element 
     * has been completely processed.
     * 
     * @param url The location used to resolve relative paths for 
     *            the root object, or null if the location is not known.
     * @param rb  The resources used to localize the root object, 
     *            or null if the root object was not localized.
     */
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
    }
    
    /**
     * Sets the user information and binds it to JavaFX components.
     * 
     * @param user the user information.
     */
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
    
    /**
     * Sets whether this controller is a child of another controller.
     * That is, whether the user sees it the first time to enter data,
     * or it was called from the email app to modify the data.
     * 
     * @param isChild indicates if this controller is a child of another controller.
     */
    public void setIsChild(boolean isChild) {
        this.isChild = isChild;
    }
    
    /**
     * Submits and saves the user input from the form if it was valid.
     * The stage is hidden afterwards and the main app is displayed.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void onSubmit(ActionEvent event) {      
        TextField[] array = {emailPassword, fromEmail, imapUrl, mysqlDbName, 
                             mysqlPassword, mysqlUrl, mysqlUser, smtpUrl};
        boolean allValid = validate(array);       
        if(allValid) {
            try {
                pm.writeTextProperties(PROPERTIES_PATH, "data", user);
                //if not the child, main app window is displayed
                if(!isChild) {
                    Stage stage = new Stage();
                    mainApp.displayEmailApp(stage, user);
                }
                ((Node)(event.getSource())).getScene().getWindow().hide();
            } 
            catch (IOException ex) {
                log.error("Error saving properties file: ", ex.getMessage());
                Platform.exit();
            }
        }
    }
    
    /**
     * Closes the stage if the user presses cancel button.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }  

    /**
     * Called by the main application to give a reference back to itself.
     * 
     * @param mainApp the reference to the main app.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }   
    
    /**
     * Displays error alert dialog if user input was invalid.
     * 
     * @param message the error message to display in the alert dialog.
     */
    private void displayError(String message) {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle(bundle.getString("alertTitle") + "!");
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    /**
     * Validates the user input.
     * @param array the array of TextFields with user input.
     * 
     * @return true if data is valid; false otherwise.
     */
    private boolean validate(TextField[] array) {
        //check that values are not empty
        for(TextField tf : array) {
            if(tf.getText().trim().isEmpty()) {
                displayError(bundle.getString("noValueErr"));
                return false;
            }
        }
        
        //check ports
        if(! checkPorts(new TextField[]{mysqlPort, imapPort, smtpPort}))
            return false;      
        
        //check valid email address
        if (!(new EmailAddress(fromEmail.getText())).isValid()) {
            displayError(bundle.getString("invalidEmailErr") + " " + fromEmail.getText());
            return false;
        }
        return true; 
    }
    
    /**
     * Verifies the port value. 
     * It should be an integer and from 0 to 65536.
     * 
     * @param ports the port values to verify.
     * 
     * @return true if ports values are valid, false otherwise.
     */
    private boolean checkPorts(TextField[] ports) {
        String digits = "^[1-9][0-9]*$";
        for(TextField num : ports) {
            String errMessage = bundle.getString("invalidPortErr") + " " +
                                num.getText()+ " " + bundle.getString("for") + 
                                " " + num.getId();
            if(!Pattern.matches(digits, num.getText())) {
                displayError(errMessage);
                return false;
            }
            int port = Integer.parseInt(num.getText());
            if(port < 0 || port > 65536) {
                displayError(errMessage);
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the PropertiesManager as well as the path to the properties file.
     * @param pm PropertiesManager object.
     * @param path the path to the properties file.
     */
    public void setProperties(PropertiesManager pm, String path) {
        this.pm = pm;
        PROPERTIES_PATH = path;
    }
    
}
