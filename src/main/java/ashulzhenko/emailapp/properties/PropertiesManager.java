package ashulzhenko.emailapp.properties;

import ashulzhenko.emailapp.bean.UserConfigBean;

/**
 * Saves and loads information from/to properties and UserConfigBean.
 *
 * @author Alena Shulzhenko
 * @version 30/09/2016
 * @since 1.8
 */
public class PropertiesManager {
    /**      
     * Returns a UserConfigBean object with the contents of the properties file.      
     *      
     * @param path Must exist, will not be created      
     * @param propFileName Name of the properties file      
     * @return The bean loaded with the properties      
     * @throws IOException      
     */     
    public final UserConfigBean loadTextProperties(final String path, final String propFileName) throws IOException {         
        Properties prop = new Properties();
    
        Path txtFile = get(path, propFileName + ".properties");         
        UserConfigBean userConfig = new UserConfigBean();         
        // File must exist         
        if (Files.exists(txtFile)) {             
            try (InputStream propFileStream = newInputStream(txtFile);) {                 
                prop.load(propFileStream);
            }            
            userConfig.setEmailPassword(prop.getProperty("emailPassword"));             
            userConfig.setFromEmail(prop.getProperty("fromEmail"));             
            userConfig.setImapPort(prop.getProperty("imapPort"));
            userConfig.setImapUrl(prop.getProperty("imapUrl")); 
            userConfig.setSmtpPort(prop.getProperty("smtpPort"));
            userConfig.setSmtpUrl(prop.getProperty("smtpUrl")); 
            
            userConfig.setMysqlPassword(prop.getProperty("mysqlPassword"));             
            userConfig.setMysqlPort(prop.getProperty("mysqlPort"));   
            userConfig.setMysqlUrl(prop.getProperty("mysqlUrl"));             
            userConfig.setMysqlUserName(prop.getProperty("mysqlUser"));
            userConfig.setMysqlDbName(prop.getProperty("mysqlDbName"));     

        }         
        return userConfig;     
    }
    
    /**      
     * Creates a plain text properties file based on the parameters      
     *      
     * @param path Must exist, will not be created      
     * @param propFileName Name of the properties file      
     * @param userConfig The bean to store into the properties      
     * @throws IOException      
     */     
    public final void writeTextProperties(final String path, final String propFileName, final UserConfigBean userConfig) 
            throws IOException {         
        Properties prop = new Properties();         
        prop.setProperty("emailPassword", userConfig.getEmailPassword());         
        prop.setProperty("fromEmail", userConfig.getFromEmail());         
        prop.setProperty("imapPort", userConfig.getImapPort());
        prop.setProperty("imapUrl", userConfig.getImapUrl());
        prop.setProperty("smtpPort", userConfig.getSmtpPort());
        prop.setProperty("smtpUrl", userConfig.getSmtpUrl());
        
        prop.setProperty("mysqlPassword", userConfig.getMysqlPassword());         
        prop.setProperty("mysqlPort", userConfig.getMysqlPort());
        prop.setProperty("mysqlUrl", userConfig.getMysqlUrl());         
        prop.setProperty("mysqlUser", userConfig.getMysqlUserName()); 
        prop.setProperty("mysqlDbName", userConfig.getMysqlDbName()); 
        
  
        Path txtFile = get(path, propFileName + ".properties");         
        // Creates the file or if file exists it is truncated to length of zero         
        // before writing         
        try (OutputStream propFileStream = newOutputStream(txtFile)) {             
            prop.store(propFileStream, "SMTP Properties");         
        }     
    }
}
