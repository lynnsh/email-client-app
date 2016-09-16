package ashulzhenko.emailapp.compare;

import java.util.Comparator;
import jodd.mail.EmailAttachment;

/**
 * EmailAttachmentSorter implements the Comparator<EmailAttachment> interface 
 * to enable sorting of EmailAttachment by name.
 * 
 * @author Alena Shulzhenko
 * @version 16/09/2016
 * @since 1.8
 */
public class EmailAttachmentSorter implements Comparator<EmailAttachment> {

    /**
     * Compares two EmailAttachments based on name.
     * 
     * @param o1 the first EmailAttachment to be compared.
     * @param o2 the second EmailAttachment to be compared.
     * 
     * @return a negative integer, zero, or a positive integer as the first
	 * 		   argument is less than, equal to, or greater than the second.
     */
    @Override
    public int compare(EmailAttachment o1, EmailAttachment o2) {
        if(o1 == o2)
            return 0;
        if(o1 == null || o2 == null)
            throw new IllegalArgumentException("One of the provided values is null");
        
        return o1.getName().compareTo(o2.getName());
    }
    
}
