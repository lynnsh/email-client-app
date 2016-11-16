package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import java.sql.SQLException;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.slf4j.LoggerFactory;

/**
 * The helper class that responsible to handle drag n drop events.
 * 
 * @author Alena Shulzhenko
 * @version 15/11/2016
 * @since 1.8
 */
public class DragNDropHelper {
    
    private MailStorageDAO maildao;
    private TreeItem<String> parent;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Initializes the object.
     * 
     * @param maildao emails DAO object to work with the database.
     * @param parent the parent object for the TreeView.
     */
    public DragNDropHelper(MailStorageDAO maildao, TreeItem<String> parent) {
        this.maildao = maildao;
        this.parent = parent;
    }
    
    /**
     * Executed when data is dragged over the target.
     * 
     * @param event the event that triggered this method.
     * @param treeCell the tree cell where the event happened.
     */
    public void dragOver(DragEvent event, TreeCell<String> treeCell) {
        //accept it only if it is  not dragged from the same node 
        //and if it has a string data 
        if (event.getGestureSource() != treeCell &&
                event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.MOVE);
        }
        event.consume();
    }
    
    /**
     * Executed when the drag-and-drop gesture entered the target.
     * 
     * @param event the event that triggered this method.
     * @param treeCell the tree cell where the event happened.
     */
    public void dragEnter(DragEvent event, TreeCell<String> treeCell) {
        // show to the user that it is an actual gesture target 
        if (event.getGestureSource() != treeCell 
                && event.getDragboard().hasString()
                && !treeCell.getText().equals(parent.getValue())) {
            treeCell.setTextFill(Color.web("#005797"));
        }
        event.consume();
    }
    
    /**
     * Executed when the drag-and-drop gesture exits the target.
     * 
     * @param event the event that triggered this method.
     * @param treeCell the tree cell where the event happened.
     */
    public void dragExit(DragEvent event, TreeCell<String> treeCell) {
        // show to the user that it is an actual gesture target 
        if (event.getGestureSource() != treeCell 
                && event.getDragboard().hasString()
                && !treeCell.getText().equals(parent.getValue())) {
            Dragboard db = event.getDragboard();
            if(db != null && !db.getString().equals(treeCell.getText()))
                treeCell.setTextFill(Color.WHITE);
            else
                treeCell.setTextFill(Color.BLACK);
        }
        event.consume();
    }
    
    /**
     * Executed when the source is dropped onto the target.
     * Changes the directory for the selected email.
     * 
     * @param event the event that triggered this method.
     * @param treeCell the tree cell where the event happened.
     * @param currentEmail the email that is dragged to new location.
     * @param emails the list that contains all emails associated 
     *               with the selected directory.
     * @param emailTable the TableView object that displays the emails.
     */
    public void dragDrop(DragEvent event, TreeCell<String> treeCell, 
                        EmailCustom currentEmail, ObservableList<EmailCustom> emails,
                        TableView<EmailCustom> emailTable) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            try {
                String oldDir = db.getString();
                String newDir = treeCell.getText();
                if(!oldDir.equals(newDir) 
                        && !newDir.equals(parent.getValue())) {
                    currentEmail.setDirectory(newDir);
                    maildao.updateEmailDirectory(currentEmail);
                    log.info("Changed directory for" + currentEmail 
                        + " from " + oldDir + " to " + newDir); 
                    emails.remove(currentEmail);
                    emailTable.refresh();
                }
                success = true;               
            } catch (SQLException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }
}
