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

/**
 * MailStorageModule class is used to save, find, update, and delete messages.
 *
 * @author Alena Shulzhenko
 * @version 30/09/2016
 * @since 1.8
 */
public class MailStorageModule extends DatabaseModule implements MailStorageDAO {
    
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
     * @return the number of deleted rows.
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
            result = pstmt.executeUpdate();
        }
        finally {
            closeConnection(connection);
        }
        return result;
    }

    /**
     * Returns emails saved in the database (from start index with the indicated
     * number of records).
     * 
     * @param start the starting index for retrieving emails (non-inclusive).
     * 
     * @param number the number of emails to retrieve.
     * 
     * @return all email saved in the database.
     * 
     * @throws SQLException If there was a problem when reading from the database.
     */
    @Override
    public List<EmailCustom> findEmails(int start, int number) throws SQLException {
        if(start < 0 || number < 0)
            throw new IllegalArgumentException ("Invalid arguments. start > 0 and number > 0.");
        
        Connection connection = getConnection();
        List<EmailCustom> emails = new ArrayList<>();
        
        String query = "select id, msgNumber, rcvDate, "
                + "(select name from directories where id = directory), "
                + "(select address from addresses where id = fromEmail), "
                + "message, sentDate, subject from emails order by id limit ?, ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, start);
            pstmt.setInt(2, number);
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()) {
                    EmailCustom email = createEmail(rs);
                    addEmails(email, connection);
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
     * Returns the indicated number of emails saved in the database.
     * 
     * @param number the number of emails to retrieve.
     * 
     * @return all email saved in the database.
     * 
     * @throws SQLException If there was a problem when reading from the database.
     */
    @Override
    public List<EmailCustom> findEmails(int number) throws SQLException {
        return findEmails(0, number);
    }
    
    /**
     * Returns all emails saved in the database.
     * 
     * @return all email saved in the database.
     * 
     * @throws SQLException If there was a problem when reading from the database.
     */
    @Override
    public List<EmailCustom> findAll() throws SQLException {
        return findEmails(0, Integer.MAX_VALUE);
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
            String query = "select id, msgNumber, rcvDate, "
                    + "(select name from directories where id = directory), "
                    + "(select address from addresses where id = fromEmail), "
                    + "message, sentDate, subject "
                    + "from emails where directory = ?";
            try(PreparedStatement pstmt = connection.prepareStatement(query)){
                pstmt.setInt(1, dirId);
                try(ResultSet rs = pstmt.executeQuery()){
                    while(rs.next()) {
                        EmailCustom email = createEmail(rs);
                        addAttachments(email, connection);
                        addEmails(email, connection);
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
        String query = "select id, msgNumber, rcvDate, "
                + "(select name from directories where id = directory), "
                + "(select address from addresses where id = fromEmail), "
                + "message, sentDate, subject "
                + "from emails where id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()) {
                    email = createEmail(rs);
                    addAttachments(email, connection);
                    addEmails(email, connection);
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
                + "fromEmail, message, sentDate, subject) "
                + "values (?,?,?,?,?,?,?)";
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
            saveAddresses(email, connection);
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
     * @return the number of updated rows.
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
            result = pstmt.executeUpdate();
        }
        finally {
            closeConnection(connection);
        }
        return result;
    }
    
    /**
     * Adds embedded attachments to an email.
     * @param email the email to which the attachments are added.
     * @param connection the Connection object to the the database.
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
     * Adds email addresses (bcc, cc, to, replyTo) to an email.
     * @param email the email to which the attachments are added.
     * @param connection the Connection object to the the database.
     * @throws SQLException If there is a problem when connecting to the database.
     */
    private void addEmails(EmailCustom email, Connection connection) throws SQLException {             
        String query = "select (select address from addresses where addressid = id) "
                        + "from email_address where emailid = ? "
                        + "and address_type = ?";
        AddressType[] addresses = AddressType.values();
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, email.getId());
            for(AddressType address : addresses) {
                pstmt.setInt(2, address.getType());
                try(ResultSet rs = pstmt.executeQuery()){
                    while(rs.next()) {
                        address.addToEmail(email, rs.getString(1));
                    }
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
        email.setDirectory(rs.getString(4));
        email.from(rs.getString(5));
        addMessages(email, rs.getString(6));
        email.setSentDate(rs.getTimestamp(7));
        email.subject(rs.getString(8));
        
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
        String from = (email.getFrom() == null? null : email.getFrom().getEmail()); 
        pstmt.setInt(4, saveAddress(from, connection));
        pstmt.setString(5, convertMessagesToStr(email.getAllMessages()));
        setDate(pstmt, email.getSentDate(), 6);
        pstmt.setString(7, email.getSubject());
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
     * Saves email addresses to the database.
     * @param email The email where the addresses come from.
     * @param connection Database Connection object.
     * @throws SQLException If there is a problem when writing to the database.
     */
    private void saveAddresses(EmailCustom email, Connection connection) throws SQLException {
        String query = "insert into email_address values (?,?,?)";
        AddressType[] addresses = AddressType.values();
        for(AddressType address : addresses) {
            MailAddress[] array = address.getList(email);
            if(array != null && array.length != 0) {
                try(PreparedStatement pstmt = connection.prepareStatement(query)){
                    for(MailAddress ma : array) {
                        pstmt.setInt(1, email.getId());
                        int addressid = saveAddress(ma.getEmail(), connection);
                        pstmt.setInt(2, addressid);
                        pstmt.setInt(3, address.getType()); 
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }
    
    private int saveAddress(String address, Connection conn) throws SQLException {
        int id;
        String query = "select id from addresses where address = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, address);
            try(ResultSet rs = pstmt.executeQuery()) {
                //address exists
                if(rs.next())
                    id = rs.getInt(1);
                //address does not exist, so it is created
                else {
                    query = "insert into addresses (address) values (?)";
                    try(PreparedStatement pstmt2 = conn.prepareStatement
                                            (query, Statement.RETURN_GENERATED_KEYS)) {
                        pstmt2.setString(1, address);
                        pstmt2.executeUpdate();
                        //get id of newly created address
                        try(ResultSet rs2 = pstmt2.getGeneratedKeys()) {
                            rs2.next();
                            id = rs2.getInt(1);
                        }
                    }
                }
            }
        }
        return id;
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
