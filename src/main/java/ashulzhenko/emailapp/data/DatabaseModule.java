package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.bean.UserConfigBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds common logic for MailStorageModule and DirectoryStorageModule.
 * 
 * @author Alena Shulzhenko.
 * @version 23/09/2016
 * @since 1.8
 */
public abstract class DatabaseModule {
    private UserConfigBean userInfo;
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Instantiates the object with all necessary information to work with the database.
     *
     * @param userInfo user's information needed to connect to the database.
     */
    public DatabaseModule(UserConfigBean userInfo) {
        if (userInfo == null)
            throw new IllegalArgumentException("User information value is null.");
        
        this.userInfo = userInfo;
    }
    
    /**
     * Returns user information in UserConfigBean.
     *
     * @return user information in UserConfigBean.
     */
    public UserConfigBean getUserInfo() {
        return userInfo;
    }

    /**
     * Sets user information using UserConfigBean.
     *
     * @param userInfo user's information needed to send the email.
     */
    public void setUserInfo(UserConfigBean userInfo) {
        
        this.userInfo = userInfo;
    }
    
    /**
     * Creates a connection to the database.
     * 
     * @return Connection variable.
     * 
     * @throws SQLException If there is a problem when opening a connection.
     */
	protected Connection getConnection() throws SQLException {
        String url = userInfo.getMysqlUrl();
        int port = userInfo.getMysqlPort();
        String dbName = userInfo.getMysqlDbName();
        String user = userInfo.getMysqlUserName();
        String password = userInfo.getMysqlPassword();
        if(url == null || url.isEmpty() || port < 1 || dbName == null || dbName.isEmpty() ||
                user == null || user.isEmpty() || password == null)
            throw new IllegalArgumentException ("Invalid values provided. Cannot connect to teh database.");
        
		Connection connection = DriverManager.getConnection
            ("jdbc:mysql://"+url + ":"+port+"/"+dbName, user, password);
        
		log.info("Connected to the database.");  
        return connection;
	}
    
    /**
     * Closes the connection to the database.
     * 
     * @param connection Connection variable.
     * 
     * @throws SQLException If there is a problem when closing a connection.
     */
    protected void closeConnection(Connection connection) throws SQLException {
        if(connection != null)
            connection.close();
    }
}
