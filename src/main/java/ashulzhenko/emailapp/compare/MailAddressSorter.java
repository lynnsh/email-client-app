package ashulzhenko.emailapp.compare;

import java.util.Comparator;
import jodd.mail.MailAddress;

/**
 * MailAddressSorter implements the Comparator<MailAddress> interface 
 * to enable sorting of MailAdresses by email address.
 * 
 * @author Alena Shulzhenko
 * @version 16/09/2016
 * @since 1.8
 */
public class MailAddressSorter implements Comparator<MailAddress> {

    /**
     * Compares two MailAddresses based on emails.
     * 
     * @param o1 the first MailAddress to be compared.
     * @param o2 the second MailAddress to be compared.
     * 
     * @return a negative integer, zero, or a positive integer as the first
	 * 		   argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(MailAddress o1, MailAddress o2) {
        if(o1 == o2)
            return 0;
        if(o1 == null || o2 == null)
            throw new IllegalArgumentException("One of the provided values is null");
        
        return o1.getEmail().compareTo(o2.getEmail());
    }
    
}
