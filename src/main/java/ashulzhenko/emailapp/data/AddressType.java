package ashulzhenko.emailapp.data;

import ashulzhenko.emailapp.bean.EmailCustom;
import jodd.mail.MailAddress;

/**
 * Enum class that represents different types of addresses: BCC, CC, TO, REPLYTO.
 * 
 * @author Alena Shulzhenko
 * @version 30/09/2016
 * @since 1.8
 */
public enum AddressType {
    BCC(1),
    CC(2),
    TO(3),
    REPLYTO(4);
    
    private int type;
    private static final long serialVersionUID = 42051768871L;
    
    /**
	 * Private constructor for AddressType enum.
	 * 
	 * @param type The type of the email address in numeric value.
	 */
	private AddressType(int type) {
		this.type = type;
	}
    
    /**
	 * Returns an EmailCustom with added address.
	 * 
     * @param email Email message to which the address is added.
     * @param address The specific email address to add.
	 * 
	 * @return EmailCustom email message to which the address is added.
	 */
	public EmailCustom addToEmail(EmailCustom email, String address) {
        if(address == null || address.isEmpty() || email == null)
            throw new IllegalArgumentException ("Invalid address or email.");
        
		switch (type) {
			case 1 : email.bcc(address);
				   	   break;
			case 2 : email.cc(address);
					   break;
			case 3 : email.to(address);
				       break;
			case 4 : email.replyTo(address);
				       break;
		}
		return email;
	}
    
    /**
	 * Returns the array of email addresses for a particular type.
	 * 
     * @param email Email message to which the address is added.
	 * 
	 * @return the array of email addresses for a particular type.
	 */
	public MailAddress[] getList(EmailCustom email) {
        if(email == null)
            throw new IllegalArgumentException ("Invalid email.");
        
        MailAddress[] array = new MailAddress[0];
		switch (type) {
			case 1 : array = email.getBcc();
				   	   break;
			case 2 : array = email.getCc();
					   break;
			case 3 : array = email.getTo();
				       break;
			case 4 : array = email.getReplyTo();
				       break;
		}
		return array;
	}
    
    /**
	 * Returns the email type as an integer value.
	 * 
	 * @return the email type.
	 */
	public int getType() {
		return this.type;
	}
    
    /**
	 * Returns an AddressType if given its type as an integer.
	 * 
	 * @param type The type of the address to find.
	 * 
	 * @return the AddressType that corresponds to a given type.
	 * 
	 * @throws IllegalArgumentException If given type is invalid.
	 */
	public static AddressType getAddress(String type) {
		AddressType address;
		
		switch (type) {
			case "1" : address = BCC;
				   	   break;
			case "2" : address = CC;
					   break;
			case "3" : address = TO;
				       break;
			case "4" : address = REPLYTO;
				       break;
		    //No matching enum constant
			default :  throw new IllegalArgumentException(type
							+ " is not a valid type for the address.");
		}
		return address;
	}
    
    
    
    
}
