package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.bean.UserConfigBean;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Flags;
import jodd.mail.EmailAddress;
import jodd.mail.EmailFilter;
import jodd.mail.ImapSslServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;

/**
 * IMAPModule class is used to receive emails.
 *
 * @author Alena Shulzhenko
 * @version 14/09/2016
 * @since 1.8
 */
public class IMAPModule {
    private UserConfigBean userInfo;
    
    /**
     * Instantiates the object with all necessary information to receive emails.
     * 
     * @param userInfo user's information needed to receive emails.
     */
    public IMAPModule(UserConfigBean userInfo) {
        if(userInfo == null)
            throw new IllegalArgumentException("UserConfigBean value is null.");
        
        String address = userInfo.getFromEmail();
        if (!new EmailAddress(address).isValid())
            throw new IllegalArgumentException ("Provided email address is invalid: " + address);
        
        this.userInfo = userInfo;
    }
    
    /**
     * Using the provided information, checks whether there are new email on the server.
     * 
     * @return Returns the list of new emails received from the server.
     */
    public List<EmailCustom> receiveEmail() {
        //create am IMAP server object
        ImapSslServer imapSslServer = new ImapSslServer(userInfo.getImapUrl(),
            userInfo.getImapPort(), userInfo.getFromEmail(), userInfo.getPasswordEmail());

        //imapSslServer.setProperty("mail.debug", "true");

        ReceiveMailSession session = imapSslServer.createSession();
        session.open();

        //messages that are delivered are then marked as read on the server
        ReceivedEmail[] rcvEmails = session.receiveEmailAndMarkSeen(EmailFilter
                .filter().flag(Flags.Flag.SEEN, false));
        
        session.close();
        
        List<EmailCustom> emails = new ArrayList<>(0);
        
        if(rcvEmails != null) {
            for(int i = 0; i < rcvEmails.length; i++) {
                emails.add(new EmailCustom(rcvEmails[i]));
            }
        }   
        return emails;
    }
}
