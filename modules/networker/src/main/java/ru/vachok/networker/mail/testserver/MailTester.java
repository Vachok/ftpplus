// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mail.testserver;


import javax.mail.MessagingException;


/**
 @since 27.06.2019 (10:37) */
public interface MailTester {
    
    
    String testInput() throws MessagingException;
    
    String testOutput() throws MessagingException;
    
    String testComplex() throws MessagingException;
}
