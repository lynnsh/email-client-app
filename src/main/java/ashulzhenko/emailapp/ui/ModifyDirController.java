package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
 *
 * @author aline
 */
public class ModifyDirController implements Initializable {
    @FXML
    private Label dirError = new Label();

    @FXML
    private Button cancel;
    
    @FXML
    private TextField input;
    
    private ResourceBundle bundle;
    private FolderStorageDAO folderdao;
    private TreeItem<String> current;
    private ObservableList<String> dirs;
    private TreeItem<String> parent;
    
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());

    public ModifyDirController() {}
    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        this.bundle = rb;   
    }  
    
    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onSubmit(ActionEvent event) {
        String newDir = input.getText();
        boolean valid = validateDir(newDir);
        if(valid) {
            dirError.setVisible(false);
            newDir = newDir.trim();
            try {
                if(current != null) {
                    List<TreeItem<String>> list = parent.getChildren();
                    list.set(list.indexOf(current), new TreeItem<>(newDir));
                    dirs.set(dirs.indexOf(current.getValue()), newDir);
                    folderdao.updateDirectory(current.getValue(), newDir);
                }
                else {
                    parent.getChildren().add(new TreeItem<>(newDir));
                    dirs.add(newDir);
                    folderdao.createDirectory(newDir);
                }

                ((Node)(event.getSource())).getScene().getWindow().hide();
            }
            catch (SQLException ex) {
                log.error("Unable to modify directory table: ", ex.getMessage());
                Platform.exit();
            }
        }
    }
    
    private boolean validateDir(String dir) {
        if(dir == null || dir.trim().isEmpty()) {
            dirError.setVisible(true);
            dirError.setText(bundle.getString("noValueErr") + " " + bundle.getString("dir"));
            return false;
        }
        List<TreeItem<String>> list = parent.getChildren();
        if(inList(list, dir)) {
            dirError.setVisible(true);
            dirError.setText(bundle.getString("duplicateDirErr"));
            return false;
        }
        return true;    
    }
    
    private boolean inList(List<TreeItem<String>> list, String value) {
        for(TreeItem<String> item : list)
            if(value.equals(item.getValue()))
                return true;
        return false;
    }

    public void setDirectory(TreeItem<String> directory) {
        this.current = directory;
        if(directory != null) {
            input.setText(current.getValue());
        }
        else
            input.setText("");
    }

    public void setFolderDAO(FolderStorageDAO folderdao) {
        this.folderdao = folderdao;
    }

    public void setDirList(ObservableList<String> dirs) {
        this.dirs = dirs;
    }

    public void setTreeParent(TreeItem<String> parent) {
        this.parent = parent;
    }
}
