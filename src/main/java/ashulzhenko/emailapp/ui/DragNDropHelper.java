package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import java.sql.SQLException;
import javafx.scene.control.TreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.slf4j.LoggerFactory;

/**
 * The helper class that responsible to handle drag n drop events.
 * 
 * @author Alena Shulzhenko
 * @version 12/11/2016
 * @since 1.8
 */
public class DragNDropHelper {
    
    private MailStorageDAO maildao;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Initializes the object.
     * 
     * @param maildao 
     */
    public DragNDropHelper(MailStorageDAO maildao) {
        this.maildao = maildao;
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
        if (event.getGestureSource() != treeCell &&
                event.getDragboard().hasString()) {
            treeCell.setTextFill(Color.GREEN);
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
        if (event.getGestureSource() != treeCell &&
                event.getDragboard().hasString()) {
            treeCell.setTextFill(Color.BLACK);
        }
        event.consume();
    }
    
    /**
     * Executed when the source is dropped onto the target.
     * 
     * @param event the event that triggered this method.
     * @param treeCell the tree cell where the event happened.
     * @param currentEmail the email that is dragged to new location.
     */
    public void dragDrop(DragEvent event, TreeCell<String> treeCell, 
                         EmailCustom currentEmail) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            try {
                String oldDir = db.getString();
                String newDir = treeCell.getText();
                currentEmail.setDirectory(newDir);
                maildao.updateEmailDirectory(currentEmail);
                success = true;
                log.info("Changed directory for" + currentEmail 
                        + " from " + oldDir + " to " + newDir); 
            } catch (SQLException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }
}
