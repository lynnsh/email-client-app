package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aline
 */
public class FolderStorageModule extends DatabaseModule implements FolderStorageDAO {
    
    public FolderStorageModule(UserConfigBean userInfo) {
        super(userInfo);
    }

    @Override
    public int createDirectory(String name) throws SQLException {
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Directory name value is invalid.");
        
        int result;
        String query = "insert into directories (name) values (?)";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            result = pstmt.executeUpdate();
        }
        
        closeConnection(connection);
        return result;
    }

    @Override
    public int deleteDirectory(String name) throws SQLException {
        if(name == null || name.isEmpty())
            throw new IllegalArgumentException("Directory name value is invalid.");
        
        int result;
        String query = "delete from directories where name = ?";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, name);
            result = pstmt.executeUpdate();
        }
        
        closeConnection(connection);
        return result;
    }

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

    @Override
    public int updateDirectory(String oldName, String newName) throws SQLException {
        if(oldName == null || oldName.isEmpty())
            throw new IllegalArgumentException("Old directory name value is invalid.");
        if(newName == null || newName.isEmpty())
            throw new IllegalArgumentException("New directory name value is invalid.");
        
        int result;
        String query = "update directories set name = ? where name = ?";
        Connection connection = getConnection();
        try(PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newName);
            pstmt.setString(1, oldName);
            result = pstmt.executeUpdate();
        }
        
        closeConnection(connection);
        return result;
    }
    
}
