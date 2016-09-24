package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import ashulzhenko.emailapp.bean.UserConfigBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * FolderStorageModule class is used to create, rename, update, and delete directories.
 *
 * @author Alena Shulzhenko
 * @version 23/09/2016
 * @since 1.8
 */
public class FolderStorageModule extends DatabaseModule implements FolderStorageDAO {
    
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
     * @return id of the created directory.
     * 
     * @throws SQLException If there was a problem when writing to the database.
     */
    @Override
    public int createDirectory(String name) throws SQLException {
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Directory name value is invalid.");
        
        int id;
        String query = "insert into directories (name) values (?)";
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
        if(name == null || name.isEmpty())
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
     * @return the id of the directory if operation was successful.
     * 
     * @throws SQLException If there was a problem when reading form the database.
     */
    @Override
    public int updateDirectory(String oldName, String newName) throws SQLException {
        if(oldName == null || oldName.isEmpty())
            throw new IllegalArgumentException("Old directory name value is invalid.");
        if(newName == null || newName.isEmpty())
            throw new IllegalArgumentException("New directory name value is invalid.");
        
        int id;
        String query = "update directories set name = ? where name = ?";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, newName);
            pstmt.setString(1, oldName);
            pstmt.executeUpdate();
            //get id of updated directory
            try(ResultSet rs = pstmt.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        }
        
        closeConnection(connection);
        return id;
    }
    
}
