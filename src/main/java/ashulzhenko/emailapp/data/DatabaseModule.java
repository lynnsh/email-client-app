package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.bean.UserConfigBean;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common logic for MailStorageModule and DirectoryStorageModule.
 * 
 * @author Alena Shulzhenko.
 * @version 27/09/2016
 * @since 1.8
 */
public abstract class DatabaseModule {
    private UserConfigBean userInfo;
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Instantiates the object with all necessary information to work with the database.
     *
     * @param userInfo user's information needed to connect to the database.
     * @throws SQLException If there is a problem when connecting to the database.
     */
    public DatabaseModule(UserConfigBean userInfo) throws SQLException {
        if (userInfo == null)
            throw new IllegalArgumentException("User information value is null.");
        
        this.userInfo = userInfo;
        
        checkTables();
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
            throw new IllegalArgumentException ("Invalid values provided. Cannot connect to the database.");
        
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
    
    /**
     * Verifies whether the necessary tables exist in the database.
     * @throws SQLException If there is a problem when connecting to the database.
     */
    private void checkTables() throws SQLException {
        Connection connection = getConnection();
        
        String query = "select * from directories";    
        try(Statement stmt = connection.createStatement();) {
            stmt.executeQuery(query);
        }
        catch(MySQLSyntaxErrorException e){
            log.info("No tables found in the database. Creating new tables.");
            createTables(connection);
        }
        finally {
            closeConnection(connection);
        }
        
    }
    
    /**
     * Creates all necessary tables for email (emails, attachments, directories).
     * @param connection Database Connection object.
     */
    private void createTables(Connection connection) {
        final String seedDataScript = loadAsString("src/main/resources/createTables.sql");
        try {
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } 
        catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }
    
    /**
     * Determines whether given string is a comment.
     * @param line The line in the database config file to examine.
     * @return true if this line is a comment; false otherwise.
     */
    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//") || line.startsWith("/*");
    }
    
    /**
     * Loads database creation file and returns it as a String.
     * @param path The path to the database config file.
     * @return database config file contents as a String.
     */
    private String loadAsString(final String path) {
        try {
            return new String(readAllBytes(get(path)));
        } catch (IOException io) {
            throw new RuntimeException("Unable to close input stream.", io);
        }
    }
    
    /**
     * Splits database config file into list of SQL statements.
     * @param reader Reader object.
     * @param statementDelimiter Delimiter used in statements.
     * @return list of SQL statements.
     */
    private List<String> splitStatements(Reader reader, String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }
}
