package ashulzhenko.emailapp.interfaces;

import ashulzhenko.emailapp.bean.EmailCustom;
import java.sql.SQLException;
import java.util.List;

/**
 * The interface for MailStorageModule
 *
 * @author Alena Shulzhenko
 * @version 23/09/2016
 * @since 1.8
 */
public interface MailStorageDAO {
    
    /**
     * Saves provided email in the database.
     * @param email The email to save in the database.
     * @return the id of the saved email
     * @throws SQLException If there was a problem when writing to the database.
     */
    int saveEmail(EmailCustom email) throws SQLException;

    /**
     * Find an email in database with given Id.
     * @param id The id of the email to find.
     * @return the found email corresponding to the given id.
     * @throws SQLException If there was a problem when reading from the database.
     */
    EmailCustom findEmailById(int id) throws SQLException;

    /**
     * Returns all email saved in the database.
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
     * Updates the directory in the database for the provided email.
     * @param email The email containing new directory name.
     * @return 1 if update was successful; 0 otherwise.
     * @throws SQLException If there was a problem when writing to the database.
     */
    int updateEmailDirectory(EmailCustom email) throws SQLException;

    /**
     * Deletes email that has the provided id.
     * @param id The id of the email to delete.
     * @return 1 if delete was successful; 0 otherwise.
     * @throws SQLException If there was a problem when writing to the database.
     */
    int deleteEmail(int id) throws SQLException;
}
