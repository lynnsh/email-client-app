package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import ashulzhenko.emailapp.interfaces.Mailer;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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

public class CreateEmailController implements Initializable {
    @FXML
    private HTMLEditor html;
    
    @FXML
    private Button cancel;
    
    @FXML
    private TextField to, subject, cc, bcc;
    
    private MainApp mainApp;
    private Mailer mail;
    private MailStorageDAO maildao;
    private UserConfigBean user;
    private ResourceBundle bundle;
    private FileChooser fileChooser;
    private List<String> attach, embedAttach;
    private String[] toArray, ccArray, bccArray;
    
    private final org.slf4j.Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public CreateEmailController() {}
    
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

    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.close();
    }

    public void setUserInfo(UserConfigBean user, Mailer mail, MailStorageDAO maildao) {
        this.user = user; 
        this.mail = mail;
        this.maildao = maildao;
    }
    
    public void setEmail(EmailCustom email) {
        String text = getEmailText(email);
        html.setHtmlText("<font size='2'>"+text+"</font>");
        subject.setText(email.getSubject());
        if(subject.getText().contains("RE:"))
            to.setText(email.getFrom().getEmail());
    }
    
    private String getEmailText(EmailCustom email) {  
        return "<br/><br/><br/>---------------------------------<br/>"
            + "<b>" + bundle.getString("subject") + ":</b> " + email.getSubject().substring(4) + "<br/>"
            + "<b>" + bundle.getString("from") + ":</b> " + email.getFrom().getEmail() + "<br/>"
            + "<b>" + bundle.getString("to") + ":</b> " + getTo(email) + "<br/>"
            + "<b>" + bundle.getString("text") + ":</b> " + getMessages(email) + "<br/>"
            + "<b>" + bundle.getString("date") + ":</b> " + getDate(email);
    }
    
    private String getDate(EmailCustom email) {
        LocalDateTime date;
        if(email.getReceivedDate() == null)
            date = LocalDateTime.ofInstant(email.getSentDate().toInstant(), ZoneId.systemDefault());
        else
            date = LocalDateTime.ofInstant(email.getReceivedDate().toInstant(), ZoneId.systemDefault());
        
           return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
    }
    
    private String getTo(EmailCustom email) {
        String emails = "";
        MailAddress[] array = email.getTo();
        for(MailAddress ma : array)
            emails += ma.getEmail() + "; ";       
        if(array.length > 1)
            emails = emails.substring(0, emails.length()-2);
        return emails;
    }
    
    private String getMessages(EmailCustom email) {
        String message = "";
        List<EmailMessage> list = email.getAllMessages();
        message = list.stream().map((em) -> em.getContent() + ". ").reduce(message, String::concat);
        
        return message;
    }
    
    @FXML
    private void onSubmit(ActionEvent event) { 
        EmailCustom email = new EmailCustom();
        boolean allValid = validate(email);
        
        if(allValid) {  
            try{
                
                email = mail.sendEmail(toArray, ccArray, bccArray, 
                        subject.getText(), html.getHtmlText(), 
                        attach.toArray(new String[attach.size()]), 
                        embedAttach.toArray(new String[embedAttach.size()]));
                
                email.setSentDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                maildao.saveEmail(email);
                ((Node)(event.getSource())).getScene().getWindow().hide();
            }
            catch(IllegalArgumentException e) {
                displayError(e.getMessage());
            }
            catch(SQLException ex) {
                log.error("Unable to save new email: ", ex.getMessage());
                Platform.exit();
            }

        }
    }
    
    private boolean validate(EmailCustom email) {
        if(to.getText().trim().isEmpty()) {
            displayError("" + bundle.getString("noValueErr") + " " + to.getId());
            return false;
        }
        if(!checkEmails(to))
            return false;
        else
            toArray = to.getText().trim().split(";");
        
        if(!cc.getText().trim().isEmpty()) {
            if(!checkEmails(cc))
                return false;
            else
                ccArray = cc.getText().trim().split(";");
        }
        
        if(!bcc.getText().trim().isEmpty()) {
            if(!checkEmails(bcc))
                return false;
            else
                bccArray = bcc.getText().trim().split(";");
        }
        
        return true;
    }
    
    private boolean checkEmails(TextField text) {
        String[] emails = text.getText().trim().split(";");
        for(String e : emails) {
            if (!(new EmailAddress(e)).isValid()) {
                displayError("" + bundle.getString("invalidEmailErr") + " " + e);
                return false;
            }
        }      
        return true;
    }
    
    private void displayError(String message) {
        Alert dialog = new Alert(Alert.AlertType.WARNING);
        dialog.setTitle("" + bundle.getString("alertTitle") + "!");
        //dialog.setHeaderText(message);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    @FXML 
    private void addAttachments(ActionEvent event) {
        Stage stage = (Stage) html.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) { 
            attach.add(file.getAbsolutePath());     
        }
    }
    
    @FXML 
    private void addEmbed(ActionEvent event) {
        Stage stage = (Stage) html.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) { 
            String path = file.getAbsolutePath();
            //file.getName()
            embedAttach.add(path);
            html.setHtmlText(html.getHtmlText()+"<img src='cid:"+file.getName()+"'/>");
        }
    }
    
    public void setFileChooser(FileChooser fc) {
        this.fileChooser = fc;
    }

}
