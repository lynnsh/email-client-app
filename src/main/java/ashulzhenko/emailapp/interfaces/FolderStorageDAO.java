package ashulzhenko.emailapp.interfaces;

import java.sql.SQLException;
import java.util.List;

/**
 * The interface for FolderStorageModule
 *
 * @author Alena Shulzhenko
 * @version 30/09/2016
 * @since 1.8
 */
public interface FolderStorageDAO {
    
    /**
     * Creates new directory with the provided name.
     * @param name The name of the directory to create.
     * @return the id of the created directory.
     * @throws SQLException If there was a problem when writing to the database.
     */
    int createDirectory(String name) throws SQLException;
    
    /**
     * Deletes the requested directory.
     * @param name The name of the directory to delete.
     * @return number of deleted directories.
     * @throws SQLException If there was a problem when writing to the database.
     */
    int deleteDirectory(String name) throws SQLException;
    
    /**
     * Find all directory names in the database.
     * @return the list of directories in the database.
     * @throws SQLException If there was a problem when reading form the database.
     */
    List<String> findAll() throws SQLException;
    
    /**
     * Updates directory's name.
     * @param oldName The old name of the directory.
     * @param newName The new name of the directory.
     * @return the number of updated directories.
     * @throws SQLException If there was a problem when reading form the database.
     */
    int updateDirectory(String oldName, String newName) throws SQLException;
    
}
