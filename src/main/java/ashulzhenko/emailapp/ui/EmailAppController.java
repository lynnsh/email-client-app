package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.data.FolderStorageModule;
import ashulzhenko.emailapp.data.MailStorageModule;
import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import ashulzhenko.emailapp.mail.MailModule;
import ashulzhenko.emailapp.properties.PropertiesManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.MailAddress;
import org.slf4j.LoggerFactory;

public class EmailAppController implements Initializable {
    
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
    
    private ObservableList<EmailCustom> emails = FXCollections.observableArrayList();
    private ObservableList<String> dirs;
    
    private boolean selectedEmail;
    private EmailCustom currentEmail;
    private String currentDir;
    
    private ResourceBundle bundle;
    private FileChooser fileChooser;
    private PropertiesManager pm;
    //the path to properties file
    private static String PROPERTIES_PATH;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public EmailAppController() {}
    
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        this.bundle = rb;
        fileChooser = new FileChooser();
        
        dirTree.setRoot(new TreeItem<>(bundle.getString("dirs")));
        
        /*dirTree.setCellFactory(data -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(item);
                    //setGraphic(getTreeItem().getGraphic());
                } else {
                    setText("");
                    //setGraphic(null);
                }
            }
        });*/
        
        
        dirTree.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> stringTreeView) {
                TreeCell<String> treeCell = new TreeCell<String>() {
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null) {
                            setText(item);
                            //setGraphic(getTreeItem().getGraphic());
                        } else {
                            setText("");
                            //setGraphic(null);
                        }
                    }
                };
                
                treeCell.setOnDragOver(new EventHandler <DragEvent>() {
                public void handle(DragEvent event) {
                    //data is dragged over the target 

                    ///accept it only if it is  not dragged from the same node 
                     //and if it has a string data 
                    if (event.getGestureSource() != treeCell &&
                            event.getDragboard().hasString()) {
                        //allow for both copying and moving, whatever user chooses 
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }

                    event.consume();
                    }
                });
                
                treeCell.setOnDragEntered(new EventHandler <DragEvent>() {
                    public void handle(DragEvent event) {
                        /* the drag-and-drop gesture entered the target */
                        /* show to the user that it is an actual gesture target */
                        if (event.getGestureSource() != treeCell &&
                                event.getDragboard().hasString()) {
                            treeCell.setTextFill(Color.GREEN);
                        }

                        event.consume();
                    }
                });

                treeCell.setOnDragDropped(new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        Dragboard db = event.getDragboard();
                        boolean success = false;
                        if (db.hasString()) {
                            try {
                                String oldDir = db.getString();
                                String newDir = treeCell.getText();
                                currentEmail.setDirectory(newDir);
                                maildao.updateEmailDirectory(currentEmail);
                                success = true;
                                log.info("Changed firectory for" + currentEmail 
                                        + " from " + oldDir + " to " + newDir);
                            } catch (SQLException ex) {
                                log.error(ex.getMessage(), ex);
                            }
                        }
                        event.setDropCompleted(success);
                        event.consume();
                    }
                });

                return treeCell;
            }
        });
        
        
        contactColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper
                (getContact(cellData)));
        subjectColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper
                (cellData.getValue().getSubject()));
        dateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper
                (getDate(cellData)));
        
        htmlDisplay.setDisable(true);
    }    

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;    
    }

    public void setUserInfo(UserConfigBean user) {
        try {
            this.user = user;
            mail = new MailModule(user);
            maildao = new MailStorageModule(user);
            folderdao = new FolderStorageModule(user);
            checkNewEmails();
            dirs = FXCollections.observableArrayList(folderdao.findAll());      
            
            if (dirs != null) {
                for (String str : dirs) {
                    TreeItem<String> item = new TreeItem<>(str);
                    dirTree.getRoot().getChildren().add(item);
                }
            }
            
            dirTree.getRoot().setExpanded(true);
            
            dirTree.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> getDirEmails(newValue));
            
            emailTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EmailCustom>() {
                @Override
                public void changed(ObservableValue<? extends EmailCustom> obs, 
                                    EmailCustom oldSelection, EmailCustom newSelection) {
                    if(newSelection != null) {
                        selectedEmail = true;
                        currentEmail = newSelection;
                        displayEmail();
                    }
                }
            });
        } catch (SQLException ex) {
            log.error("Error connecting to the database.", ex.getMessage());
            Platform.exit();
        }
    }
    
    private void getDirEmails(TreeItem<String> directory) {
        if (directory != null) {
            try {               
                currentDir = directory.getValue();
                List <EmailCustom> emailsFromDb = maildao.findAllInDirectory(currentDir);
                emails = FXCollections.observableArrayList(emailsFromDb);
                emailTable.setItems(emails);
                setColumnName(emailsFromDb);

            } catch (SQLException e) {
                log.error("Error connecting to the database: ", e.getMessage());
                Platform.exit();
            }
        }
    }
    
    private void displayEmail() {
        String email = 
              "<b>" + bundle.getString("subject") + ":</b> " + currentEmail.getSubject() + "<br/>"
            + "<b>" + bundle.getString("from") + ":</b> " + currentEmail.getFrom().getEmail() + "<br/>"
            + "<b>" + bundle.getString("to") + ":</b> " + getEmails(currentEmail.getTo()) + "<br/>"
            + (currentEmail.getCc().length != 0? "<b>CC:</b> " +  getEmails(currentEmail.getCc()) + "<br/>" : "")
            + (currentEmail.getBcc().length != 0? "<b>BCC:</b> " +  getEmails(currentEmail.getBcc()) + "<br/>" : "")
            + "<b>" + bundle.getString("text") + ":</b> " + getMessages() + "<br/>"
            + "<b>" + bundle.getString("date") + ":</b> " + getDate();
        htmlDisplay.setHtmlText(email);
        
    }
    
    private String getDate() {
        LocalDateTime date;
        if(currentEmail.getReceivedDate() == null)
            date = LocalDateTime.ofInstant(currentEmail.getSentDate().toInstant(), ZoneId.systemDefault());
        else
            date = LocalDateTime.ofInstant(currentEmail.getReceivedDate().toInstant(), ZoneId.systemDefault());
        
           return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
    }
    
    private String getEmails(MailAddress[] array) {
        String str = "";
        for(MailAddress ma : array)
            str += ma.getEmail() + "; ";
        
        if(array.length > 1)
            str = str.substring(0, str.length()-2);
        return str;
    }
    
    private String getMessages() {
        String message = "";
        List<EmailMessage> list = currentEmail.getAllMessages();
        message = list.stream().map((em) -> em.getContent() + ". ").reduce(message, String::concat);
        
        return message;
    }
    
    @FXML
    private void checkNew(ActionEvent event) { 
        checkNewEmails();     
    }
    
    private void checkNewEmails(){
        try {
            List<EmailCustom> list = mail.receiveEmail();
            for(EmailCustom e : list)
                maildao.saveEmail(e);
        } catch (SQLException ex) {
            log.error("Unable to save new emails: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    
    private String getContact(TableColumn.CellDataFeatures<EmailCustom,String> cellData) {
        EmailCustom email = cellData.getValue();
        String contact = email.getFrom().getEmail();
        if (contact.equals(user.getFromEmail()))
            contact = email.getTo()[0].getEmail();                
        return contact;
    }
    
    private String getDate(TableColumn.CellDataFeatures<EmailCustom,String> cellData) {
        EmailCustom email = cellData.getValue();
        LocalDateTime date;
        LocalDateTime now = LocalDateTime.now();
        if(email.getFrom().getEmail().equals(user.getFromEmail()))
            date = LocalDateTime.ofInstant(email.getSentDate().toInstant(), ZoneId.systemDefault());
        else
            date = LocalDateTime.ofInstant(email.getReceivedDate().toInstant(), ZoneId.systemDefault());
        
       if(now.toLocalDate().equals(date.toLocalDate()))
           return date.format(DateTimeFormatter.ofPattern("hh:mm a"));
       else
           return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    
    private void setColumnName(List<EmailCustom> emailsFromDb) {
        if(emailsFromDb.size() > 0) {
            String contact = emailsFromDb.get(0).getFrom().getEmail();
            if(contact.equals(user.getFromEmail()))
                contactColumn.setText(bundle.getString("to"));
            else
                contactColumn.setText(bundle.getString("from"));
        }
        else
            contactColumn.setText(bundle.getString("from"));
    }
    
    @FXML 
    private void deleteEmail(ActionEvent event) {
        try {
            if(selectedEmail) {
                System.out.println(currentEmail.getId()+"");
                maildao.deleteEmail(currentEmail.getId());
                emails.remove(currentEmail);                   
            }
            else
                displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
            selectedEmail = false;
        }
        catch (SQLException ex) {
            log.error("Unable to delete email: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    @FXML
    private void onClose(ActionEvent event) {
        Platform.exit();
    }
    
    
    private void displayAlert(String message, AlertType type) {
        Alert dialog = new Alert(type);
        dialog.setTitle("" + bundle.getString("alertTitle") + "!");
        //dialog.setHeaderText(message);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    
    @FXML 
    private void configure(ActionEvent event) {
        try {
            Stage stage = new Stage();
            mainApp.displayForm(stage, user, true);
            //restore valid data if user cancels with invalid data
            user = pm.loadTextProperties(PROPERTIES_PATH, "data");
        } catch (IOException ex) {
            log.error("Error retrieving properties file: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    @FXML 
    private void forwardEmail(ActionEvent event) {
        if(selectedEmail) {
            currentEmail.subject("FW: " + currentEmail.getSubject());
            create(currentEmail);
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        selectedEmail = false;
    }
    
    @FXML 
    private void replyToEmail(ActionEvent event) {
        if(selectedEmail) {
            currentEmail.subject("RE: " + currentEmail.getSubject());
            create(currentEmail);
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        selectedEmail = false;
    }
    
    @FXML 
    private void newEmail(ActionEvent event) {
        create(null);
    }
    
    private void create(EmailCustom email) {
        try {
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("newTitle"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateEmail.fxml"), bundle);
            BorderPane root = (BorderPane)loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            
            CreateEmailController controller = loader.getController();
            controller.setUserInfo(user, mail, maildao);
            controller.setFileChooser(fileChooser);
            if(email != null) 
                controller.setEmail(email);
            stage.show();
        } catch (IOException ex) {
            log.error("Error reading the file: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    
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
    
    @FXML 
    private void newDir(ActionEvent event) {   
        TreeItem<String> parent = dirTree.getRoot();
        getDirectoryFromUser(null, parent);
    }
    
    @FXML 
    private void renameDir(ActionEvent event) {
        TreeItem<String> item = dirTree.getSelectionModel().getSelectedItem();
        if(item != null && !item.getValue().equals(bundle.getString("dirs"))) {    
            TreeItem<String> parent = item.getParent();
            getDirectoryFromUser(item, parent);
        }
        else
            displayAlert(bundle.getString("notSelectedDirErr"), Alert.AlertType.ERROR);           
    }
    
    
    
    private void getDirectoryFromUser(TreeItem<String> directory, TreeItem<String> parent) {
        try {
            Stage stage = new Stage();
            stage.setTitle(bundle.getString("dirTitle"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifyDir.fxml"), bundle);
            GridPane root = (GridPane)loader.load();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            scene.getStylesheets().add("/styles/Styles.css");
            
            ModifyDirController controller = loader.getController();
            controller.setDirectory(directory);
            controller.setFolderDAO(folderdao);
            controller.setDirList(dirs);
            controller.setTreeParent(parent);
            stage.showAndWait();
         } catch (IOException ex) {
            log.error("Error reading the file: ", ex.getMessage());
            Platform.exit();
        }
    }
    
    @FXML 
    private void onSaveAttach(ActionEvent event) {
        if(selectedEmail) {
            List<EmailAttachment> list = currentEmail.getAttachments();
            if(list != null && list.size() > 0) {
                for(EmailAttachment attach : list) {
                    fileChooser.setTitle(bundle.getString("saveAttach"));
                    fileChooser.setInitialFileName(attach.getName());
                    File savedFile = fileChooser.showSaveDialog(emailTable.getScene().getWindow());
                    
                    if(savedFile != null) {
                        try(FileOutputStream fos = new FileOutputStream(savedFile);
                            InputStream is = new ByteArrayInputStream(attach.toByteArray());) {
                            byte[] buffer = new byte[1];

                            while(is.read(buffer) > 0)
                                fos.write(buffer);
                        }
                        catch(IOException io) {
                            log.error("Error writing to the disk", io);
                        }
                    }
                    
                }
            }
            else
                displayAlert(bundle.getString("noAttachmentsW"), Alert.AlertType.WARNING);
        }
        else
            displayAlert(bundle.getString("notSelectedEmailErr"), Alert.AlertType.ERROR);
        selectedEmail = false;
    }
    
     @FXML
    private void onDragDetect(MouseEvent event) {
        Dragboard db = emailTable.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(currentEmail.getDirectory());
        log.debug("CURRENT DETECT: "+currentEmail.getDirectory());
        db.setContent(content);
        event.consume();
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
