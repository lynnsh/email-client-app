package ashulzhenko.emailapp.bean;

import java.io.Serializable;

/**
 * UserConfigBean class that encapsulates user information, such as email,
 * password, IMAP and SMTP information for this account.
 *
 * @author Alena Shulzhenko
 * @version 27/09/2016
 * @since 1.8
 */
public class UserConfigBean implements Serializable {
    
    private static final long serialVersionUID = 42051768871L;
    private String emailPassword;
    private String fromEmail;
    private int imapPort;
    private String imapUrl;
    private String mysqlPassword;
    private int mysqlPort;
    private String mysqlUrl;
    private String mysqlUser;
    private int smtpPort;
    private String smtpUrl;
    private String mysqlDbName;

    
    
    /**
     * Instantiates the object with default information.
     * Gmail information is used as default.
     */
    public UserConfigBean() {
        super();
        this.fromEmail = "";
        this.emailPassword = "";
        this.imapPort = 993;
        this.smtpPort = 465;
        this.imapUrl = "imap.gmail.com";
        this.smtpUrl = "smtp.gmail.com";
        this.mysqlUrl = "";
        this.mysqlPort = 3306;
        this.mysqlUser = "";
        this.mysqlPassword = "";
    }

    /**
     * Instantiates the object when obtaining email, password, SMTP and IMAP
     * URLs and ports information.
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
        this.emailPassword = passwordEmail;
        this.imapPort = imapPort;
        this.imapUrl = imapUrl;
        this.smtpPort = smtpPort;
        this.smtpUrl = smtpUrl;
    }
    /**
     * Returns the database name.
     *
     * @return the database name.
     */
    public String getMysqlDbName() {
        return mysqlDbName;
    }
    /**
     * Sets the database name.
     *
     * @param mysqlDbName the database name to set.
     */
    public void setMysqlDbName(String mysqlDbName) {
        this.mysqlDbName = mysqlDbName;
    }
    /**
     * Returns the password for the user's email account.
     *
     * @return the password for the user's email account.
     */
    public String getEmailPassword() {
        return emailPassword;
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
     * Returns the password of MySQL user.
     *
     * @return the password of MySQL user.
     */
    public String getMysqlPassword() {
        return mysqlPassword;
    }
    /**
     * Returns the port number of the MySQL server.
     *
     * @return the port number of the MySQL server.
     */
    public int getMysqlPort() {
        return mysqlPort;
    }
    /**
     * Returns the URL of the MySQL server.
     *
     * @return the URL of the MySQL server.
     */
    public String getMysqlUrl() {
        return mysqlUrl;
    }
    /**
     * Returns the username of MySQL user.
     *
     * @return the username of MySQL user.
     */
    public String getMysqlUserName() {
        return mysqlUser;
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
     * Sets the password of the user's email account.
     *
     * @param emailPassword the password of the user's email account.
     */
    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
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
     * Sets the password of MySQL user.
     *
     * @param mysqlPassword the password of MySQL user.
     */
    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword = mysqlPassword;
    }
    /**
     * Sets the port number of the MySQL server.
     *
     * @param mysqlPort the port number of the MySQL server.
     */
    public void setMysqlPort(int mysqlPort) {
        this.mysqlPort = mysqlPort;
    }
    /**
     * Sets the URL of the MySQL server.
     *
     * @param mysqlUrl the URL of the MySQL server.
     */
    public void setMysqlUrl(String mysqlUrl) {
        this.mysqlUrl = mysqlUrl;
    }
    /**
     * Sets the username of MySQL user.
     *
     * @param mysqlUser the username of MySQL user.
     */
    public void setMysqlUserName(String mysqlUser) {
        this.mysqlUser = mysqlUser;
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
     *         SMTP port number, SMTP server URL, MySQL URL, MySQL port number,
     *         MySQL user name, MySQL password in a String.
     */
    @Override
    public String toString() {
        return "UserConfigBean{" + "fromEmail=" + fromEmail + 
                ", imapPort=" + imapPort + ", imapUrl=" + imapUrl + 
                ", emailPassword=" + emailPassword + ", smtpPort=" + smtpPort + 
                ", smtpUrl=" + smtpUrl + ", mysqlUrl=" + mysqlUrl + 
                ", mysqlPort=" + mysqlPort + ", mysqlUser=" + mysqlUser + 
                ", mysqlPassword=" + mysqlPassword + '}';
    }

    
    

}
