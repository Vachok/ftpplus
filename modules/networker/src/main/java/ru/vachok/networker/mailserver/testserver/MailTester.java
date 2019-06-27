package ru.vachok.networker.mailserver.testserver;


import ru.vachok.mysqlandprops.props.FileProps;

import javax.mail.MessagingException;
import javax.mail.Session;


/**
 @since 27.06.2019 (10:37) */
public interface MailTester {
    
    
    Session MAIL_SESSION = Session.getDefaultInstance(new FileProps("mail").getProps());
    
    
    String testInput() throws MessagingException;
    
    String testOutput() throws MessagingException;
    
    String testComplex() throws MessagingException;
}
