package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import ashulzhenko.emailapp.rules.MethodLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import static java.nio.file.Files.readAllBytes;
import jodd.mail.EmailAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import static java.nio.file.Paths.get;
import jodd.mail.EmailAttachment;
import static org.junit.Assert.fail;

/**
 * Tests MailStorageModule.
 * @author Alena Shulzhenko
 */
public class MailStorageModuleTest {
    private final Logger log = LogManager.getLogger(MailStorageModuleTest.class.getName());
    private MailStorageDAO data;
    
     //A Rule is implemented as a class with methods that are associated     
     //with the lifecycle of a unit test. These methods run when required.     
     //Avoids the need to cut and paste code into every test method.     
    @Rule     
    public MethodLogger methodLogger = new MethodLogger();
    
    @Test
    public void deleteEmailTest() throws SQLException {
        data.deleteEmail(3);
        assertEquals(null, data.findEmailById(3));
    } 
    
    @Test
    public void deleteEmailTest_IdNotExists() throws SQLException {
        int result = data.deleteEmail(33);
        assertEquals(result, 0);
    }   
    
    @Test(expected=IllegalArgumentException.class)
    public void deleteEmailTest_InvalidArgument() throws SQLException {
        data.deleteEmail(-3);
        fail();
    }
    
    @Test
    public void findAllInDirectoryTest() throws SQLException {
        List <EmailCustom> list = data.findAllInDirectory("inbox");
        assertEquals(list.size(), 3);
    }  
    
    @Test
    public void findAllInDirectoryTest_DirNotExists() throws SQLException {
        List <EmailCustom> list = data.findAllInDirectory("new");
        assertEquals(list.size(), 0);
    } 
    
    @Test(expected=IllegalArgumentException.class)
    public void findAllInDirectoryTest_InvalidArgument() throws SQLException {
        data.findAllInDirectory("");
        fail();
    }
     
    @Test
    public void findAllTest() throws SQLException {
        List <EmailCustom> list = data.findAll();
        assertEquals(list.size(), 6);
    }
    
    @Test
    public void findEmailByIdTest() throws SQLException {
        EmailCustom emailDb = data.findEmailById(3);
        EmailCustom email = createEmail();
        assertEquals(email, emailDb);
    } 
    
    @Test(expected=IllegalArgumentException.class)
    public void findEmailByIdTest_InvalidArgument() throws SQLException {
        data.findEmailById(-3);
        fail();
    }  
    
    @Test
    public void findEmailByIdTest_NotInDb() throws SQLException {
        EmailCustom emailDb = data.findEmailById(100);
        assertEquals(null, emailDb);
    }
    
    @Test
    public void saveEmailTest() throws SQLException {
        EmailCustom email = createEmail();
        email.embed(EmailAttachment.attachment().bytes(new File("src/test/res/c.jpg")));
        email.attach(EmailAttachment.attachment().file(new File("src/test/res/w.jpg")));
        int id = data.saveEmail(email);
        assertEquals(email, data.findEmailById(id));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void saveEmailTest_InvalidArgument() throws SQLException {
        data.saveEmail(null);
        fail();
    }
    
    @Test
    public void updateEmailDirectoryTest() throws SQLException {
        EmailCustom email = data.findEmailById(4);
        email.setDirectory("trash");
        data.updateEmailDirectory(email);
        assertEquals("trash", data.findEmailById(4).getDirectory());
    } 
    
    @Test
    public void updateEmailDirectoryTest_EmailIdNotInDb() throws SQLException {
        EmailCustom email = createEmail();
        email.setId(100);
        int result = data.updateEmailDirectory(email);
        assertEquals(0, result);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void updateEmailDirectoryTest_InvalidArgument() throws SQLException {
        data.updateEmailDirectory(null);
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
            
            data = new MailStorageModule(userInfo);
            
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }
    
    /**
     * Creates new EmailCustom object.
     * @return new EmailCustom object.
     */
    private EmailCustom createEmail() {
        EmailCustom email = new EmailCustom();
        email.setDirectory("inbox");
        email.cc(new EmailAddress[]{new EmailAddress("cs.517.send@outlook.com"),
                                     new EmailAddress("cs.517.send@gmail.com")});
        email.from("cs.517.send@gmail.com");
        email.addText("plain text3");
        email.to("cs.517.receive@gmail.com");
        email.subject("important3");
        return email;
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