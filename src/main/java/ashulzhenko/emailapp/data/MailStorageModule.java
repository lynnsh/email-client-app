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
 * Under construction..
 * @author aline
 */
public class MailStorageModule extends DatabaseModule implements MailStorageDAO {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public MailStorageModule(UserConfigBean userInfo) {
        super(userInfo);
    }

    @Override
    public int createEmail(EmailCustom email) throws SQLException {
        if(email == null)
            throw new IllegalArgumentException("Email value is null.");
        
        int result;
        String query = "insert into emails (msgNumber, rcvDate, directory, "
                + "bcc, cc, fromEmail, message, toEmails, replyTo, sentDate, "
                + "subject, attachments) values (?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, email.getMessageNumber());
            pstmt.setTimestamp(2, new Timestamp(email.getReceivedDate().getTime()));
            pstmt.setInt(3, findDirectoryId(connection, email.getDirectory()));
            pstmt.setString(4, convertArrayToStr(email.getBcc()));
            pstmt.setString(5, convertArrayToStr(email.getCc()));
            pstmt.setString(6, email.getFrom().getEmail());
            pstmt.setString(7, convertMessagesToStr(email.getAllMessages()));
            pstmt.setString(8, convertArrayToStr(email.getTo()));
            pstmt.setString(9, convertArrayToStr(email.getReplyTo()));
            pstmt.setTimestamp(10, new Timestamp(email.getSentDate().getTime()));
            pstmt.setString(11, email.getSubject());
            pstmt.setString(12, convertAttachToStr(email.getAttachments()));
            
            result = pstmt.executeUpdate();
        }
        
        closeConnection(connection);
        return result;
    }

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
        
        closeConnection(connection);
        return result;
    }

    @Override
    public List<EmailCustom> findAll() throws SQLException {
        Connection connection = getConnection();
        List<EmailCustom> emails = new ArrayList<>();
        
        String query = "select id, msgNumber, rcvDate, directory, bcc, cc, fromEmail, "
                + "message, toEmails, replyTo, sentDate, subject, attachments from emails";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()) {
                    EmailCustom email = createEmail(rs);
                    emails.add(email);
                }
            }
        }
        closeConnection(connection);
        return emails;
    }

    @Override
    public List<EmailCustom> findAllInDirectory(String directory) throws SQLException {
        if(directory == null || directory.isEmpty())
            throw new IllegalArgumentException("Directory value is null or empty.");
        
        Connection connection = getConnection();
        int dirId = findDirectoryId(connection, directory);
        List<EmailCustom> emails = new ArrayList<>();
        
        String query = "select id, msgNumber, rcvDate, directory, bcc, cc, fromEmail, "
                + "message, toEmails, replyTo, sentDate, subject, attachments "
                + "from emails where directory = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, dirId);
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next()) {
                    EmailCustom email = createEmail(rs);
                    emails.add(email);
                }
            }
        }
        closeConnection(connection);
        return emails;
    }

    @Override
    public EmailCustom findEmailById(int id) throws SQLException {
        if(id < 1)
            throw new IllegalArgumentException("Id value is invalid: " + id);
        
        Connection connection = getConnection();
        EmailCustom email;
        
        String query = "select id, msgNumber, rcvDate, directory, bcc, cc, fromEmail, "
                + "message, toEmails, replyTo, sentDate, subject, attachments "
                + "from emails where id = ?";
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setInt(1, id);
            try(ResultSet rs = pstmt.executeQuery()){
                rs.next();
                email = createEmail(rs);
            }
        }
        closeConnection(connection);
        return email;
    }
    
    
    @Override
    public int updateEmailDirectory(EmailCustom email) throws SQLException {
        if(email == null)
            throw new IllegalArgumentException("Email value is null.");
        
        int result;
        String query = "update emails set directory = ? where id = ?";
        Connection connection = getConnection();
        int dirId = findDirectoryId(connection, email.getDirectory());
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, dirId);
            pstmt.setInt(2, email.getId());
            result = pstmt.executeUpdate();
        }
        
        closeConnection(connection);
        return result;
    }
    
    /**
     * Adds embedded and ordinary attachments.
     *
     * @param email the email to which the attachments are added
     * @throws Exception If there is a problem when adding the attachment.
     */
    private void addAttachments(Email email, String[] array) throws Exception {
        for(String str : array) {
            email.embed(EmailAttachment.attachment().bytes(str.getBytes()));
        }
    }
    
    private String convertArrayToStr(MailAddress[] array) {
        String str = "";
        if(array != null && array.length != 0) {
            for(MailAddress ma : array)
                str += ma.getEmail() + ",";
        }
        return str;
    }
    
    private String convertAttachToStr(List<EmailAttachment> list) {
        String str = "";
        if(list != null && !list.isEmpty()) {
            for(EmailAttachment ea : list) {
                byte[] array = ea.toByteArray();
                String byteStr = new String(array);
                str += byteStr + ",";
            }
        }
        return str;
    }
    
    private String convertMessagesToStr(List<EmailMessage> list) {
        String str = "";
        if(list != null && !list.isEmpty()) {
            for(EmailMessage em : list)
                str += em.getContent() + ",";
        }
        return str;
    }
    
    private EmailCustom createEmail(ResultSet rs) throws SQLException {
        EmailCustom email = new EmailCustom();
        email.setId(rs.getInt(1));
        email.setMessageNumber(rs.getInt(2));
        email.setReceivedDate(rs.getTimestamp(3));
        email.setDirectory(findDirectoryName(rs.getInt(4)));
        String str = rs.getString(5);
        if(!str.isEmpty())
            email.bcc(str.split(","));
        str = rs.getString(6);
        if(!str.isEmpty())
            email.cc(str.split(","));
        email.from(rs.getString(7));
        email.addHtml(rs.getString(8));
        str = rs.getString(9);
        if(!str.isEmpty())
            email.to(str.split(","));
        str = rs.getString(10);
        if(!str.isEmpty())
            email.replyTo(str.split(","));
        email.setSentDate(rs.getTimestamp(11));
        email.subject(rs.getString(12));
        try {
            str = rs.getString(13);
            if(!str.isEmpty())
                addAttachments(email, str.split(","));
        } catch (Exception ex) {
            log.error("Error with the attachments", ex);
            throw new IllegalArgumentException("Attachment error: " + ex.getMessage());
        }
        return email;
    }
    
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
        closeConnection(conn);
        return name;
    }
    
    private int findDirectoryId(Connection conn, String directory) throws SQLException {
        int id;
        String query = "select id from directories where name = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, directory);
            try(ResultSet rs = pstmt.executeQuery()) {
                if(rs.next())
                    id = rs.getInt(1);
                else {
                    query = "insert into directories (name) values (?)";
                    try(PreparedStatement pstmt2 = conn.prepareStatement
                                    (query, Statement.RETURN_GENERATED_KEYS)){
                        pstmt2.setString(1, directory);
                        pstmt2.executeUpdate();
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
    
}
