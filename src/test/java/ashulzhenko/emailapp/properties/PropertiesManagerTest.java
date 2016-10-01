package ashulzhenko.emailapp.properties;

import ashulzhenko.emailapp.bean.UserConfigBean;
import ashulzhenko.emailapp.rules.MethodLogger;
import static org.junit.Assert.*; 
import java.io.FileNotFoundException; 
import java.io.IOException; 
import org.junit.Before; 
import org.junit.Rule; 
import org.junit.Test;

/**
 * Tests PropertiesManager class.
 */
public class PropertiesManagerTest {
        @Rule     
        public MethodLogger methodLogger = new MethodLogger();     
        private PropertiesManager pm;
        
        @Before     
        public void setUp() throws Exception {         
            pm = new PropertiesManager();     
        } 
        
        @Test     
        public void testWriteText() throws FileNotFoundException, IOException {         
            UserConfigBean userConfig1 = new UserConfigBean("cs.517.send@gmail.com", "v3ryl0ngp@2s", 
                                            993, "imap.gmail.com", 465, "smtp.gmail.com");
            pm.writeTextProperties("", "src/test/res/TextProps", userConfig1);         
            UserConfigBean userConfig2 = pm.loadTextProperties("", "src/test/res/TextProps");         
            assertEquals("The two beans do not match", userConfig1, userConfig2);     
        } 
}
