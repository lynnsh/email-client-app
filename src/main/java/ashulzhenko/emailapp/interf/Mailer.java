package ashulzhenko.emailapp.interf;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import java.util.List;


public interface Mailer {
    /**
     * Using all the provided information, sends the email.
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
