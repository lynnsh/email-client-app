/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.interfaces.MailStorageDAO;
import java.io.BufferedReader;
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
import org.junit.Ignore;
import org.junit.Test;
import static java.nio.file.Paths.get;

/**
 * Tests MailStorageModule.
 * @author Alena Shulzhenko
 */
public class MailStorageModuleTest {
    private final Logger log = LogManager.getLogger(MailStorageModuleTest.class.getName());
    private UserConfigBean userInfo;
    
    @Test
    public void saveEmailTest() throws SQLException {
        //try{
        MailStorageDAO data = new MailStorageModule(userInfo);
        EmailCustom email = createEmail();
        int id = data.saveEmail(email);
        assertEquals(email, data.findEmailById(id));
       // }
       // catch(NullPointerException npe){
        //    npe.printStackTrace();
        //}
    }
    
    @Test
    public void deleteEmailTest() throws SQLException {
        MailStorageDAO data = new MailStorageModule(userInfo);
        data.deleteEmail(3);
        assertEquals(null, data.findEmailById(3));
    }
    
    @Test
    public void findAllTest() throws SQLException {
        MailStorageDAO data = new MailStorageModule(userInfo);
        List <EmailCustom> list = data.findAll();
        assertEquals(list.size(), 6);
    }
    
    @Test
    public void findAllInDirectoryTest() throws SQLException {
        MailStorageDAO data = new MailStorageModule(userInfo);
        List <EmailCustom> list = data.findAllInDirectory("inbox");
        assertEquals(list.size(), 3);
    }
    
    @Test
    public void findEmailByIdTest() throws SQLException {
        MailStorageDAO data = new MailStorageModule(userInfo);
        EmailCustom emailDb = data.findEmailById(3);
        EmailCustom email = createEmail();
        assertEquals(email, emailDb);
    }
    
    @Test
    public void findEmailByIdTest_NotInDb() throws SQLException {
        MailStorageDAO data = new MailStorageModule(userInfo);
        EmailCustom emailDb = data.findEmailById(100);
        assertEquals(null, emailDb);
    }
    
    @Test
    public void updateEmailDirectoryTest() throws SQLException {
        MailStorageDAO data = new MailStorageModule(userInfo);
        EmailCustom email = data.findEmailById(4);
        email.setDirectory("trash");
        data.updateEmailDirectory(email);
        assertEquals("trash", data.findEmailById(4).getDirectory());
    }
    
    @Before
    public void init() {
        userInfo = new UserConfigBean("cs.517.receive@gmail.com", "3t12ll0ngl3arn", 
                                      993, "imap.gmail.com", 465, "smtp.gmail.com");
        userInfo.setMysqlPassword("compsci");
        userInfo.setMysqlPort(3306);
        userInfo.setMysqlUserName("local");
        userInfo.setMysqlUrl("localhost");
        
        log.info("Seeding");
        final String seedDataScript = loadAsString("src/test/res/createDB.sql");
        try (Connection connection = DriverManager.getConnection
                ("jdbc:mysql://"+userInfo.getMysqlUrl()+":"+userInfo.getMysqlPort(),
                userInfo.getMysqlUserName(), userInfo.getMysqlPassword());) {
            for (String statement : splitStatements(new StringReader(seedDataScript), ";")) {
                connection.prepareStatement(statement).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed seeding database", e);
        }
    }
    
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
     * The following methods support the seedDatabse method
     */
    private String loadAsString(final String path) {
        try {
            return new String(readAllBytes(get(path)));
        } catch (IOException io) {
            throw new RuntimeException("Unable to close input stream.", io);
        }
    }
    
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
    
    private boolean isComment(final String line) {
        return line.startsWith("--") || line.startsWith("//") || line.startsWith("/*");
    }
}
