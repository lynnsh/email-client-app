package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.bean.UserConfigBean;
import java.io.File;
import java.util.Arrays;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpServer;
import jodd.mail.SmtpSslServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SMTPModule class is used to send an email.
 *
 * @author Alena Shulzhenko
 * @version 14/09/2016
 * @since 1.8
 */
public class SMTPModule {
    private UserConfigBean userInfo;
    private EmailAddress[] toEmails;
    private EmailAddress[] ccEmails;
    private EmailAddress[] bccEmails;
    private String subject;
    private String message;
    private String[] embedAttach;
    private String[] attach;
    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    /**
     * Instantiates the object with all necessary information to send the email.
     * 
     * @param userInfo user's information needed to send the email.
     * @param to the array of addresses to receive the email.
     * @param cc the carbon copy array of addresses to receive the email.
     * @param bcc the blind carbon copy array of addresses to receive the email.
     * @param subject the subject of the email.
     * @param message the message of the email.
     * @param attachments the array of attachments (file paths).
     * @param embedAttachments the array of attachments to embed (file paths).
     * @throws IllegalArgumentException If email address is invalid or no recipient provided.
     */
    public SMTPModule (UserConfigBean userInfo, String[] to, String[] cc,
                       String[] bcc, String subject, String message, 
                       String[] attachments, String[] embedAttachments) {
        
        if(userInfo == null || to == null || cc == null || bcc == null ||
           subject == null || message == null || attachments == null || embedAttachments == null)
             throw new IllegalArgumentException ("Invalid value passed to the constructor.");
        if(to.length == 0)
            throw new IllegalArgumentException ("No recipient email address provided.");
        
        this.userInfo = userInfo;
        this.subject = subject;
        this.message = message;
        
        String address = userInfo.getFromEmail();
        if (!new EmailAddress(address).isValid())
            throw new IllegalArgumentException ("Provided email address is invalid: " + address);
        
        toEmails = setEmailArray(to, toEmails);
        ccEmails = setEmailArray(cc, ccEmails);
        bccEmails = setEmailArray(bcc, bccEmails);
        
        attach = Arrays.copyOf(attachments, attachments.length);
        embedAttach = Arrays.copyOf(embedAttachments, embedAttachments.length);
    }
    
    /**
     * Returns the array of attachments (file paths).
     * 
     * @return the array of attachments (file paths).
     */
    public String[] getAttachments() {
        return Arrays.copyOf(attach, attach.length);
    }
    
    /**
     * Returns the array of embedded attachments (file paths).
     * 
     * @return the array of embedded attachments (file paths).
     */
    public String[] getEmbedAttachments() {
        return Arrays.copyOf(embedAttach, embedAttach.length);
    }

    /**
     * Returns the email addresses to bcc when sending the email.
     * 
     * @return the email addresses to bcc when sending the email.
     */
    public EmailAddress[] getBccEmails() {
        return Arrays.copyOf(bccEmails, bccEmails.length);
    }

    /**
     * Returns the email addresses to cc when sending the email.
     * 
     * @return the email addresses to cc when sending the email.
     */
    public EmailAddress[] getCcEmails() {
        return Arrays.copyOf(ccEmails, ccEmails.length);
    }

    /**
     * Returns the message of the email.
     * 
     * @return the message of the email.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the subject of the email.
     * 
     * @return the subject of the email.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns the recipient email addresses.
     * 
     * @return the recipient email addresses.
     */
    public EmailAddress[] getToEmails() {
        return Arrays.copyOf(toEmails, toEmails.length);
    }
    
    /**
     * Using all the provided information, sends the email.
     * 
     * @return the created and sent email.
     */
    public EmailCustom sendEmail() {
        //create am SMTP server object
        SmtpServer<SmtpSslServer> smtpServer = SmtpSslServer
                .create(userInfo.getSmtpUrl(), userInfo.getSmtpPort())
                .authenticateWith(userInfo.getFromEmail(), userInfo.getPasswordEmail());

        //display Java Mail debug conversation with the server
        //smtpServer.debug(true);

        //create text message
        EmailCustom email = new EmailCustom();
        email.from(userInfo.getFromEmail()).to(toEmails).addHtml(message);
        
        if(!subject.isEmpty())
            email.subject(subject);
        if(ccEmails.length != 0)
            email.cc(ccEmails);
        if(bccEmails.length != 0)
            email.bcc(bccEmails);
        
        //add attachments
        try {
            addAttachments(email);
        } catch (Exception ex) {
            log.error("Error with the attachments", ex);
            throw new IllegalArgumentException("Attachment error: " + ex.getMessage());
        }

        SendMailSession session = smtpServer.createSession();

        //null pointer exception if file name is invalid
        try {
            session.open();
            session.sendMail(email);
            session.close();
        }
        catch(NullPointerException npe) {
            throw new IllegalArgumentException
                    ("Attachment error, such file does not exist. " + npe.getMessage());
        }
        return email;
    }
    
    /**
     * Adds embedded and ordinary attachments.
     * 
     * @param email the email to which the attachments are added
     * @throws Exception If the is a problem when adding the attachment.
     */
    private void addAttachments(Email email) throws Exception {
        if(embedAttach.length != 0) {
            for(String file : embedAttach)
                email.embed(EmailAttachment.attachment().bytes(new File(file)));
        }
        
        if(attach.length != 0) {
            for(String file : attach) 
                email.attach(EmailAttachment.attachment().file(new File(file)));
        }
    }
    
    /**
     * Copies and fills the EmailAddress array that is used when sending the email.
     * 
     * @param original the provided string array of email addresses.
     * @param toFill the EmailAddress array to use when sending the email.
     * @return the EmailAddress array to use when sending the email.
     */
    private EmailAddress[] setEmailArray (String[] original, EmailAddress[] toFill) {
        if(original.length != 0) {
            toFill = new EmailAddress [original.length];
            for(int i = 0; i < original.length; i++) {
                EmailAddress ea = new EmailAddress(original[i]);
                if (!ea.isValid())
                    throw new IllegalArgumentException
                        ("Provided email address is invalid: " + original[i]);
                toFill[i] = ea;
            }
        }
        else
            toFill = new EmailAddress[0];
        
        return toFill;
    }
   
}
