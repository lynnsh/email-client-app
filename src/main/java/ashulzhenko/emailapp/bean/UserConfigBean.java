package ashulzhenko.emailapp.bean;

import java.io.Serializable;

/**
 * UserConfigBean class that encapsulates user information, such as email,
 * password, IMAP and SMTP information for this account.
 *
 * @author Alena Shulzhenko
 * @version 16/09/2016
 * @since 1.8
 */
public class UserConfigBean implements Serializable {
    
    private static final long serialVersionUID = 42051768871L;
    private String fromEmail;
    private int imapPort;
    private String imapUrl;
    private String passwordEmail;
    private int smtpPort;
    private String smtpUrl;
    
    /**
     * Instantiates the object with default information.
     */
    public UserConfigBean() {
        super();
        this.fromEmail = "";
        this.passwordEmail = "";
        this.imapPort = 0;
        this.smtpPort = 0;
        this.imapUrl = "";
        this.smtpUrl = "";
    }

    /**
     * Instantiates the object when obtaining email, password, SMTP and IMAP
     * URLs and ports information.
     * Default ports for Gmail are imap-993, smtp-465.
     *
     * @param fromEmail User's email from where the messages will be send and retrieved.
     * @param passwordEmail The password of the provided email.
     * @param imapPort The port number for the IMAP server.
     * @param imapUrl The URL of the IMAP server.
     * @param smtpPort The port number for the SMTP server.
     * @param smtpUrl The URL of the SMTP server.
     */
    public UserConfigBean(String fromEmail, String passwordEmail, int imapPort,
                          String imapUrl, int smtpPort, String smtpUrl) {
        super();
        this.fromEmail = fromEmail;
        this.passwordEmail = passwordEmail;
        this.imapPort = imapPort;
        this.imapUrl = imapUrl;
        this.smtpPort = smtpPort;
        this.smtpUrl = smtpUrl;
    }
    
    /**
     * Returns user's email address.
     *
     * @return user's email address.
     */
    public String getFromEmail() {
        return fromEmail;
    }

    /**
     * Returns port number of the used IMAP server.
     *
     * @return port number of the used IMAP server.
     */
    public int getImapPort() {
        return imapPort;
    }

    /**
     * Returns the URL of the IMAP server.
     *
     * @return the URL of the IMAP server.
     */
    public String getImapUrl() {
        return imapUrl;
    }

    /**
     * Returns the password for the user's email account.
     *
     * @return the password for the user's email account.
     */
    public String getPasswordEmail() {
        return passwordEmail;
    }

    /**
     * Returns the port number of the SMTP server.
     *
     * @return the port number of the SMTP server.
     */
    public int getSmtpPort() {
        return smtpPort;
    }

    /**
     * Returns the URL of the SMTP server.
     *
     * @return the URL of the SMTP server.
     */
    public String getSmtpUrl() {
        return smtpUrl;
    }
    

    /**
     * Sets user's email address.
     *
     * @param fromEmail user's email address to set.
     */
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    /**
     * Sets the port number of the IMAP server.
     *
     * @param imapPort the port number of the IMAP server.
     */
    public void setImapPort(int imapPort) {
        this.imapPort = imapPort;
    }

    /**
     * Sets the URL of the IMAP server.
     *
     * @param imapUrl the URL of the IMAP server.
     */
    public void setImapUrl(String imapUrl) {
        this.imapUrl = imapUrl;
    }

    /**
     * Sets the password of the user's email account.
     *
     * @param passwordEmail the password of the user's email account.
     */
    public void setPasswordEmail(String passwordEmail) {
        this.passwordEmail = passwordEmail;
    }

    /**
     * Sets the port number of the SMTP server.
     *
     * @param smtpPort the port number of the SMTP server.
     */
    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    /**
     * Sets the URL of the SMTP server.
     *
     * @param smtpUrl the URL of the SMTP server.
     */
    public void setSmtpUrl(String smtpUrl) {
        this.smtpUrl = smtpUrl;
    }

    /**
     * The object itself is returned while represented as a string.
     *
     * @return user's email, password, IMAP port number, IMAP server URL, 
     *         SMTP port number, SMTP server URL in a String.
     */
    @Override
    public String toString() {
        return "UserConfigBean{" + "fromEmail=" + fromEmail
                + ", passwordEmail=" + passwordEmail + ", imapPort=" + imapPort
                + ", imapUrl=" + imapUrl + ", smtpPort=" + smtpPort
                + ", smtpUrl=" + smtpUrl + '}';
    }

}
