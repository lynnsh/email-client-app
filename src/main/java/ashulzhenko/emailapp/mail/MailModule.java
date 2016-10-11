package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.interfaces.Mailer;
import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.mail.Flags;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailFilter;
import jodd.mail.ImapSslServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.mail.SmtpSslServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MailModule class is used to send and receive emails.
 *
 * @author Alena Shulzhenko
 * @version 04/10/2016
 * @since 1.8
 */
public class MailModule implements Mailer {

    private String[] embedCopy;
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    private UserConfigBean userInfo;

    /**
     * Instantiates the object with all necessary information to send and
     * receive emails.
     *
     * @param userInfo user's information needed to send the email.
     */
    public MailModule(UserConfigBean userInfo) {
        validateUserInfo(userInfo);
        this.userInfo = userInfo;
    }

    /**
     * Returns user information in UserConfigBean.
     *
     * @return user information in UserConfigBean.
     */
    @Override
    public UserConfigBean getUserInfo() {
        return userInfo;
    }
    
    
    /**
     * Using the provided information, checks whether there are new emails on the
     * server.
     *
     * @return Returns the list of new emails received from the server.
     */
    @Override
    public List<EmailCustom> receiveEmail() {
        validateUserInfo(userInfo);
        //create an IMAP server object
        ImapSslServer imapSslServer = new ImapSslServer(userInfo.getImapUrl(),
                userInfo.getImapPort(), userInfo.getFromEmail(), userInfo.getEmailPassword());
        
        //imapSslServer.setProperty("mail.debug", "true");
        ReceiveMailSession session = imapSslServer.createSession();
        session.open();
        
        //messages that are delivered are then marked as read on the server
        ReceivedEmail[] rcvEmails = session.receiveEmailAndMarkSeen
                            (EmailFilter.filter().flag(Flags.Flag.SEEN, false));
        
        session.close();
        
        List<EmailCustom> emails = new ArrayList<>(0);
        
        if (rcvEmails != null) {
            for (int i = 0; i < rcvEmails.length; i++) {
                emails.add(new EmailCustom(rcvEmails[i]));
            }
        }
        return emails;
    }

    /**
     * Sends the email provided.
     * Jodd's session object removes embedded attachments from the email object.
     * Since file paths are no longer stored, the received EmailCustom object will only
     * contain usual attachments.
     *
     * @param email The email to send.
     * 
     * @return sent email.
     */
    @Override
    public EmailCustom sendEmail(EmailCustom email) {
        if (email == null) {
            throw new IllegalArgumentException("Given email is null.");
        }

        validateUserInfo(userInfo);

        //create an SMTP server object
        SmtpServer<SmtpSslServer> smtpServer = SmtpSslServer
                .create(userInfo.getSmtpUrl(), userInfo.getSmtpPort())
                .authenticateWith(userInfo.getFromEmail(), userInfo.getEmailPassword());

        //display Java Mail debug conversation with the server
        //smtpServer.debug(true);
        
        SendMailSession session = smtpServer.createSession();

        //null pointer exception if file name is invalid
        try {
            session.open();
            session.sendMail(email);
            session.close();
        } catch (NullPointerException npe) {
            throw new IllegalArgumentException
                    ("Attachment error, such file does not exist: " + npe.getMessage());
        }
        
        return addEmbed(email);
    }

    /**
     * Using all the provided information, sends the email.
     * Both embedded and usual attachments are preserved with the returned email.
     *
     * @param to the array of addresses to receive the email.
     * @param cc the carbon copy array of addresses to receive the email.
     * @param bcc the blind carbon copy array of addresses to receive the email.
     * @param subject the subject of the email.
     * @param message the message of the email.
     * @param attach the array of attachments (file paths).
     * @param embedAttach the array of attachments to embed (file paths).
     *
     * @return the created and sent email.
     */
    @Override
    public EmailCustom sendEmail(String[] to, String[] cc, String[] bcc,
            String subject, String message, String[] attach, String[] embedAttach) {

        if (to == null || cc == null || bcc == null || subject == null || 
            message == null || attach == null || embedAttach == null) {
              throw new IllegalArgumentException("Null value passed to the sendEmail method.");
        }
        if (to.length == 0) {
            throw new IllegalArgumentException("No recipient email address provided.");
        }

        EmailAddress[] toEmails = setEmailArray(to);
        EmailAddress[] ccEmails = setEmailArray(cc);
        EmailAddress[] bccEmails = setEmailArray(bcc);

        //create message
        EmailCustom email = new EmailCustom();
        email.from(userInfo.getFromEmail()).to(toEmails).addHtml(message);

        if (!subject.isEmpty()) {
            email.subject(subject);
        }
        if (ccEmails.length != 0) {
            email.cc(ccEmails);
        }
        if (bccEmails.length != 0) {
            email.bcc(bccEmails);
        }

        //add attachments
        try {
            addAttachments(email, embedAttach, attach);
        } catch (Exception ex) {
            log.error("Error with the attachments", ex);
            throw new IllegalArgumentException("Attachment error: " + ex.getMessage());
        }

        return sendEmail(email);
    }
    

    /**
     * Adds embedded and ordinary attachments.
     *
     * @param email the email to which the attachments are added
     * @throws Exception If there is a problem when adding the attachment.
     */
    private void addAttachments(Email email, String[] embed, String[] attach) throws Exception {
        if (embed.length != 0) {
            for (String file : embed) {
                email.embed(EmailAttachment.attachment().bytes(new File(file)));
            }
            //copy of the embedded attachments array to add to the sent email
            //since session object removes it
            embedCopy = Arrays.copyOf(embed, embed.length);
        }
        if (attach.length != 0) {
            for (String file : attach) {
                email.attach(EmailAttachment.attachment().file(new File(file)));
            }
        }
    }
    
    /**
     * Adds embedded attachments if they were removed when sending the message.
     * 
     * @param email Email to add attachments.
     * 
     * @return email with added embedded attachments.
     */
    private EmailCustom addEmbed(EmailCustom email) {
        if(embedCopy != null) {
            for(String file : embedCopy)
                email.embed(EmailAttachment.attachment().bytes(new File(file)));
            //default to null for the next email
            embedCopy = null;
        }
        return email;
    }

    /**
     * Copies and fills the EmailAddress array that is used when sending the
     * email.
     *
     * @param original the provided string array of email addresses.
     * 
     * @return the EmailAddress array to use when sending the email.
     */
    private EmailAddress[] setEmailArray(String[] original) {
        EmailAddress[] toFill;
        if (original.length != 0) {
            toFill = new EmailAddress[original.length];
            for (int i = 0; i < original.length; i++) {
                EmailAddress ea = new EmailAddress(original[i]);
                if (!ea.isValid()) {
                    throw new IllegalArgumentException
                            ("Provided email address is invalid: " + original[i]);
                }
                toFill[i] = ea;
            }
        } 
        else {
            toFill = new EmailAddress[0];
        }

        return toFill;
    }

    /**
     * Validates user information.
     *
     * @param userInfo user information to validate.
     */
    private void validateUserInfo(UserConfigBean userInfo) {
        if (userInfo == null) {
            throw new IllegalArgumentException("User information value is null.");
        }

        String address = userInfo.getFromEmail();
        if (!new EmailAddress(address).isValid()) {
            throw new IllegalArgumentException("Provided email address is invalid: " + address);
        }
    }

}
