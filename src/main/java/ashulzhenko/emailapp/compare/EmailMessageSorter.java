package ashulzhenko.emailapp.compare;

import java.util.Comparator;
import jodd.mail.EmailMessage;

/**
 * EmailMessageSorter implements the Comparator<EmailMessage> interface 
 * to enable sorting of EmailMessages by content.
 * 
 * @author Alena Shulzhenko
 * @version 16/09/2016
 * @since 1.8
 */
public class EmailMessageSorter implements Comparator<EmailMessage> {

    /**
     * Compares two EmailMessages based on content.
     * 
     * @param o1 the first EmailMessage to be compared.
     * @param o2 the second EmailMessage to be compared.
     * 
     * @return a negative integer, zero, or a positive integer as the first
	 * 		   argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(EmailMessage o1, EmailMessage o2) {
        if(o1 == o2)
            return 0;
        if(o1 == null || o2 == null)
            throw new IllegalArgumentException("One of the provided values is null");
        
        return o1.getContent().compareTo(o2.getContent());
    }
    
}
