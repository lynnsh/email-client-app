package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;

/**
 * The controller that is responsible for displaying the stage for
 * renaming or adding a new folder.
 * 
 * Note: since it is a child window, when an error occurs (e.g. SQLException),
 * the window is closed to display the main app, and the error is logged.
 *
 * @author Alena Shulzhenko
 * @version 09/11/2016
 * @since 1.8
 */
public class ModifyDirController implements Initializable {
    @FXML
    private Label dirError;
    @FXML
    private Button cancel;   
    @FXML
    private TextField input;
    
    private ResourceBundle bundle;
    private FolderStorageDAO folderdao;
    private TreeItem<String> currentDir;
    private ObservableList<String> dirs;
    private TreeItem<String> parent;
    
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());

    /**
     * Instantiates the object.
     */
    public ModifyDirController() {}
    
    /**
     * Called to initialize a controller after its root element 
     * has been completely processed.
     * 
     * @param url The location used to resolve relative paths for 
     *            the root object, or null if the location is not known.
     * @param rb The resources used to localize the root object, 
     *            or null if the root object was not localized.
     */
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        this.bundle = rb;   
        dirError = new Label();
        dirError.setVisible(true);
        log.debug(dirError.getText());
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
     * Renames or creates a new directory if the user input was valid.
     * The stage is hidden afterwards.
     * Both TreeView and ObservableList are updated.
     * The changes are saved in the database.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void onSubmit(ActionEvent event) {
        String newDir = input.getText();
        boolean valid = validateDir(newDir);
        if(valid) {
            dirError.setVisible(false);
            newDir = newDir.trim();
            try {
                //rename directory
                if(currentDir != null) {
                    List<TreeItem<String>> list = parent.getChildren();
                    list.set(list.indexOf(currentDir), new TreeItem<>(newDir));
                    dirs.set(dirs.indexOf(currentDir.getValue()), newDir);
                    folderdao.updateDirectory(currentDir.getValue(), newDir);
                }
                //create new directory
                else {
                    parent.getChildren().add(new TreeItem<>(newDir));
                    dirs.add(newDir);
                    folderdao.createDirectory(newDir);
                }
            }
            catch (SQLException ex) {
                log.error("Unable to modify directory table: ", ex.getMessage());                
            }
            finally {
                ((Node)(event.getSource())).getScene().getWindow().hide();
            }
        }
    }
    
    /**
     * Validates the directory name provided by the user.
     * It is invalid if it is null, an empty string, or it is a duplicate.
     * 
     * @param dir the directory name provided by the user.
     * 
     * @return true if name was valid; false otherwise.
     */
    private boolean validateDir(String dir) {
        log.debug("in validate");
        if(dir == null || dir.trim().isEmpty()) {           
            dirError.setText(bundle.getString("noValueErr"));
            dirError.setVisible(true);
            return false;
        }
        List<TreeItem<String>> list = parent.getChildren();
        if(inList(list, dir)) {           
            dirError.setText(bundle.getString("duplicateDirErr"));
            dirError.setVisible(true);
            return false;
        }
        return true;    
    }
    
    /**
     * Verifies if the specific value is in the list.
     * 
     * @param list the list to search for the value.
     * @param value the value to find in the list.
     * 
     * @return true if the value was found in the list; false otherwise. 
     */
    private boolean inList(List<TreeItem<String>> list, String value) {
        for(TreeItem<String> item : list)
            if(value.equals(item.getValue()))
                return true;
        return false;
    }

    /**
     * Sets the current directory name. It is null if the new directory
     * should be created. The input field shows the old directory name.
     * 
     * @param directory the old directory name.
     */
    public void setDirectory(TreeItem<String> directory) {
        this.currentDir = directory;
        if(directory != null) {
            input.setText(currentDir.getValue());
        }
        else
            input.setText("");
    }

    /**
     * Sets directory data access object in order to
     * work with the database.
     * 
     * @param folderdao DAO object to work with the database.
     */
    public void setFolderDAO(FolderStorageDAO folderdao) {
        this.folderdao = folderdao;
    }

    /**
     * Sets the ObservableList of directories containing the names of all
     * current directories.
     * 
     * @param dirs the ObservableList all current directories.
     */
    public void setDirList(ObservableList<String> dirs) {
        this.dirs = dirs;
    }

    /**
     * Sets the parent object for the TreeView, which will contain
     * the new directory, or which contains the directory to be renamed.
     * 
     * @param parent the parent object for the TreeView.
     */
    public void setTreeParent(TreeItem<String> parent) {
        this.parent = parent;
    }
}
