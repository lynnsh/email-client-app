package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.bean.UserConfigBean;
import java.util.Arrays;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
@Ignore
/**
 * Tests MailModule with invalid data.
 * @author Alena Shulzhenko
 */
@RunWith(Parameterized.class)
public class MailModuleExceptionsTest {
    
    private final Logger log = LogManager.getLogger(MailModuleExceptionsTest.class.getName());
    private UserConfigBean userInfo;
    private String[] to;
    private String[] cc;
    private String[] bcc;
    private String subject;
    private String message;
    private String[] attach;
    private String[] embed;
    
    //parameters: to, cc, bcc, subject, message, attachments, embedAttachments
    @Parameters(name = "{index} plan[{0}]={1}]")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            //1: no to's addresses
            {new String[0], new String[0], new String[0], "no to's addresses", 
                "a very important message", new String[0], new String[0]},
            //2: null for cc
            {new String[]{"cs.517.receive@gmail.com", "cs.517.send@gmail.com"}, null, 
                new String[0], "null for cc", "a very important message", new String[0], new String[0]},
            //3: to with invalid email
            {new String[]{"cs.517.receivegmail.com"}, new String[]{"cs.517.send@gmail.com"}, 
                new String[0], "to with invalid email", 
                "a very important message", new String[0], new String[0]},
            //4: cc with invalid email
            {new String[]{"cs.517.receive@gmail.com"}, new String[]{"cs.517.send@"}, 
                new String[0], "cc with invalid email", 
                "a very important plain message", new String[0], new String[0]},
            //5: bcc with invalid email
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[]{"cs.517.send@gmailcom"}, "bcc with invalid email", 
                "a very important message", new String[0], new String[0]},
            //6: attachment error: file not found
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[0], "attachment error: file not found", "a very important message", 
                new String[]{"src/test/res/notfound.jpg"}, new String[0]},        
    });
    }
    
    
    public MailModuleExceptionsTest (String[] to, String[] cc, String[] bcc, String subject, 
                           String message, String[] attach, String[] embed) {
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.message = message;
        this.attach = attach;
        this.embed = embed;
    }
    
    @Before
    public void init() {
        userInfo = new UserConfigBean("cs.517.send@gmail.com", "v3ryl0ngp@2s", 
                    993, "imap.gmail.com", 465, "smtp.gmail.com");
    }

    @Test(expected=IllegalArgumentException.class)
    public void TestExceptions() {
        log.debug("in TestExceptions with " + subject);

        MailModule send = new MailModule (userInfo);
        //should not be able send the email because of the invalid data
        send.sendEmail(to, cc, bcc, subject, message, attach, embed);
        fail();
    }
    
}
