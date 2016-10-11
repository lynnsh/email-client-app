package ashulzhenko.emailapp.interfaces;

import ashulzhenko.emailapp.bean.EmailCustom;
import java.sql.SQLException;
import java.util.List;

/**
 * The interface for MailStorageModule
 *
 * @author Alena Shulzhenko
 * @version 30/09/2016
 * @since 1.8
 */
public interface MailStorageDAO {
    
    /**
     * Deletes email that has the provided id.
     * @param id The id of the email to delete.
     * @return the number of deleted rows.
     * @throws SQLException If there was a problem when writing to the database.
     */
    int deleteEmail(int id) throws SQLException;


    /**
     * Returns emails saved in the database (from start index with the indicated
     * number of records).
     * @param start the starting index for retrieving emails.
     * @param number the number of emails to retrieve.
     * @return all email saved in the database.
     * @throws SQLException If there was a problem when reading from the database.
     */
    List<EmailCustom> findEmails(int start, int number) throws SQLException;
    
    /**
     * Returns the indicated number of emails saved in the database.
     * @param number the number of emails to retrieve.
     * @return all email saved in the database.
     * @throws SQLException If there was a problem when reading from the database.
     */
    List<EmailCustom> findEmails(int number) throws SQLException;
    
    /**
     * Returns all emails saved in the database.
     * @return all email saved in the database.
     * @throws SQLException If there was a problem when reading from the database.
     */
    List<EmailCustom> findAll() throws SQLException;

    /**
     * Returns all emails in the given directory.
     * @param directory The directory where requested email are stored.
     * @return all emails in the given directory.
     * @throws SQLException If there was a problem when reading from the database.
     */
    List<EmailCustom> findAllInDirectory(String directory) throws SQLException;
    
    
    /**
     * Find an email in database with given Id.
     * @param id The id of the email to find.
     * @return the found email corresponding to the given id.
     * @throws SQLException If there was a problem when reading from the database.
     */
    EmailCustom findEmailById(int id) throws SQLException;
    
    
    /**
     * Saves provided email in the database.
     * @param email The email to save in the database.
     * @return the id of the saved email
     * @throws SQLException If there was a problem when writing to the database.
     */
    int saveEmail(EmailCustom email) throws SQLException;

    /**
     * Updates the directory in the database for the provided email.
     * @param email The email containing new directory name.
     * @return the number of updated rows.
     * @throws SQLException If there was a problem when writing to the database.
     */
    int updateEmailDirectory(EmailCustom email) throws SQLException;

}
