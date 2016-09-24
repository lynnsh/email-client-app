package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import ashulzhenko.emailapp.bean.UserConfigBean;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FolderStorageModule class is used to create, rename, update, and delete directories.
 *
 * @author Alena Shulzhenko
 * @version 23/09/2016
 * @since 1.8
 */
public class FolderStorageModule extends DatabaseModule implements FolderStorageDAO {
    private final Logger log = LoggerFactory.getLogger(getClass().getName());
    
    /**
     * Instantiates the object with all necessary information to work with the database.
     *
     * @param userInfo user's information needed to connect to the database.
     */
    public FolderStorageModule(UserConfigBean userInfo) {
        super(userInfo);
    }

    /**
     * Creates new directory with the provided name.
     * 
     * @param name The name of the directory to create.
     * 
     * @return the id of the created directory.
     * 
     * @throws SQLException If there was a problem when writing to the database.
     */
    @Override
    public int createDirectory(String name) throws SQLException {
        if(name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Directory name value is invalid.");
        
        int id = -1;
        String query = "insert into directories (name) values (?)";
        try {
            Connection connection = getConnection();
            try(PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.executeUpdate();
                //get id of newly created directory
                try(ResultSet rs = pstmt.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }
            closeConnection(connection);
        }
        catch(MySQLIntegrityConstraintViolationException e) {
            log.error("Such directory already exists", e);
            throw new IllegalArgumentException("Such directory already exists: " + name);
        }
        return id;
    }

    /**
     * Deletes the requested directory.
     * 
     * @param name The name of the directory to delete.
     * 
     * @return 1 if operation was successful; 0 otherwise.
     * 
     * @throws SQLException If there was a problem when writing to the database.
     */
    @Override
    public int deleteDirectory(String name) throws SQLException {
        if(name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Directory name value is invalid.");

        int result;
        String query = "delete from directories where name = ?";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            result = 1;
        }

        closeConnection(connection);
        return result;
    }

    /**
     * Find all directory names in the database.
     * 
     * @return the list of directories in the database.
     * 
     * @throws SQLException If there was a problem when reading form the database.
     */
    @Override
    public List<String> findAll() throws SQLException {
        List<String> dirs = new ArrayList<>(0);
        String query = "select name from directories";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)){
            try(ResultSet rs = pstmt.executeQuery()){
                while(rs.next())
                    dirs.add(rs.getString(1));
            }
        }
        closeConnection(connection);
        return dirs;
    }

    /**
     * Updates directory's name.
     * 
     * @param oldName The old name of the directory.
     * @param newName The new name of the directory.
     * 
     * @return 1 if operation was successful; 0 otherwise.
     * 
     * @throws SQLException If there was a problem when reading form the database.
     */
    @Override
    public int updateDirectory(String oldName, String newName) throws SQLException {
        checkNames(oldName, newName);
        
        int result;
        String query = "update directories set name = ? where name = ?";
        try{
            Connection connection = getConnection();
            try(PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, oldName);
                pstmt.executeUpdate();
                result = 1;
            }
            closeConnection(connection);
        }
        catch(MySQLIntegrityConstraintViolationException e) {
            log.error("Such directory already exists", e);
            throw new IllegalArgumentException("Such directory already exists: " + newName);
        }
        return result;
    }
    
    /**
     * Verifies that provided names are not empty and not the same.
     * @param oldName The old name of the directory.
     * @param newName The new name of the directory.
     */
    private void checkNames(String oldName, String newName) {
        if(oldName == null || oldName.trim().isEmpty())
            throw new IllegalArgumentException("Old directory name value is invalid.");
        if(newName == null || newName.trim().isEmpty())
            throw new IllegalArgumentException("New directory name value is invalid.");
        oldName = oldName.trim();
        newName = newName.trim();
        if(oldName.equals(newName))
            throw new IllegalArgumentException("Both names are the same.");
    }
    
}
