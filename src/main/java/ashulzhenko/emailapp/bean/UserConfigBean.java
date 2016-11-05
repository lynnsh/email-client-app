package ashulzhenko.emailapp.bean;

import java.io.Serializable;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * UserConfigBean class that encapsulates user information, such as email,
 * password, IMAP and SMTP information for this account.
 *
 * @author Alena Shulzhenko
 * @version 22/10/2016
 * @since 1.8
 */
public class UserConfigBean implements Serializable {
    
    private static final long serialVersionUID = 42051768871L;
    private StringProperty emailPassword;
    private StringProperty fromEmail;
    private StringProperty imapPort;
    private StringProperty imapUrl;
    private StringProperty mysqlDbName;
    private StringProperty mysqlPassword;
    private StringProperty mysqlPort;
    private StringProperty mysqlUrl;
    private StringProperty mysqlUser;
    private StringProperty smtpPort;
    private StringProperty smtpUrl;

    
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
        this.fromEmail = new SimpleStringProperty(fromEmail);
        this.emailPassword = new SimpleStringProperty(passwordEmail);
        this.imapPort = new SimpleStringProperty(imapPort+"");
        this.imapUrl = new SimpleStringProperty(imapUrl);
        this.smtpPort = new SimpleStringProperty(smtpPort+"");
        this.smtpUrl = new SimpleStringProperty(smtpUrl);
        this.mysqlPort = new SimpleStringProperty("3306");
        this.mysqlDbName = new SimpleStringProperty("");
        this.mysqlPassword = new SimpleStringProperty("");
        this.mysqlUrl = new SimpleStringProperty("");
        this.mysqlUser = new SimpleStringProperty("");
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
        if (!Objects.equals(this.imapPort.get(), other.imapPort.get())) {
            return false;
        }
        if (!Objects.equals(this.mysqlPort.get(), other.mysqlPort.get())) {
            return false;
        }
        if (!Objects.equals(this.smtpPort.get(), other.smtpPort.get())) {
            return false;
        }
        if (!Objects.equals(this.emailPassword.get(), other.emailPassword.get())) {
            return false;
        }
        if (!Objects.equals(this.fromEmail.get(), other.fromEmail.get())) {
            return false;
        }
        if (!Objects.equals(this.imapUrl.get(), other.imapUrl.get())) {
            return false;
        }
        if (!Objects.equals(this.mysqlPassword.get(), other.mysqlPassword.get())) {
            return false;
        }
        if (!Objects.equals(this.mysqlUrl.get(), other.mysqlUrl.get())) {
            return false;
        }
        if (!Objects.equals(this.mysqlUser.get(), other.mysqlUser.get())) {
            return false;
        }
        if (!Objects.equals(this.smtpUrl.get(), other.smtpUrl.get())) {
            return false;
        }
        
        return Objects.equals(this.mysqlDbName.get(), other.mysqlDbName.get());
    }
    
    
    /**
     * Returns the password for the user's email account.
     *
     * @return the password for the user's email account.
     */
    public String getEmailPassword() {
        return emailPassword.get();
    }
    
    /**
     * Returns user's email address.
     *
     * @return user's email address.
     */
    public String getFromEmail() {
        return fromEmail.get();
    }

    /**
     * Returns port number of the used IMAP server.
     *
     * @return port number of the used IMAP server.
     */
    public int getImapPort() {
        return Integer.parseInt(imapPort.get());
    }

    /**
     * Returns the URL of the IMAP server.
     *
     * @return the URL of the IMAP server.
     */
    public String getImapUrl() {
        return imapUrl.get();
    }
    
    /**
     * Returns the database name.
     *
     * @return the database name.
     */
    public String getMysqlDbName() {
        return mysqlDbName.get();
    }
    
    /**
     * Returns the password of MySQL user.
     *
     * @return the password of MySQL user.
     */
    public String getMysqlPassword() {
        return mysqlPassword.get();
    }
    
    /**
     * Returns the port number of the MySQL server.
     *
     * @return the port number of the MySQL server.
     */
    public int getMysqlPort() {
        return Integer.parseInt(mysqlPort.get());
    }
    
    /**
     * Returns the URL of the MySQL server.
     *
     * @return the URL of the MySQL server.
     */
    public String getMysqlUrl() {
        return mysqlUrl.get();
    }
    
    /**
     * Returns the username of MySQL user.
     *
     * @return the username of MySQL user.
     */
    public String getMysqlUserName() {
        return mysqlUser.get();
    }

    /**
     * Returns the port number of the SMTP server.
     *
     * @return the port number of the SMTP server.
     */
    public int getSmtpPort() {
        return Integer.parseInt(smtpPort.get());
    }

    /**
     * Returns the URL of the SMTP server.
     *
     * @return the URL of the SMTP server.
     */
    public String getSmtpUrl() {
        return smtpUrl.get();
    }
    
    /**
     * Returns the password for the user's email account as a property.
     *
     * @return the password for the user's email account as a property.
     */
    public StringProperty emailPassword() {
        return emailPassword;
    }
    
    /**
     * Returns user's email address as a property.
     *
     * @return user's email address as a property.
     */
    public StringProperty fromEmail() {
        return fromEmail;
    }
    
    /**
     * Returns port number of the used IMAP server as a property.
     *
     * @return port number of the used IMAP server as a property.
     */
    public StringProperty imapPort() {
        return imapPort;
    }
    
    /**
     * Returns the URL of the IMAP server as a property.
     *
     * @return the URL of the IMAP server as a property.
     */
    public StringProperty imapUrl() {
        return imapUrl;
    }
    
    /**
     * Returns the database name as a property.
     *
     * @return the database name as a property.
     */
    public StringProperty mysqlDbName() {
        return mysqlDbName;
    }
    
    /**
     * Returns the password of MySQL user as a property.
     *
     * @return the password of MySQL user as a property.
     */
    public StringProperty mysqlPassword() {
        return mysqlPassword;
    }
    
    /**
     * Returns the port number of the MySQL server as a property.
     *
     * @return the port number of the MySQL server as a property.
     */
    public StringProperty mysqlPort() {
        return mysqlPort;
    }
    
    /**
     * Returns the URL of the MySQL server as a property.
     *
     * @return the URL of the MySQL server as a property.
     */
    public StringProperty mysqlUrl() {
        return mysqlUrl;
    }
    
    /**
     * Returns the username of MySQL user as a property.
     *
     * @return the username of MySQL user as a property.
     */
    public StringProperty mysqlUser() {
        return mysqlUser;
    }
    
    /**
     * Returns the port number of the SMTP server as a property.
     *
     * @return the port number of the SMTP server as a property.
     */
    public StringProperty smtpPort() {
        return smtpPort;
    }
    
    /**
     * Returns the URL of the SMTP server as a property.
     *
     * @return the URL of the SMTP server as a property.
     */
    public StringProperty smtpUrl() {
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
        hash = 23 * hash + Objects.hashCode(this.emailPassword.get());
        hash = 23 * hash + Objects.hashCode(this.fromEmail.get());
        hash = 23 * hash + Objects.hashCode(this.imapPort.get());
        hash = 23 * hash + Objects.hashCode(this.imapUrl.get());
        hash = 23 * hash + Objects.hashCode(this.mysqlPassword.get());
        hash = 23 * hash + Objects.hashCode(this.mysqlPort.get());
        hash = 23 * hash + Objects.hashCode(this.mysqlUrl.get());
        hash = 23 * hash + Objects.hashCode(this.mysqlUser.get());
        hash = 23 * hash + Objects.hashCode(this.smtpPort.get());
        hash = 23 * hash + Objects.hashCode(this.smtpUrl.get());
        hash = 23 * hash + Objects.hashCode(this.mysqlDbName.get());
        return hash;
    }
    
    /**
     * Sets the password of the user's email account.
     *
     * @param emailPassword the password of the user's email account.
     */
    public void setEmailPassword(String emailPassword) {
        this.emailPassword.set(emailPassword);
    }
    
    /**
     * Sets user's email address.
     *
     * @param fromEmail user's email address to set.
     */
    public void setFromEmail(String fromEmail) {
        this.fromEmail.set(fromEmail);
    }

    /**
     * Sets the port number of the IMAP server.
     *
     * @param imapPort the port number of the IMAP server.
     */
    public void setImapPort(int imapPort) {
        this.imapPort.set(imapPort+"");
    }

    /**
     * Sets the URL of the IMAP server.
     *
     * @param imapUrl the URL of the IMAP server.
     */
    public void setImapUrl(String imapUrl) {
        this.imapUrl.set(imapUrl);
    }
    
    /**
     * Sets the database name.
     *
     * @param mysqlDbName the database name to set.
     */
    public void setMysqlDbName(String mysqlDbName) {
        this.mysqlDbName.set(mysqlDbName);
    }
    
    /**
     * Sets the password of MySQL user.
     *
     * @param mysqlPassword the password of MySQL user.
     */
    public void setMysqlPassword(String mysqlPassword) {
        this.mysqlPassword.set(mysqlPassword);
    }
    
    /**
     * Sets the port number of the MySQL server.
     *
     * @param mysqlPort the port number of the MySQL server.
     */
    public void setMysqlPort(int mysqlPort) {
        this.mysqlPort.set(mysqlPort+"");
    }
    
    /**
     * Sets the URL of the MySQL server.
     *
     * @param mysqlUrl the URL of the MySQL server.
     */
    public void setMysqlUrl(String mysqlUrl) {
        this.mysqlUrl.set(mysqlUrl);
    }
    
    /**
     * Sets the username of MySQL user.
     *
     * @param mysqlUser the username of MySQL user.
     */
    public void setMysqlUserName(String mysqlUser) {
        this.mysqlUser.set(mysqlUser);
    }

    /**
     * Sets the port number of the SMTP server.
     *
     * @param smtpPort the port number of the SMTP server.
     */
    public void setSmtpPort(int smtpPort) {
        this.smtpPort.set(smtpPort+"");
    }

    /**
     * Sets the URL of the SMTP server.
     *
     * @param smtpUrl the URL of the SMTP server.
     */
    public void setSmtpUrl(String smtpUrl) {
        this.smtpUrl.set(smtpUrl);
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
        return "UserConfigBean{" + "emailPassword=" + emailPassword.get() + 
                ", fromEmail=" + fromEmail.get() + ", imapPort=" + imapPort.get() + 
                ", imapUrl=" + imapUrl.get() + ", mysqlDbName=" + mysqlDbName.get() + 
                ", mysqlPassword=" + mysqlPassword.get() + ", mysqlPort=" + mysqlPort.get() + 
                ", mysqlUrl=" + mysqlUrl.get() + ", mysqlUser=" + mysqlUser.get() + 
                ", smtpPort=" + smtpPort.get() + ", smtpUrl=" + smtpUrl.get() + "}";
    }

}
