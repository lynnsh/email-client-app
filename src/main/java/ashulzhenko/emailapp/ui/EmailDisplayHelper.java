package ashulzhenko.emailapp.ui;

import ashulzhenko.emailapp.bean.EmailCustom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.MailAddress;

/**
 * The helper class used to create a String object containing all
 * information about the email that needs to be displayed.
 * 
 * @author Alena Shulzhenko
 * @version 12/11/2016
 * @since 1.8
 */
public class EmailDisplayHelper {
    private ResourceBundle bundle;
    private EmailCustom currentEmail; 

    /**
     * Instantiates the object with the necessary information.
     * 
     * @param bundle the resources used to localize the root object.
     * @param currentEmail the email to display.
     */
    public EmailDisplayHelper(ResourceBundle bundle, EmailCustom currentEmail) {
        this.bundle = bundle;
        this.currentEmail = currentEmail;
    }
    
    /**
     * Returns all the information about user-selected email.
     * 
     * @return the information about user-selected email.
     */
    public String getEmailText() {
        StringBuilder email = new StringBuilder ("<b>");
        email.append("<body style='background-color: black; color: white;'/>")
             .append(bundle.getString("subject")).append(":</b> ")
             .append(currentEmail.getSubject()).append("<br/><b>")
             .append(bundle.getString("from")).append(":</b> ")
             .append(currentEmail.getFrom().getEmail()).append("<br/><b>")
             .append(bundle.getString("to")).append(":</b> ")
             .append(getEmails(currentEmail.getTo())).append("<br/>")
             .append(currentEmail.getCc().length != 0? "<b>CC:</b> " + 
                     getEmails(currentEmail.getCc()) + "<br/>" : "")
             .append(currentEmail.getBcc().length != 0? "<b>BCC:</b> " + 
                     getEmails(currentEmail.getBcc()) + "<br/>" : "")
             .append("<b>").append(bundle.getString("text")).append(":</b> ")
             .append(getMessages()).append("<br/><b>").append(bundle.getString("date"))
             .append(":</b> ").append(getDate());
                  
        email.trimToSize();
        return email.toString();
    }
    
     /**
     * Returns the full date of the currently selected email as a String.
     * 
     * @return the formatted date of the provided email.
     */
    private String getDate() {
        LocalDateTime date;
        //email was sent to the user
        if(currentEmail.getReceivedDate() == null)
            date = LocalDateTime.ofInstant(currentEmail.getSentDate().toInstant(), 
                    ZoneId.systemDefault());
        //email was received by the user
        else
            date = LocalDateTime.ofInstant(currentEmail.getReceivedDate().toInstant(), 
                    ZoneId.systemDefault());
        
           return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
    }
    
    /**
     * Returns the emails from the array formatted in the String,
     * separated with the semicolon.
     * 
     * @param array the array containing emails to put in the string.
     * 
     * @return the emails from the array formatted in a String.
     */
    public String getEmails(MailAddress[] array) {
        StringBuilder str = new StringBuilder("");
        for(MailAddress ma : array)
            str.append(ma.getEmail()).append("; ");
        
        if(array.length > 1)
            return str.substring(0, str.length()-2);
        str.trimToSize();
        return str.toString();
    }
    
    /**
     * Returns all messages belonging to the currently selected email.
     * 
     * @return all messages belonging to the selected email.
     */
    private String getMessages() {
        StringBuilder message = new StringBuilder("");
        List<EmailMessage> list = currentEmail.getAllMessages();
        for(EmailMessage em : list)
            message.append(em.getContent()).append("<br/>");
        return replaceImage(message);
    }
    
    /**
     * Makes the embedded image viewable in the HTMLEditor.
     * If an image was embedded in the previous message
     * (e.g. in forward or reply), this image will not be replaced.
     * 
     * @param str the original message with the embedded image(s).
     * 
     * @return the modified message that can show the embedded image(s). 
     */
    private String replaceImage(StringBuilder str) {
        str.trimToSize();
        String message = str.toString();
        List<EmailAttachment> attach = currentEmail.getAttachments();
        if(attach != null && attach.size() > 0) {
            for(EmailAttachment ea : attach) {
                String file = ea.getName();
                if(message.contains("cid:"+file)) {
                    message = message.replace("<img src=\"cid:"+file+"\">",
                        "<img src=\"data:image/"
                        +file.substring(file.lastIndexOf(".")+1)+";base64,"
                        +Base64.getMimeEncoder().encodeToString(ea.toByteArray())
                        +"\"/>");
                }
            }
                
        }
        return message;
    }
}
