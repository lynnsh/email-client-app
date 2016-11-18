package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.properties.PropertiesManager;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.layout.BorderPane;
import static javafx.application.Application.launch;
import javafx.scene.image.Image;

/**
 * The class responsible for starting the Email Application.
 *
 * @author Alena Shulzhenko
 * @version 12/11/2016
 * @since 1.8
 */
public class MainApp extends Application {   
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    private PropertiesManager pm;
    private ResourceBundle bundle;
    private static final String PROPERTIES_PATH = "src/main/resources/properties";
    
    /**
     * Instantiates the object.
     */
    public MainApp() {
        bundle = ResourceBundle.getBundle("resources/LanguageBundle");
    }
    
    /**
     * The main entry point for JavaFX application.
     * 
     * @param stage the primary stage for this application, 
     *               onto which the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        try {           
            pm = new PropertiesManager();
            UserConfigBean user = pm.loadTextProperties(PROPERTIES_PATH, "data");
            stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/email.png")));
            //if there is at least one value missing, 
            //the config form should be displayed
            if(isEmptyBean(user))             
                displayForm(stage, user, false);
            else               
                displayEmailApp(stage, user);          
        }
        catch(Exception e) {
            log.error("Error displaying layout", e);
            System.exit(1);
        }
    }

    /**
     * Starts the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    
    /**
     * Displays the Email Application stage with 
     * all necessary GUI components loaded.
     * 
     * @param stage the stage for this module.
     * @param user the user information.
     */
    public void displayEmailApp(Stage stage, UserConfigBean user) {
        try {
            stage.setTitle(bundle.getString("emailTitle"));           
            FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/fxml/EmailApp.fxml"), bundle);
            BorderPane root = (BorderPane)loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Ubuntu");
            EmailAppController controller = loader.getController();
            controller.setBundle(bundle);
            controller.setMainApp(this);
            controller.setUserInfo(user);
            stage.show();
        } catch (Exception ex) {
            log.error("Error in Email App", ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Displays the Configuration Form stage with 
     * all necessary GUI components loaded.
     * 
     * @param stage the stage for this module.
     * @param user the user information.
     * @param isChild indicates if this stage is the child of EmailApp stage.
     */
    public void displayForm(Stage stage, UserConfigBean user, boolean isChild) {
        try {
            stage.setTitle(bundle.getString("configTitle"));
            FXMLLoader loader = new FXMLLoader(getClass()
                                .getResource("/fxml/ConfigForm.fxml"), bundle);
            GridPane root = (GridPane)loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Ubuntu");
            ConfigFormController controller = loader.getController();
            controller.setMainApp(this);
            controller.setUserInfo(user);
            controller.setProperties(pm, PROPERTIES_PATH);
            controller.setIsChild(isChild);
            stage.show();
        } 
        catch (Exception ex) {
            log.error("Error in Config Form", ex.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Verifies the UserConfigBean if some values are empty.
     * 
     * @param user the UserConfigBean to verify.
     * 
     * @return true if there are empty fields present or port number is invalid;
     *         false otherwise.
     */
    private boolean isEmptyBean(UserConfigBean user) {   
        int[] ports = new int[]{user.getImapPort(), user.getMysqlPort(), user.getSmtpPort()};
        for(int port: ports)
            if(port < 0 || port > 65536)
                return true;
        return user.getSmtpUrl().trim().isEmpty() || 
               user.getEmailPassword().trim().isEmpty() ||
               user.getFromEmail().trim().isEmpty() ||
               user.getImapUrl().trim().isEmpty() ||
               user.getMysqlDbName().trim().isEmpty() ||
               user.getMysqlPassword().trim().isEmpty() ||
               user.getMysqlUrl().trim().isEmpty() ||
               user.getMysqlUserName().trim().isEmpty();
    }


}
