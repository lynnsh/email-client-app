package ashulzhenko.emailapp.interfaces;

import java.sql.SQLException;
import java.util.List;

/**
 * Under construction..
 * @author aline
 */
public interface FolderStorageDAO {
    //create
    int createDirectory(String name) throws SQLException;
    
    //read
    List<String> findAll() throws SQLException;
    
    //update
    int updateDirectory(String oldName, String newName) throws SQLException;
    
    //delete
    int deleteDirectory(String name) throws SQLException;
}
