// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver.testserver;


import ru.vachok.networker.AppComponents;

import javax.mail.MessagingException;
import javax.mail.Session;


/**
 @since 27.06.2019 (10:37) */
public interface MailTester {
    
    
    Session MAIL_SESSION = Session.getInstance(AppComponents.getMailProps());
    
    
    String testInput() throws MessagingException;
    
    String testOutput() throws MessagingException;
    
    String testComplex() throws MessagingException;
}
