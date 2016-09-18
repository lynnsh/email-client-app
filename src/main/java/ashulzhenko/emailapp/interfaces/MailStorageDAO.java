package ashulzhenko.emailapp.interfaces;

import ashulzhenko.emailapp.bean.EmailCustom;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author aline
 */
public interface MailStorageDAO {
    // Create
    int createEmail(EmailCustom email) throws SQLException;

    // Read
    EmailCustom findEmailById(int id) throws SQLException;

    List<EmailCustom> findAll() throws SQLException;

    List<EmailCustom> findAllInDirectory(String directory) throws SQLException;

    // Update
    int updateEmailDirectory(EmailCustom email) throws SQLException;

    // Delete
    int deleteEmail(int id) throws SQLException;
}
