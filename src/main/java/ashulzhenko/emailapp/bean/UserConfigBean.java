package ashulzhenko.emailapp.bean;

import java.io.Serializable;
import java.util.Objects;

/**
 * UserConfigBean class that encapsulates user information, such as email,
 * password, IMAP and SMTP information for this account.
 *
 * @author Alena Shulzhenko
 * @version 30/09/2016
 * @since 1.8
 */
public class UserConfigBean implements Serializable {
    
    private static final long serialVersionUID = 42051768871L;
    private String emailPassword;
    private String fromEmail;
    private int imapPort;
    private String imapUrl;
    private String mysqlDbName;
    private String mysqlPassword;
    private int mysqlPort;
    private String mysqlUrl;
    private String mysqlUser;
    private int smtpPort;
    private String smtpUrl;

    
    /**
     * Instantiates the object with default information.
     * Gmail information is used as a default.
     */
    public UserConfigBean() {
        this("", "", 993, "imap.gmail.com", 465, "smtp.gmail.com");
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
        this.mysqlPort = 3306;
        this.mysqlDbName = "";
        this.mysqlPassword = "";
        this.mysqlUrl = "";
        this.mysqlUser = "";
    }
    
    
    /**
     * Compares this UserConfigBean to the specified object.
     * The result is true if the two objects are of the same class,
     * and their field values are the same.
     *
     * @param obj The object to compare this against.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserConfigBean other = (UserConfigBean) obj;
        if (this.imapPort != other.imapPort) {
            return false;
        }
        if (this.mysqlPort != other.mysqlPort) {
            return false;
        }
        if (this.smtpPort != other.smtpPort) {
            return false;
        }
        if (!Objects.equals(this.emailPassword, other.emailPassword)) {
            return false;
        }
        if (!Objects.equals(this.fromEmail, other.fromEmail)) {
            return false;
        }
        if (!Objects.equals(this.imapUrl, other.imapUrl)) {
            return false;
        }
        if (!Objects.equals(this.mysqlPassword, other.mysqlPassword)) {
            return false;
        }
        if (!Objects.equals(this.mysqlUrl, other.mysqlUrl)) {
            return false;
        }
        if (!Objects.equals(this.mysqlUser, other.mysqlUser)) {
            return false;
        }
        if (!Objects.equals(this.smtpUrl, other.smtpUrl)) {
            return false;
        }
        
        return Objects.equals(this.mysqlDbName, other.mysqlDbName);
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
     * Returns the database name.
     *
     * @return the database name.
     */
    public String getMysqlDbName() {
        return mysqlDbName;
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
     * Returns a hash code value for the UserConfigBean object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.emailPassword);
        hash = 23 * hash + Objects.hashCode(this.fromEmail);
        hash = 23 * hash + this.imapPort;
        hash = 23 * hash + Objects.hashCode(this.imapUrl);
        hash = 23 * hash + Objects.hashCode(this.mysqlPassword);
        hash = 23 * hash + this.mysqlPort;
        hash = 23 * hash + Objects.hashCode(this.mysqlUrl);
        hash = 23 * hash + Objects.hashCode(this.mysqlUser);
        hash = 23 * hash + this.smtpPort;
        hash = 23 * hash + Objects.hashCode(this.smtpUrl);
        hash = 23 * hash + Objects.hashCode(this.mysqlDbName);
        return hash;
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
     * Sets the database name.
     *
     * @param mysqlDbName the database name to set.
     */
    public void setMysqlDbName(String mysqlDbName) {
        this.mysqlDbName = mysqlDbName;
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
     *         MySQL user name, MySQL password, and MySQL database name in a String.
     */
    @Override
    public String toString() {
        return "UserConfigBean{" + "emailPassword=" + emailPassword + 
                ", fromEmail=" + fromEmail + ", imapPort=" + imapPort + 
                ", imapUrl=" + imapUrl + ", mysqlDbName=" + mysqlDbName + 
                ", mysqlPassword=" + mysqlPassword + ", mysqlPort=" + mysqlPort + 
                ", mysqlUrl=" + mysqlUrl + ", mysqlUser=" + mysqlUser + 
                ", smtpPort=" + smtpPort + ", smtpUrl=" + smtpUrl + '}';
    }

}
