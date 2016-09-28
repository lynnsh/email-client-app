package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.MailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MailStorageModule class is used to save, find, update, and delete messages.
 *
 * @author Alena Shulzhenko
 * @version 27/09/2016
 * @since 1.8
 */
public class MailStorageModule extends DatabaseModule implements MailStorageDAO {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Instantiates the object with all necessary information to work with the database.
     *
     * @param userInfo user's information needed to connect to the database.
     * 
     * @throws SQLException If there is a problem when connecting to the database.
     */
    public MailStorageModule(UserConfigBean userInfo) throws SQLException {
        super(userInfo);
    }


    /**
     * Deletes email that has the provided id.
     * 
     * @param id The id of the email to delete.
     * 
     * @return 1 if delete was successful; 0 otherwise.
     * 
     * @throws SQLException If there was a problem when writing to the database.
     */
    @Override
    public int deleteEmail(int id) throws SQLException {
        if(id < 1)
            throw new IllegalArgumentException("Id value is invalid: " + id);
        
        int result;
        String query = "delete from emails where id = ?";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            result = 1;
        }
        finally {
            closeConnection(connection);
        }
        return result;
    }

    /**
     * Returns all email saved in the database.
     * 
     * @return all email saved in the database.
     * 
     * @throws SQLException If there was a problem when reading from the database.
     */
    @Override
    public List<EmailCustom> findAll() throws SQLException {
        Connection connection = getConnection();
        List<EmailCustom> emails = new ArrayList<>();
        
        String query = "select id, msgNumber, rcvDate, directory, bcc, cc, fromEmail, "
                + "message, toEmails, replyTo, sentDate, subject from emails";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()) {
                    EmailCustom email = createEmail(rs);
                    addAttachments(email, connection);
                    emails.add(email);
                }
            }
        }
        finally {
            closeConnection(connection);
        }
        return emails;
    }

    /**
     * Returns all emails in the given directory.
     * 
     * @param directory The directory where requested email are stored.
     * 
     * @return all emails in the given directory.
     * 
     * @throws SQLException If there was a problem when reading from the database.
     */
    @Override
    public List<EmailCustom> findAllInDirectory(String directory) throws SQLException {
        if(directory == null || directory.trim().isEmpty())
            throw new IllegalArgumentException("Directory value is null or empty.");
        
        Connection connection = getConnection();
        int dirId = findDirectoryId(connection, directory, false);
        List<EmailCustom> emails = new ArrayList<>(0);
        if(dirId != -1) {
            String query = "select id, msgNumber, rcvDate, directory, bcc, cc, fromEmail, "
                    + "message, toEmails, replyTo, sentDate, subject "
                    + "from emails where directory = ?";
            try(PreparedStatement pstmt = connection.prepareStatement(query)){
                pstmt.setInt(1, dirId);
                try(ResultSet rs = pstmt.executeQuery()){
                    while(rs.next()) {
                        EmailCustom email = createEmail(rs);
                        addAttachments(email, connection);
                        emails.add(email);
                    }
                }
            }
        }
        closeConnection(connection);
        return emails;
    }

    /**
     * Find an email in database with given Id.
     * 
     * @param id The id of the email to find.
     * 
     * @return the found email corresponding to the given id.
     * 
     * @throws SQLException If there was a problem when reading from the database.
     */
    @Override
    public EmailCustom findEmailById(int id) throws SQLException {
        if(id < 1)
            throw new IllegalArgumentException("Id value is invalid: " + id);
        
        Connection connection = getConnection();
        EmailCustom email  = null;
        String query = "select id, msgNumber, rcvDate, directory, bcc, cc, fromEmail, "
                + "message, toEmails, replyTo, sentDate, subject "
                + "from emails where id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) {
                    email = createEmail(rs);
                    addAttachments(email, connection);
                }
            }
        }
        finally {
            closeConnection(connection);
        }
        return email;
    }
    
    /**
     * Saves provided email in the database.
     *
     * @param email The email to save in the database.
     *
     * @return the id of the saved email
     *
     * @throws SQLException If there was a problem when writing to the database.
     */
    @Override
    public int saveEmail(EmailCustom email) throws SQLException {
        if(email == null)
            throw new IllegalArgumentException("Email value is null.");
        
        int id;
        String query = "insert into emails (msgNumber, rcvDate, directory, "
                + "bcc, cc, fromEmail, message, toEmails, replyTo, sentDate, "
                + "subject) values (?,?,?,?,?,?,?,?,?,?,?)";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            prepareEmail(pstmt, email, connection);
            pstmt.executeUpdate();
            //get id of newly created email
            try(ResultSet rs = pstmt.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
            email.setId(id);
            saveAttachments(email, connection);
        }
        finally {
            closeConnection(connection);
        }
        return id;
    }
    
    /**
     * Updates the directory in the database for the provided email.
     * 
     * @param email The email containing new directory name.
     * 
     * @return 1 if update was successful; 0 otherwise.
     * 
     * @throws SQLException If there was a problem when writing to the database.
     */
    @Override
    public int updateEmailDirectory(EmailCustom email) throws SQLException {
        if(email == null)
            throw new IllegalArgumentException("Email value is null.");
        
        int result;
        String query = "update emails set directory = ? where id = ?";
        Connection connection = getConnection();
        int dirId = findDirectoryId(connection, email.getDirectory(), true);
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, dirId);
            pstmt.setInt(2, email.getId());
            pstmt.executeUpdate();
            result = 1;
        }
        finally {
            closeConnection(connection);
        }
        return result;
    }
    
    /**
     * Adds embedded attachments to an email.
     * @param email the email to which the attachments are added.
     * @throws SQLException If there is a problem when connecting to the database.
     */
    private void addAttachments(EmailCustom email, Connection connection) throws SQLException {             
        String query = "select binarydata, filename from attachments where email = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, email.getId());
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()) {
                    byte[] attach = rs.getBytes(1);
                    String name = rs.getString(2);
                    email.embed(EmailAttachment.attachment().bytes(attach).setName(name));
                }
            }
        }  
    }
    
    /**
     * Adds messages to email.
     * @param email The email to which the messages are added
     * @param messages The messages to add to email.
     */
    private void addMessages(Email email, String messages) {
        for(String str : messages.split(";")) {
            email.addHtml(str);
        }
    }
    
    /**
     * Returns string with mail addresses separated by semicolon.
     * @param array MailAddress array to convert.
     * @return string with mail addresses separated by semicolon.
     */
    private String convertArrayToStr(MailAddress[] array) {
        String str = "";
        if(array != null && array.length != 0) {
            for(MailAddress ma : array)
                str += ma.getEmail() + ";";
        }
        return str;
    }

    
    /**
     * Returns string with messages separated by semicolon.
     * @param list EmailMessage list to convert.
     * @return string with messages separated by semicolon.
     */
    private String convertMessagesToStr(List<EmailMessage> list) {
        String str = "";
        if(list != null && !list.isEmpty()) {
            str = list.stream().map(x -> x.getContent() + ";").reduce(str, String::concat);
        }
        return str;
    }
    
    /**
     * Creates email from database data.
     * @param rs ResultSet containing database data.
     * @return created email.
     * @throws SQLException If there was a problem when reading from the database.
     */
    private EmailCustom createEmail(ResultSet rs) throws SQLException {
        EmailCustom email = new EmailCustom();
        email.setId(rs.getInt(1));
        email.setMessageNumber(rs.getInt(2));
        email.setReceivedDate(rs.getTimestamp(3));
        email.setDirectory(findDirectoryName(rs.getInt(4)));
        String str = rs.getString(5);
        if(!str.isEmpty())
            email.bcc(str.split(";"));
        str = rs.getString(6);
        if(!str.isEmpty())
            email.cc(str.split(";"));
        email.from(rs.getString(7));
        addMessages(email, rs.getString(8));
        str = rs.getString(9);
        if(!str.isEmpty())
            email.to(str.split(";"));
        str = rs.getString(10);
        if(!str.isEmpty())
            email.replyTo(str.split(";"));
        email.setSentDate(rs.getTimestamp(11));
        email.subject(rs.getString(12));
        
        return email;
    }
    
    
    /**
     * Returns the id of the provided directory. Creates new directory if one
     * is not found in the database.
     * @param conn Database Connection object.
     * @param directory Directory name for which id is required.
     * @param addNew Indicates whether the directory needs to be created if one does not exist.
     * @return the id of the provided directory; -1 if directory is not set and id is not found.
     * @throws SQLException If there was a problem when reading from the database.
     */
    private int findDirectoryId(Connection conn, String directory, boolean addNew) throws SQLException {
        int id = -1;
        String query = "select id from directories where name = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, directory);
            try(ResultSet rs = pstmt.executeQuery()) {
                //directory exists
                if(rs.next())
                    id = rs.getInt(1);
                //directory does not exist, so it is created
                else if(addNew) {
                    FolderStorageModule fs = new FolderStorageModule(getUserInfo());
                    id = fs.createDirectory(directory);
                }
            }
        }
        return id;
    }
    
    /**
     * Returns directory name corresponding to the provided id.
     * @param dirId The id of the directory to find.
     * @return directory name corresponding to the provided id.
     * @throws SQLException If there was a problem when reading from the database.
     */
    private String findDirectoryName(int dirId) throws SQLException {
        Connection conn = getConnection();
        String name;
        String query = "select name from directories where id = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, dirId);
            try(ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                name = rs.getString(1);
            }
        }
        finally{
            closeConnection(conn);
        }
        return name;
    }
    
    /**
     * Sets all necessary data to PreparedStatement.
     * @param pstmt The PreparedStatement.
     * @param email The email that provided data for the PreparedStatement.
     * @param connection Database Connection object.
     * @return PreparedStatement with all necessary data to execute it.
     * @throws SQLException If there is a problem when connecting to the database.
     */
    private PreparedStatement prepareEmail(PreparedStatement pstmt, EmailCustom email, 
                                           Connection connection) throws SQLException {
        pstmt.setInt(1, email.getMessageNumber());
        setDate(pstmt, email.getReceivedDate(), 2);
        pstmt.setInt(3, findDirectoryId(connection, email.getDirectory(), true));
        pstmt.setString(4, convertArrayToStr(email.getBcc()));
        pstmt.setString(5, convertArrayToStr(email.getCc()));
        String from = (email.getFrom() == null? null : email.getFrom().getEmail()); 
        pstmt.setString(6, from);
        pstmt.setString(7, convertMessagesToStr(email.getAllMessages()));
        pstmt.setString(8, convertArrayToStr(email.getTo()));
        pstmt.setString(9, convertArrayToStr(email.getReplyTo()));
        setDate(pstmt, email.getSentDate(), 10);
        pstmt.setString(11, email.getSubject());
        return pstmt;
    }
    
    /**
     * Saves email attachments to the database.
     * @param email The email where the attachments come from.
     * @param connection Database Connection object.
     * @throws SQLException If there is a problem when writing to the database.
     */
    private void saveAttachments(EmailCustom email, Connection connection) throws SQLException {
        String query = "insert into attachments (binarydata, filename, email) values (?,?,?)";
        List<EmailAttachment> list = email.getAttachments();
        if(list != null && !list.isEmpty()) {
            try(PreparedStatement pstmt = connection.prepareStatement(query)){
                for(EmailAttachment ea : list) {
                    pstmt.setBytes(1, ea.toByteArray());
                    pstmt.setString(2, ea.getName());
                    pstmt.setInt(3, email.getId());
                    pstmt.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Add date to the PreparedStatement.
     * @param pstmt PreparedStatement for which the date is set.
     * @param date The date to set.
     * @param index The index of this date in sql query.
     * @throws SQLException If there is a problem when connecting to the database.
     */
    private void setDate(PreparedStatement pstmt, java.util.Date date, int index) throws SQLException {
        if(date != null)
            pstmt.setTimestamp(index, new Timestamp(date.getTime()));
        else
            pstmt.setTimestamp(index, null);
    }
    
}
