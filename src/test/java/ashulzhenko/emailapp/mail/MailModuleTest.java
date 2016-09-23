package ashulzhenko.emailapp.mail;

import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.bean.EmailCustom;
import ashulzhenko.emailapp.interfaces.Mailer;
import java.util.Arrays;
import java.util.Collection;
import jodd.mail.EmailAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests MailModule.
 * @author Alena Shulzhenko
 */
@RunWith(Parameterized.class)
public class MailModuleTest {
    
    private final Logger log = LogManager.getLogger(MailModuleTest.class.getName());
    private EmailCustom email, emailReceived;
    private String[] to, cc, bcc;
    private String subject, message;
    private String[] attach, embed;
    
    //parameters: to, cc, bcc, subject, message, attachments, embedAttachments
    @Parameters(name = "{index} plan[{0}]={1}]")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            //1: plain email 1 receiver
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[0], "plain email 1 receiver", 
                "a very important plain message", new String[0], new String[0]},
            //2: plain email 2 receivers
            {new String[]{"cs.517.receive@gmail.com", "cs.517.send@gmail.com"}, new String[0], 
                new String[0], "plain email 2 receivers", 
                "a very important plain message", new String[0], new String[0]},
            //3: plain email 1 receiver 1 cc
            {new String[]{"cs.517.receive@gmail.com"}, new String[]{"cs.517.send@gmail.com"}, 
                new String[0], "plain email 1 receiver 1 cc", 
                "a very important plain message", new String[0], new String[0]},
            //4: plain email 1 receiver 2 cc
            {new String[]{"cs.517.receive@gmail.com"}, new String[]{"cs.517.send@gmail.com", 
                "cs.517.send@outlook.com"}, new String[0], "plain email 1 receiver 2 cc", 
                "a very important plain message", new String[0], new String[0]},
            //5: plain email 1 receiver 1 bcc
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[]{"cs.517.send@gmail.com"}, "plain email 1 receiver 1 bcc", 
                "a very important plain message", new String[0], new String[0]},
            //6: plain email 1 receiver 2 bcc
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[]{"cs.517.send@gmail.com", "cs.517.send@outlook.com"}, 
                "plain email 1 receiver 2 bcc", "a very important plain message", 
                new String[0], new String[0]},
            //7: plain email 1 attach
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[0], "plain email 1 attach", "a very important plain message", 
                new String[]{"src/test/res/c.jpg"}, new String[0]},
            //8: html email 1 embed attach
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[0], "html email 1 embed attach", 
                "a very important message<html><body><img src='cid:c.jpg'/><body></html>", 
                new String[0], new String[]{"src/test/res/c.jpg"}},
            //9: html email 1 embed attach and 1 usual attach
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[0], "html email 1 embed attach and 1 usual attach", 
                "a very important message<html><META http-equiv=Content-Type "
                    + "content=\"text/html; charset=utf-8\"><body><h2>this is a header</h2>"
                    + "<img src='cid:c.jpg'/></body></html>now with html and plain", 
                new String[]{"src/test/res/w.jpg"}, new String[]{"src/test/res/c.jpg"}},
            //10: all included (html email 2 to, 1 cc, 1 bcc, 1 embed and 1 attach)
            {new String[]{"cs.517.receive@gmail.com", "cs.517.send@outlook.com"}, 
                new String[]{"cs.517.send@gmail.com"}, new String[]{"cs.517.send@outlook.com"}, 
                "html email 2 to, 1 cc, 1 bcc, 1 embed and 1 attach", 
                "a very important message<html><META http-equiv=Content-Type "
                    + "content=\"text/html; charset=utf-8\"><body><h2>this is a header</h2>"
                    + "<img src='cid:c.jpg'/></body></html>now with html and plain", 
                new String[]{"src/test/res/w.jpg"}, new String[]{"src/test/res/c.jpg"}},
            //11: html email 1 embed attach with invalid cid (becomes usual attach)
            {new String[]{"cs.517.receive@gmail.com"}, new String[0], 
                new String[0], "html email 1 invalid cid embed attach (becomes usual attach)", 
                "a very important message<html><body><img src='cid:a.jpg'/><body></html>", 
                new String[0], new String[]{"src/test/res/c.jpg"}}
    });
    }
    
    
    public MailModuleTest (String[] to, String[] cc, String[] bcc, String subject, 
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
        UserConfigBean userInfo = new UserConfigBean("cs.517.send@gmail.com", "v3ryl0ngp@2s", 
                    993, "imap.gmail.com", 465, "smtp.gmail.com");
        UserConfigBean receiverInfo = new UserConfigBean("cs.517.receive@gmail.com", "3t12ll0ngl3arn", 
                                      993, "imap.gmail.com", 465, "smtp.gmail.com");
        Mailer send = new MailModule (userInfo);
        Mailer receive = new MailModule(receiverInfo);
        
        email = send.sendEmail(to, cc, bcc, subject, message, attach, embed);
        
        //wait for gmail to receive the message
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error("Threaded sleep failed", e);
            System.exit(1);
        }

        emailReceived = receive.receiveEmail().get(0);
    }

    @Test
    public void TestSentAndReceivedMessages() {
        log.debug("in TestSentAndReceivedMessages with " + email.getSubject());

        assertTrue(email.equals(emailReceived));
    }
    
    @Test
    public void TestBcc() {
        //since bcc is invisible for recipients, it is always absent in the received email
        assertArrayEquals(emailReceived.getBcc(), new EmailAddress[0]);
    }
    
}
