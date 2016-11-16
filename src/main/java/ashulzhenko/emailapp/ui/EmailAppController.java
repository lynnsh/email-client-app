package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.data.FolderStorageModule;
import ashulzhenko.emailapp.data.MailStorageModule;
import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import ashulzhenko.emailapp.mail.MailModule;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import jodd.mail.EmailAttachment;
import jodd.mail.MailAddress;
import org.slf4j.LoggerFactory;

/**
 * The controller that is responsible for displaying the stage for
 * the main application module.
 * It allows the user to change email folder, call dialogs to send emails,
 * modify folder tree structure, download attachments, check for new emails,
 * view messages content.
 * Since there is no support for AttachedMessages feature, the text and
 * attachments of embedded message are stored as a String.
 *
 * @author Alena Shulzhenko
 * @version 15/11/2016
 * @since 1.8
 */
public class EmailAppController {
    
    @FXML
    private TableView<EmailCustom> emailTable;
    @FXML
    private TableColumn<EmailCustom, String> contactColumn, subjectColumn, dateColumn;
    
    @FXML
    private TreeView<String> dirTree;

    @FXML
    private HTMLEditor htmlDisplay;
    
    private MainApp mainApp;
    private MailStorageDAO maildao;
    private FolderStorageDAO folderdao;
    private MailModule mail;
    private UserConfigBean user;
    
    private ObservableList<EmailCustom> emails;
    private ObservableList<String> dirs;
    
    private EmailCustom currentEmail;
    private DragNDropHelper dropHelper;
    private EmailDisplayHelper displayHelper;
    
    private ResourceBundle bundle;
    private FileChooser fileChooser;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Instantiates the object.
    */
    public EmailAppController() {}
    
    /**
     * Sets the ResourceBundle used for localization.
     * 
     * @param rb the resources used to localize the root object, 
     *           or null if the root object was not localized.
     */
    public void setBundle(ResourceBundle rb) {
        this.bundle = rb;      
        controllerSetUp();     
    }
    
    /**
     * Initializes the important GUI components.
     */
    private void controllerSetUp() {
        fileChooser = new FileChooser();        
        dirTree.setRoot(new TreeItem<>(bundle.getString("dirs")));        
        setTreeEvents();       
        
        emails = FXCollections.observableArrayList();
        contactColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper
                (getContact(cellData)));
        subjectColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper
                (cellData.getValue().getSubject()));
        dateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper
                (getDate(cellData)));
        
        //htmlDisplay.setDisable(true);
        htmlDisplay.setHtmlText("<body style='background-color: black; "
                                + "color: white;'/>");
        
    }
    
    /**
     * Returns the contact to display in the table.
     * The TO contact is displayed if it is the user who sent the message,
     * FROM if the user received the message.
     * 
     * @param cellData the cell where the contact will be displayed.
     * 
     * @return the contact to display in the cell. 
     */
    private String getContact(TableColumn.CellDataFeatures<EmailCustom,String> cellData) {
        EmailCustom email = cellData.getValue();
        String contact = email.getFrom().getEmail();
        if (contact.equals(user.getFromEmail()) && email.getTo().length != 0)
            contact = email.getTo()[0].getEmail();                
        return contact;
    }
    
    /**
     * Returns the date of the email to display in the table.
     * If the user sent the email, the sent date will be displayed; 
     * otherwise the received date will be displayed.
     * If the email is dated today, the time is displayed;
     * otherwise the date is displayed.
     * 
     * @param cellData the cell where the contact will be displayed.
     * 
     * @return the date to display in the cell. 
     */
    private String getDate(TableColumn.CellDataFeatures<EmailCustom,String> cellData) {
        EmailCustom email = cellData.getValue();
        LocalDateTime date;
        LocalDateTime now = LocalDateTime.now();
        if(email.getFrom().getEmail().equals(user.getFromEmail()))
            date = LocalDateTime.ofInstant(email.getSentDate().toInstant(), 
                    ZoneId.systemDefault());
        else
            date = LocalDateTime.ofInstant(email.getReceivedDate().toInstant(), 
                    ZoneId.systemDefault());
        
       if(now.toLocalDate().equals(date.toLocalDate()))
           return date.format(DateTimeFormatter.ofPattern("hh:mm a"));
       else
           return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    /**
     * Sets tree events, such as to update the item, and drag n drop events.
     */
    private void setTreeEvents() {
        dirTree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> stringTreeView) {
                TreeCell<String> treeCell = new TreeCell<String>() {
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null) {
                            setText(item);
                            setGraphic(getTreeItem().getGraphic());
                        } else {
                            setText("");
                            setGraphic(null);
                        }
                    }
                };      
                dropHelper = new DragNDropHelper(maildao, dirTree.getRoot());
                treeCell.setOnDragOver(event -> dropHelper.dragOver(event, treeCell));
                treeCell.setOnDragEntered(event -> dropHelper.dragEnter(event, treeCell));
                treeCell.setOnDragExited(event -> dropHelper.dragExit(event, treeCell));
                treeCell.setOnDragDropped(event -> dropHelper.dragDrop(event, treeCell, 
                                                  currentEmail, emails, emailTable));
                return treeCell;
            }
        });
    }     
    
    /**
     * Executed when there is a drag event detected.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void onDragDetect(MouseEvent event) {
        if(currentEmail != null) {
            Dragboard db = emailTable.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(currentEmail.getDirectory());
            db.setContent(content);           
        }
        event.consume();
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
     * Sets the user information necessary for the app to function,
     * and sets the corresponding GUI elements with user data.
     * 
     * @param user the user information.
     */
    public void setUserInfo(UserConfigBean user) {
        try {
            this.user = user;
            mail = new MailModule(user);
            maildao = new MailStorageModule(user);
            folderdao = new FolderStorageModule(user);
            refreshApp();
                  
            
            dirTree.getRoot().setExpanded(true);
            dirTree.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> getDirEmails(newValue, oldValue));
            
            emailTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> emailSelected(newValue));
            
        } catch (SQLException ex) {
            log.error("Error connecting to the database.", ex.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Populates TreeView with values from the database.
     */
    private void populateTreeView() {
        if (dirs != null) {
            //update TreeView directories
            if(dirTree.getRoot().getChildren() != null)
                dirTree.getRoot().getChildren().clear();
            for (String str : dirs) {
                TreeItem<String> item = new TreeItem<>(str);                
                dirTree.getRoot().getChildren().add(item);
                item.setGraphic(new ImageView("/images/closedir.png"));
            }
        }
    }
    
    /**
     * Queries the database for emails corresponding to the selected directory.
     * 
     * @param directory the directory for which the emails are queried.
     * @param oldDirectory the previously selected directory.
     */
    private void getDirEmails(TreeItem<String> directory, TreeItem<String> oldDirectory) {
        if (directory != null) {
            try {       
                if(directory.getParent() != null) {
                    directory.setGraphic(new ImageView("/images/opendir.png"));
                    if(oldDirectory != null && oldDirectory.getParent() != null)
                        oldDirectory.setGraphic(new ImageView("/images/closedir.png"));
                }
                dirTree.refresh();
                String currentDir = directory.getValue();
                List <EmailCustom> emailsFromDb = maildao.findAllInDirectory(currentDir);
                emails = FXCollections.observableArrayList(emailsFromDb);
                emailTable.setItems(emails);
                setColumnName(emailsFromDb);
                htmlDisplay.setHtmlText("<body style='background-color: black; "
                                        + "color: white;'/>");
            } catch (SQLException e) {
                log.error("Error connecting to the database: ", e.getMessage());
                Platform.exit();
            }
        }
    }
    
    /**
     * Depending on which email is chosen (either the user sent it or received it)
     * the appropriate column name is displayed.
     * If the directory contains emails sent and received by the user,
     * the column name is determined by the fist email.
     * 
     * @param emailsFromDb the emails queried from the database to be displayed.
     */
    private void setColumnName(List<EmailCustom> emailsFromDb) {
        if(emailsFromDb.size() > 0) {
            //get one contact to determine who was the sender
            String contact = emailsFromDb.get(0).getFrom().getEmail();
            if(contact.equals(user.getFromEmail()))
                contactColumn.setText(bundle.getString("to"));
            else
                contactColumn.setText(bundle.getString("from"));
        }
        else
            contactColumn.setText(bundle.getString("from"));
    }
    
    /**
     * Called when user selects an email.
     * The email content is displayed.
     * 
     * @param newSelection the user selected email.
     */
    private void emailSelected(EmailCustom newSelection) {
        if(newSelection != null) {
            currentEmail = newSelection;
            displayHelper = new EmailDisplayHelper(bundle, currentEmail);
            htmlDisplay.setHtmlText(displayHelper.getEmailText());
        }
    } 
    
    /**
     * Gets new messages from the server and updates the tree structure.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void checkNew(ActionEvent event) { 
        refreshApp();     
    }
    
    /**
     * Looks for the new messages on the server and updates the tree structure.
     * If new emails are found, adds them to the email table
     * and saves them to the database.
     */
    private void refreshApp(){
        try {
            //get new messages
            List<EmailCustom> list = mail.receiveEmail();
            //save new messages in the database
            for(EmailCustom e : list)
                maildao.saveEmail(e);
            //update directories
            dirs = FXCollections.observableArrayList(folderdao.findAll());             
            populateTreeView();
        } catch (SQLException ex) {
            log.error("Error when working with the database.", ex.getMessage());
            Platform.exit();
        }
    }  
    
    /**
     * Closes the application.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void onClose(ActionEvent event) {
        Platform.exit();
    }
    
    /**
     * Shows the information about this application.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void about(ActionEvent event) {
        try {
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("about"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/About.fxml"), bundle);
            GridPane root = (GridPane)loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Ubuntu");
            stage.initOwner((Stage) emailTable.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException ex) {
            log.error("Error reading the file: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Displays a warning or error alert dialog if user input was invalid.
     * 
     * @param message the error message to display in the alert dialog.
     */
    private void displayAlert(String message, AlertType type) {
        Alert dialog = new Alert(type);
        dialog.setTitle(bundle.getString("alertTitle") + "!");
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    /**
     * Shows the configure module.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void configure(ActionEvent event) {
        Stage current = (Stage) emailTable.getScene().getWindow();
        mainApp.displayForm(current, user, true);
    }
    
    //EMAIL EVENT HANDLERS
    
    /**
     * Deletes the selected email.
     * Removes email from table and database.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void deleteEmail(ActionEvent event) {
        try {
            if(currentEmail != null) {
                maildao.deleteEmail(currentEmail.getId());
                emails.remove(currentEmail);    
                emailTable.refresh();
            }
            else
                displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
            currentEmail = null;
        }
        catch (SQLException ex) {
            log.error("Unable to delete email: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Opens a new window to forward the selected email.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void forwardEmail(ActionEvent event) {
        if(currentEmail != null) {
            currentEmail.subject("FW: " + currentEmail.getSubject());
            createEmail(currentEmail, null);
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        currentEmail = null;
    }
    
    /**
     * Opens a new window to reply to the selected email.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void replyToEmail(ActionEvent event) {
        if(currentEmail != null) {
            currentEmail.subject("RE: " + currentEmail.getSubject());
            createEmail(currentEmail, currentEmail.getFrom().getEmail());
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        currentEmail = null;
    }
    
    /**
     * Opens a new window to reply to all recipients 
     * and the sender of the selected email.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void replyToAll(ActionEvent event) {
        
        if(currentEmail != null) {
            currentEmail.subject("RE: " + currentEmail.getSubject());
            MailAddress[] cc = currentEmail.getCc();
            String address = cc == null ? "" : displayHelper.getEmails(cc);
            createEmail(currentEmail, currentEmail.getFrom().getEmail() 
                            + ";" + address);
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        currentEmail = null;
    }
    
    /**
     * Opens a new window to createEmail new email.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void newEmail(ActionEvent event) {
        createEmail(null, null);
    }
    
    /**
     * Creates new stage to display a new window in order to create new email.
     * If given email is not null, it will be forwarded or replied to.
     * 
     * @param email the event that triggered this action.
     * @param to the to field to fill in; null for new email and forward.
     */
    private void createEmail(EmailCustom email, String to) {
        try {
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("newTitle"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateEmail.fxml"), bundle);
            BorderPane root = (BorderPane)loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Ubuntu");
            CreateEmailController controller = loader.getController();
            controller.setMailUtilities(mail, maildao);
            controller.setFileChooser(fileChooser);
            if(email != null) 
                controller.setEmail(email);
            if(to != null)
                controller.setText(to);
            stage.initOwner((Stage) emailTable.getScene().getWindow());
            stage.showAndWait();
            emailTable.refresh();
            dirTree.refresh();
        } catch (IOException ex) {
            log.error("Error reading the file: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    //DIRECTORY EVENT HANDLERS
    
    /**
     * Deletes the selected directory.
     * The directory is deleted from the tree, list and database.
     * It is invalid to delete the parent node.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void deleteDir(ActionEvent event) {
        try {
            TreeItem<String> item = dirTree.getSelectionModel().getSelectedItem();
            if(item != null) {
                TreeItem<String> parent = item.getParent();
                if (parent == null )
                    displayAlert(bundle.getString("parentErr"), Alert.AlertType.ERROR);
                else {
                    parent.getChildren().remove(item);
                    dirs.remove(item.getValue());
                    folderdao.deleteDirectory(item.getValue());
                }
            }
            else
                displayAlert(bundle.getString("notSelectedDirErr"), Alert.AlertType.ERROR);
        }
        catch (SQLException ex) {
            log.error("Unable to delete directory: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Creates new directory.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void newDir(ActionEvent event) {   
        TreeItem<String> parent = dirTree.getRoot();
        getDirectoryFromUser(null, parent);
    }
    
    /**
     * Renames the selected directory.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void renameDir(ActionEvent event) {
        TreeItem<String> item = dirTree.getSelectionModel().getSelectedItem();
        if(item != null && !item.getValue().equals(dirTree.getRoot().getValue())) {    
            TreeItem<String> parent = item.getParent();
            getDirectoryFromUser(item, parent);
        }
        else
            displayAlert(bundle.getString("notSelectedDirErr"), Alert.AlertType.ERROR);           
    }
      
    /**
     * Creates new stage to display a new window in order to add new folder
     * or rename a selected folder.
     * 
     * @param directory the directory to rename; null otherwise.
     * @param parent the parent tree item of the selected directory.
     */
    private void getDirectoryFromUser(TreeItem<String> directory, TreeItem<String> parent) {
        try {
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("dirTitle"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifyDir.fxml"), bundle);
            GridPane root = (GridPane)loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            scene.getStylesheets().add("http://fonts.googleapis.com/css?family=Ubuntu");
            ModifyDirController controller = loader.getController();
            controller.setDirectory(directory);
            controller.setFolderDAO(folderdao);
            controller.setDirList(dirs);
            controller.setTreeParent(parent);
            stage.initOwner((Stage) emailTable.getScene().getWindow());
            stage.showAndWait();
         } catch (IOException ex) {
            log.error("Error reading the file: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Saves attachments of the selected email to the disk.
     * @param event the event that triggered this action.
     */
    @FXML 
    private void onSaveAttach(ActionEvent event) {
        if(currentEmail != null) {
            List<EmailAttachment> list = currentEmail.getAttachments();
            if(list != null && list.size() > 0) {
                for(EmailAttachment attach : list) {
                    File savedFile = getPath(attach);                 
                    if(savedFile != null)
                        saveFileToDisk(savedFile, attach);
                }
            }
            else
                displayAlert(bundle.getString("noAttachmentsW"), Alert.AlertType.WARNING);
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        currentEmail = null;
    }
    
    /**
     * Get the path from the user where to save the file.
     * 
     * @param file the file to save to disk.
     * 
     * @return the path from the user where to save the file.
     */
    private File getPath(EmailAttachment file) {
        fileChooser.setTitle(bundle.getString("saveAttach"));
        fileChooser.setInitialFileName(file.getName());
        return fileChooser.showSaveDialog(emailTable.getScene().getWindow());
    }
    
    /**
     * Saves selected file to disk.
     * 
     * @param path the user-chosen path where to save the attachment.
     * @param file the attachment to save to the disk.
     */
    private void saveFileToDisk(File path, EmailAttachment file) {
        try(FileOutputStream fos = new FileOutputStream(path);
            InputStream is = new ByteArrayInputStream(file.toByteArray());) {
            byte[] buffer = new byte[1];

            while(is.read(buffer) > 0)
                fos.write(buffer);
        }
        catch(IOException io) {
            log.error("Error writing to the disk", io);
        }
    }

    
    
}
