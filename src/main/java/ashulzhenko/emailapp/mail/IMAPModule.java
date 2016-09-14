package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.bean.UserConfigBean;
import javax.mail.Flags;
import jodd.mail.EmailFilter;
import jodd.mail.ImapSslServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;

/**
 * IMAPModule class is used to receive emails.
 *
 * @author Alena Shulzhenko
 * @version 11/09/2016
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
        this.userInfo = userInfo;
    }
    
    /**
     * Using the provided information, checks whether there are new email on the server.
     * 
     * @return Returns new emails received from the server.
     */
    public ReceivedEmail[] receiveEmail() {
        //create am IMAP server object
        ImapSslServer imapSslServer = new ImapSslServer(userInfo.getImapUrl(),
            userInfo.getImapPort(), userInfo.getFromEmail(), userInfo.getPasswordEmail());

        //imapSslServer.setProperty("mail.debug", "true");

        ReceiveMailSession session = imapSslServer.createSession();
        session.open();

        //messages that are delivered are then marked as read on the server
        ReceivedEmail[] emails = session.receiveEmailAndMarkSeen(EmailFilter
                .filter().flag(Flags.Flag.SEEN, false));
        
        session.close();
        
        return emails;
    }
}
