package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import java.util.List;

/**
 * The interface for MailModule
 *
 * @author Alena Shulzhenko
 * @version 16/09/2016
 * @since 1.8
 */
public interface Mailer {
    /**
     * Using all the provided information, sends the email.
     * @param to the array of addresses to receive the email.
     * @param cc the carbon copy array of addresses to receive the email.
     * @param bcc the blind carbon copy array of addresses to receive the email.
     * @param subject the subject of the email.
     * @param message the message of the email.
     * @param attachments the array of attachments (file paths).
     * @param embedAttachments the array of attachments to embed (file paths).
     * @return the created and sent email.
     */
    EmailCustom sendEmail(String[] to, String[] cc, String[] bcc, String subject, 
                          String message, String[] attachments, String[] embedAttachments);
    
    
    /**
     * Using the provided information, checks whether there are new email on the server.
     * @return Returns the list of new emails received from the server.
     */
    List<EmailCustom> receiveEmail();
    
    
    /**
     * Sends the email provided.
     * @param email The email to send.
     * @return the email sent.
     */
    EmailCustom sendEmail(EmailCustom email);
    
    
    /**
     * Sets user information using UserConfigBean.
     * @param userInfo user's information needed to send the email.
     */
    void setUserInfo(UserConfigBean userInfo);
    
    
    /**
     * Returns user information in UserConfigBean.
     * @return user information in UserConfigBean.
     */
    UserConfigBean getUserInfo();
}
