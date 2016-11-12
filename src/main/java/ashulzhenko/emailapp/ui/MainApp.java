package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.properties.PropertiesManager;
import java.util.Locale;
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


public class MainApp extends Application {   
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    private Stage stage;
    private PropertiesManager pm;
    private ResourceBundle bundle;
    private static final String PROPERTIES_PATH = "src/main/resources/properties";
    
    public MainApp() {
        bundle = ResourceBundle.getBundle("resources/LanguageBundle");//, Locale.CANADA_FRENCH);
    }
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        try {           
            pm = new PropertiesManager();
            UserConfigBean user = pm.loadTextProperties(PROPERTIES_PATH, "data");            
            if(user.getEmailPassword().isEmpty())             
                displayForm(stage, user, false);
            else               
                displayEmailApp(stage, user);          
        }
        catch(Exception e) {
            log.error("Error displaying layout", e);
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

    
    //catch exceptions
    public void displayEmailApp(Stage stage, UserConfigBean user) {
        try {
            stage.setTitle(bundle.getString("emailTitle"));           
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmailApp.fxml"), bundle);
            BorderPane root = (BorderPane)loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            
            EmailAppController controller = loader.getController();
            controller.setBundle(bundle);
            controller.setMainApp(this);
            controller.setUserInfo(user);
            controller.setProperties(pm, PROPERTIES_PATH);
            stage.show();
        } catch (Exception ex) {
            log.error("Error in Email App: ", ex.getMessage());
            System.exit(1);
        }
    }

    public void displayForm(Stage stage, UserConfigBean user, boolean second) {
        try {
            stage.setTitle(bundle.getString("configTitle"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConfigForm.fxml"), bundle);
            GridPane root = (GridPane)loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            
            ConfigFormController controller = loader.getController();
            controller.setMainApp(this);
            controller.setUserInfo(user);
            controller.setProperties(pm, PROPERTIES_PATH);
            controller.setIsChild(second);
            stage.show();
        } 
        catch (Exception ex) {
            log.error("Error in Config Form: ", ex.getMessage());
            System.exit(1);
        }
    }


}
