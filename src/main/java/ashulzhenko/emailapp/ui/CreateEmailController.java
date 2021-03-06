package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import ashulzhenko.emailapp.interfaces.Mailer;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jodd.mail.EmailAddress;
import jodd.mail.EmailMessage;
import jodd.mail.MailAddress;
import org.slf4j.LoggerFactory;

/**
 * The controller that is responsible for displaying the stage for
 * creating an email module. 
 * An email can be created to be a new one or created as a reply 
 * or created in order to forward.
 * 
 * Note: since it is a child window, when an error occurs (e.g. SQLException),
 * the window is closed to display the main app, and the error is logged.
 *
 * @author Alena Shulzhenko
 * @version 18/11/2016
 * @since 1.8
 */
public class CreateEmailController implements Initializable {
    @FXML
    private HTMLEditor html;   
    @FXML
    private Button cancel;   
    @FXML
    private TextField to, subject, cc, bcc;
    
    private List<String> attach, embedAttach;
    private String[] toArray, ccArray, bccArray;
    
    private Mailer mail;
    private MailStorageDAO maildao;
    private ResourceBundle bundle;
    private FileChooser fileChooser;     
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Instantiates the object.
     */
    public CreateEmailController() {}
    
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
        attach = new ArrayList<>();
        embedAttach = new ArrayList<>();
        toArray = new String[0];
        ccArray = new String[0];
        bccArray = new String[0];
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
     * Sets Mailer in order to send the email and MailStorageDAO
     * in order to work with the database.
     * 
     * @param mail Mailer object that is used to send an email.
     * @param maildao data access object that is used to read/write to the database.
     */
    public void setMailUtilities(Mailer mail, MailStorageDAO maildao) { 
        this.mail = mail;
        this.maildao = maildao;
    }
    
    /**
     * Sets the FileChooser object.
     * 
     * @param fc the FileChooser object to set.
     */
    public void setFileChooser(FileChooser fc) {
        this.fileChooser = fc;
    }
    
    /**
     * Displays the email if it is to be forwarded or replied to.
     * 
     * @param email the email to be forwarded or replied to.
     */
    public void setEmail(EmailCustom email) {
        String text = getEmailText(email);
        html.setHtmlText("<font size='2'>"+text+"</font>");
        subject.setText(email.getSubject());
    }
    
    /**
     * If the email is a reply, sets the recipients
     * (sender if reply was chosen, sender and CC if reply all was chosen).
     * 
     * @param toStr the recipient addresses.
     */
    public void setText(String toStr) {
        to.setText(toStr);
    }
    
    /**
     * Returns the email information, such as subject, from email, to emails,
     * text and date, as a String.
     * 
     * @param email the email which information is returned.
     * 
     * @return the email information.
     */
    private String getEmailText(EmailCustom email) { 
        EmailDisplayHelper helper = new EmailDisplayHelper(bundle, email);
        StringBuilder text = new StringBuilder("<br/><br/>-------------------"
                + "----------------------------------<br/><b>");
        text.append(bundle.getString("subject")).append(":</b> ")
            .append(email.getSubject().substring(4)).append("<br/><b>")
            .append(bundle.getString("from")).append(":</b> ")
            .append(email.getFrom().getEmail()).append("<br/><b>")
            .append(bundle.getString("to")).append(":</b> ")
            .append(helper.getEmails(email.getTo())).append("<br/><b>")
            .append(bundle.getString("text")).append(":</b> ")
            .append(getMessages(email)).append(":</b> <b>")
            .append(bundle.getString("date")).append(":</b> ")
            .append(helper.getDate()).append("<br/>-------------------------")
            .append("----------------------------<br/>");
        text.trimToSize();
        return text.toString();
    }
    
    /**
     * Returns all messages belonging to the specified email.
     * 
     * @param email the email which message content is returned.
     * 
     * @return all messages belonging to the specified email.
     */
    private StringBuilder getMessages(EmailCustom email) {
        StringBuilder message = new StringBuilder("");
        List<EmailMessage> list = email.getAllMessages();
        for(EmailMessage em : list)
            message.append(em.getContent()).append("<br/>");
        message.trimToSize();
        return message;
    }
    
    /**
     * Creates and sends the new email if user input is valid.
     * The stage is hidden afterwards.
     * The email is saved in the database.
     * 
     * @param event the event that triggered this action.
     */
    @FXML
    private void onSubmit(ActionEvent event) { 
        EmailCustom email = new EmailCustom();
        boolean allValid = validate(email);      
        if(allValid) {  
            try {       
                email = mail.sendEmail(toArray, ccArray, bccArray, 
                        subject.getText(), html.getHtmlText(), 
                        attach.toArray(new String[attach.size()]), 
                        embedAttach.toArray(new String[embedAttach.size()])); 
                //set sent date
                email.setSentDate(Date.from(LocalDateTime.now().
                        atZone(ZoneId.systemDefault()).toInstant()));
                log.info("The email was sent: " + email);
                maildao.saveEmail(email); 
            }
            catch(SQLException ex) {
                log.error("Unable to save new email: ", ex);  
            }
            finally {
                Stage stage = (Stage) cancel.getScene().getWindow();
                stage.close();    
            }
        }      
    }
    
    /**
     * Validates TO, CC, and BCC emails.
     * If values are valid they are added to the email.
     * The value is invalid if it is an empty string or it is
     * in invalid email format.
     * 
     * @param email the email that the correct emails are added to.
     * 
     * @return true if all emails are valid; false otherwise.
     */
    private boolean validate(EmailCustom email) {
        //TO
        if(to.getText().trim().isEmpty()) {
            displayError(bundle.getString("noValueErr"));
            return false;
        }
        toArray = checkEmails(to);
        if(toArray == null)
            return false;
        
        //CC
        if(!cc.getText().trim().isEmpty()) {
            ccArray = checkEmails(cc);
            if(ccArray == null)
                return false;
        }
        //BCC
        if(!bcc.getText().trim().isEmpty()) {
            bccArray = checkEmails(bcc);
            if(bccArray == null)
                return false;
        }       
        return true;
    }
    
    /**
     * Verifies if user-provided emails are in valid format.
     * If there are several emails they should be separated with the semicolon.
     * Duplicate email addresses are not allowed.
     * 
     * @param text the user inputted emails to check.
     * 
     * @return the array of emails if emails are valid; null otherwise.
     */
    private String[] checkEmails(TextField text) {
        String[] emails = text.getText().trim().split(";");
        Set<String> set = new HashSet<>();
        
        for(int i = 0; i < emails.length; i++) {
            String address = emails[i].trim();                                
            if (address.contains(" ") || !(new EmailAddress(address)).isValid()) {
                displayError(bundle.getString("invalidEmailErr") + " " + address 
                        + ". " + bundle.getString("multiEmailErr"));
                return null;
            }
            set.add(address);
        }      
        if(set.size() < emails.length) {
            displayError(bundle.getString("duplicateEmailErr"));
            return null;
        }
        
        return emails;
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
     * Opens the dialog so that the user can choose 
     * files to add as attachments.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void addAttachments(ActionEvent event) {
        Stage stage = (Stage) html.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) { 
            attach.add(file.getAbsolutePath());     
        }
    }
    
     /**
     * Opens the dialog so that the user can choose 
     * files to add as embedded attachments.
     * The file is then embedded in the message body.
     * 
     * @param event the event that triggered this action.
     */
    @FXML 
    private void addEmbed(ActionEvent event) {
        Stage stage = (Stage) html.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) { 
            String path = file.getAbsolutePath();
            embedAttach.add(path);
            html.setHtmlText(html.getHtmlText()+"<img src='cid:"+file.getName()+"'/>");
        }
    }
    
    

}
