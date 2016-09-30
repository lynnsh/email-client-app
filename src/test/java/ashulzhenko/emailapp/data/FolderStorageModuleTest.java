package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.interfaces.FolderStorageDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests FolderStorageModule.
 * @author Alena Shulzhenko
 */
public class FolderStorageModuleTest {
    private final Logger log = LogManager.getLogger(MailStorageModuleTest.class.getName());
    private FolderStorageDAO data;
    
    //A Rule is implemented as a class with methods that are associated     
     //with the lifecycle of a unit test. These methods run when required.     
     //Avoids the need to cut and paste code into every test method.     
    @Rule     
    public MethodLogger methodLogger = new MethodLogger();
    
    @Test
    public void createDirectoryTest() throws SQLException {
        data.createDirectory("newdir");
        assertTrue(data.findAll().contains("newdir"));
    } 
    
    @Test(expected=IllegalArgumentException.class)
    public void createDirectoryTest_InvalidArgument() throws SQLException {
        data.createDirectory("");
        fail();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void createDirectoryTest_DuplicateDir() throws SQLException {
        data.createDirectory("new");
        fail();
    }
    
    @Test
    public void deleteDirectoryTest() throws SQLException {
        data.deleteDirectory("new");
        assertFalse(data.findAll().contains("new"));
    } 
    
    @Test
    public void deleteDirectoryTest_DirNotExists() throws SQLException {
        int result = data.deleteDirectory("newdir");
        assertEquals(1, result);
    } 
    
    @Test(expected=IllegalArgumentException.class)
    public void deleteDirectoryTest_InvalidArgument() throws SQLException {
        data.deleteDirectory("");
        fail();
    }
    
    @Test
    public void findAllTest() throws SQLException {
        List <String> list = data.findAll();
        assertEquals(list.size(), 6);
    }
    
    @Test
    public void updateDirectoryTest() throws SQLException {
        data.updateDirectory("new","newdir");
        assertTrue(data.findAll().contains("newdir") && !data.findAll().contains("new"));
    } 
    
    @Test(expected=IllegalArgumentException.class)
    public void updateDirectoryTest_InvalidSameValueArgument() throws SQLException {
        data.updateDirectory("new", "new");
        fail();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void updateDirectoryTest_InvalidArgument() throws SQLException {
        data.updateDirectory("", "");
        fail();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void updateDirectoryTest_DuplicateDir() throws SQLException {
        data.updateDirectory("new", "trash");
        fail();
    }
    
    @Before
    public void init() {
        UserConfigBean userInfo = new UserConfigBean("cs.517.receive@gmail.com", "3t12ll0ngl3arn",
                993, "imap.gmail.com", 465, "smtp.gmail.com");
        userInfo.setMysqlPassword("compsci");
        userInfo.setMysqlPort(3306);
        userInfo.setMysqlUserName("local");
        userInfo.setMysqlUrl("localhost");
        userInfo.setMysqlDbName("emailapp");
        
        log.info("Seeding");
        final String seedDataScript = loadAsString("src/test/res/createDB.sql");
        try (Connection connection = DriverManager.getConnection
                        ("jdbc:mysql://"+userInfo.getMysqlUrl()+":"+userInfo.getMysqlPort()+"/"+
                                userInfo.getMysqlDbName(), userInfo.getMysqlUserName(), 
                                userInfo.getMysqlPassword());) {
            
            data = new FolderStorageModule(userInfo);
            
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }
    
    /**
     * Determines whether given string is a comment.
     * @param line The line in the database config file to examine.
     * @return true if this line is a comment; false otherwise.
     */
    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//") || line.startsWith("/*");
    }
    
    /**
     * Loads database creation file and returns it as a String.
     * @param path The path to the database config file.
     * @return database config file contents as a String.
     */
    private String loadAsString(final String path) {
        try {
            return new String(readAllBytes(get(path)));
        } catch (IOException io) {
            throw new RuntimeException("Unable to close input stream.", io);
        }
    }
    
    /**
     * Splits database config file into list of sql statements.
     * @param reader Reader object.
     * @param statementDelimiter Delimiter used in statements.
     * @return list of sql statements.
     */
    private List<String> splitStatements(Reader reader, String statementDelimiter) {
        final BufferedReader bufferedReader = new BufferedReader(reader);
        final StringBuilder sqlStatement = new StringBuilder();
        final List<String> statements = new LinkedList<>();
        try {
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isComment(line)) {
                    continue;
                }
                sqlStatement.append(line);
                if (line.endsWith(statementDelimiter)) {
                    statements.add(sqlStatement.toString());
                    sqlStatement.setLength(0);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new RuntimeException("Failed parsing sql", e);
        }
    }
}
