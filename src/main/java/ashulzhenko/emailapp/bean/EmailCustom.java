package ashulzhenko.emailapp.bean;

import ashulzhenko.emailapp.compare.EmailAttachmentSorter;
import ashulzhenko.emailapp.compare.EmailMessageSorter;
import ashulzhenko.emailapp.compare.MailAddressSorter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.mail.Flags;
import jodd.mail.Email;
import jodd.mail.EmailAttachment;
import jodd.mail.EmailMessage;
import jodd.mail.MailAddress;
import jodd.mail.ReceivedEmail;

/**
 * EmailCustom class that describes an email. It extends Jodd's Email with added
 * field for the email directory.
 *
 * @author Alena Shulzhenko
 * @version 18/09/2016
 * @since 1.8
 */
public class EmailCustom extends Email implements Serializable {
    
    private static final long serialVersionUID = 42051768871L;
    private List<ReceivedEmail> attachedMessages;
    //used to differentiate send and received emails ("sent" and "inbox" directories)
    private String directory;
    private Flags flags;
    private int id = -1;
    private int messageNumber;
    private Date rcvDate;

    /**
     * Instantiates EmailCustom object. Sets directory default to sent.
     */
    public EmailCustom() {
        super();
        this.directory = "sent";
    }

    /**
     * Instantiates EmailCustom object from Received email object.
     * Sets directory to inbox.
     *
     * @param rcvEmail ReceivedEmail to convert to EmailCustom.
     */
    public EmailCustom(ReceivedEmail rcvEmail) {
        if (rcvEmail == null) {
            throw new IllegalArgumentException("Received email value is null.");
        }

        this.flags = rcvEmail.getFlags();
        this.directory = "inbox";

        List<EmailAttachment> list = rcvEmail.getAttachments();
        if (list != null) {
            this.attachments = new ArrayList<>(list);
        }

        this.bcc = rcvEmail.getBcc();
        this.cc = rcvEmail.getCc();
        this.from = rcvEmail.getFrom();
        this.messages = rcvEmail.getAllMessages();
        this.messageNumber = rcvEmail.getMessageNumber();
        this.attachedMessages = rcvEmail.getAttachedMessages();
        this.replyTo = rcvEmail.getReplyTo();
        this.sentDate = rcvEmail.getSentDate();
        this.subject = rcvEmail.getSubject();
        this.subjectEncoding = rcvEmail.getSubjectEncoding();
        this.to = rcvEmail.getTo();
        this.setPriority(rcvEmail.getPriority());
        this.rcvDate = rcvEmail.getReceiveDate();
    }
    
    
    /**
     * Compares this EmailCustom to the specified object. The result is true if
     * the two objects are of the same class and from, to, cc addresses as well
     * as subject, messages contents and attachments are equal. BCC is not
     * checked since for ReceivedEmail the receiver does not have the access to
     * other recipients.
     *
     * @param obj The object to compare this against.
     * 
     * @return true if objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        EmailCustom email;
        
        if (this == obj) 
            return true;
        
        if (obj == null) 
            return false;
        
        if (obj instanceof EmailCustom) 
            email = (EmailCustom) obj;
        else 
            return false;
        
        if (!Objects.equals(this.from.toString(), email.from.toString())) 
            return false;

        //since Jodd.MailAddress and EmailAttachment do not implement equals
        if (!compareArrays(this.to, email.to)) 
            return false;

        if (!compareArrays(this.cc, email.cc)) 
            return false;

        if (!checkAttachments(this.attachments, email.attachments)) 
            return false;

        if (!checkMessagesContent(this.messages, email.messages)) 
            return false;

        return Objects.equals(this.subject, email.subject);
    }

    /**
     * Returns the attached messages.
     *
     * @return the attached messages.
     */
    public List<ReceivedEmail> getAttachedMessages() {
        return attachedMessages;
    }
    /**
     * Returns the directory name where the email is located.
     *
     * @return the directory name where the email is located.
     */
    public String getDirectory() {
        return directory;
    }
    /**
     * Returns flags for the received message.
     *
     * @return flags for the received message.
     */
    public Flags getFlags() {
        return flags;
    }
    /**
     * Returns the unique id of the message.
     *
     * @return id of the message.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the message number.
     *
     * @return the message number.
     */
    public int getMessageNumber() {
        return messageNumber;
    }
    /**
     * Returns the received date of this email.
     *
     * @return the received date of this email.
     */
    public Date getReceivedDate() {
        return rcvDate;
    }
    /**
     * Returns a hash code value for this object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.from);
        hash = 71 * hash + Objects.hashCode(this.to);
        hash = 71 * hash + Objects.hashCode(this.cc);
        hash = 71 * hash + Objects.hashCode(this.subject);
        hash = 71 * hash + Objects.hashCode(this.messages);
        hash = 71 * hash + Objects.hashCode(this.attachments);
        return hash;
    }

    /**
     * Sets the attached messages for this email.
     *
     * @param attachedMessages The attached messages for this email.
     */
    public void setAttachedMessages(List<ReceivedEmail> attachedMessages) {
        if(attachedMessages != null)
            this.attachedMessages = attachedMessages;
        else
            throw new IllegalArgumentException("Attached messages value is null.");
    }
    
    /**
     * Sets the directory where the email is located.
     *
     * @param directory the directory name where the email is located.
     */
    public void setDirectory(String directory) {
        if (directory != null && !directory.isEmpty()) 
            this.directory = directory;
        else 
            throw new IllegalArgumentException("No directory name provided.");
    }

    /**
     * Sets flags for received message.
     *
     * @param flags Flags for received message to set.
     */
    public void setFlags(Flags flags) {
        if(flags != null)
            this.flags = flags;
        else
            throw new IllegalArgumentException("Flags value is null.");
    }
    
    /**
     * Sets the id of the message.
     *
     * @param id The id of the message.
     */
    public void setId(int id) {
        if(id > 0)
            this.id = id;
        else
            throw new IllegalArgumentException("Id value is invalid: " + id);
    }
    
    /**
     * Sets the message number.
     *
     * @param messageNumber The message number.
     */
    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }
    
    /**
     * Sets the received date of the email.
     *
     * @param rcvDate The received date of the email.
     */
    public void setReceivedDate(Date rcvDate) {
        if(rcvDate != null)
            this.rcvDate = rcvDate;
        else
            throw new IllegalArgumentException("Received date value is null.");
    }


    /**
     * Compares EmailAttachments.
     *
     * @param first the first EmailAttachment to be compared.
     * @param second the second EmailAttachment to be compared.
     * 
     * @return true if attachments are the same; false otherwise.
     */
    private boolean checkAttachments(List<EmailAttachment> first, List<EmailAttachment> second) {
        EmailAttachment ea1, ea2;
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        int length = first.size();
        if (length != second.size()) {
            return false;
        }

        Collections.sort(first, new EmailAttachmentSorter());
        Collections.sort(second, new EmailAttachmentSorter());

        for (int i = 0; i < length; i++) {
            ea1 = first.get(i);
            ea2 = second.get(i);
            if (ea1 != ea2) {
                if (ea1 == null || ea2 == null)
                    return false;
                byte[] b1 = ea1.toByteArray();
                byte[] b2 = ea2.toByteArray();
                for (int j = 0; j < b1.length; j++) {
                    if (b1[j] != b2[j])
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares EmailMessages. If the content of both messages is the same,
     * those messages are considered equal.
     *
     * @param first the first EmailMessage to be compared.
     * @param second the second EmailMessage to be compared.
     * 
     * @return true if contents of both messages are the same; false otherwise.
     */
    private boolean checkMessagesContent(List<EmailMessage> first, List<EmailMessage> second) {
        EmailMessage em1, em2;
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        int length = first.size();
        if (length != second.size()) {
            return false;
        }
        
        Collections.sort(first, new EmailMessageSorter());
        Collections.sort(second, new EmailMessageSorter());

        for (int i = 0; i < length; i++) {
            em1 = first.get(i);
            em2 = second.get(i);
            if (em1 != em2) {
                if (em1 == null || em2 == null)
                    return false;
                if (!em1.getContent().trim().equals(em2.getContent().trim()))
                    return false;
            }
        }
        return true;
    }

    /**
     * Compares two MailAddress arrays.
     *
     * @param first the first array to be compared.
     * @param second the second array to be compared.
     * 
     * @return true if elements of arrays are the same; false otherwise.
     */
    private boolean compareArrays(MailAddress[] first, MailAddress[] second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.length != second.length) {
            return false;
        }

        Arrays.sort(first, new MailAddressSorter());
        Arrays.sort(second, new MailAddressSorter());

        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                if (first[i] == null || second[i] == null) {
                    return false;
                }
                if (!first[i].toString().equals(second[i].toString())) {
                    return false;
                }
            }
        }
        return true;
    }
}
