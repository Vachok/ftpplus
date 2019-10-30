// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mail.testserver;


import com.sun.mail.smtp.SMTPMessage;
import org.testng.Assert;
import org.testng.annotations.*;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;


/**
 @see MailPOPTester
 @since 27.06.2019 (10:42) */
public class MailPOPTesterTest {
    
    
    private static final Session MAIL_SESSION = Session.getInstance(AppComponents.getMailProps());
    
    private TestConfigure testConfigure = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private String userName;
    
    private char[] passChars;
    
    private File fileForAppend = new File("err" + System.getProperty(PropertiesNames.PRSYS_SEPARATOR) + "mail.err");
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 5));
        testConfigure.before();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.after();
    }
    
    @Test(enabled = false)
    public void realTesting() {
        MailTester mailTester = new MailPOPTester();
        try {
            mailTester.testComplex();
        }
        catch (MessagingException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test(enabled = false)
    public void testTestInput() throws MessagingException {
        
        Store mailSessionStore = null;
        try {
            mailSessionStore = MAIL_SESSION.getStore();
            mailSessionStore.connect(ConstantsFor.SRV_MAIL3, "ad", "1qaz@WSX");
        }
        catch (MessagingException e) {
            Assert.assertNull(e, e.getMessage());
        }
        
        Folder inboxFolder = null;
        try {
            Folder defaultFolder = mailSessionStore.getDefaultFolder();
            URLName folderURLName = defaultFolder.getURLName();
            Folder[] folders = defaultFolder.list();
            for (Folder folder : folders) {
                if (folder.getName().equalsIgnoreCase(MailPOPTester.INBOX_FOLDER)) {
                    inboxFolder = folder;
                }
            }
        }
        catch (MessagingException e) {
            Assert.assertNull(e, e.getMessage());
        }
        try {
            inboxFolder.open(Folder.READ_ONLY);
            System.out.println(inboxFolder.getMessageCount());
        }
        catch (MessagingException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    @Test(enabled = false)
    public void testTestOutput() throws MessagingException {
        Transport sessionTransport = MAIL_SESSION.getTransport();
        SMTPMessage testMessage = new SMTPMessage(MAIL_SESSION);
        testMessage.setFrom("kusok@govna.kto");
        testMessage.setSubject("test SMTP " + new Date());
        testMessage.setText(sessionTransport.toString());
        sessionTransport.connect();
        sessionTransport.sendMessage(testMessage, new InternetAddress[]{new InternetAddress("m.v.spirin@velkomfood.ru"), new InternetAddress("netvisor@eatmeat.ru")});
    }
    
    @Test(enabled = false)
    public void testTestComplex() throws MessagingException {
        testTestOutput();
        testTestInput();
    }
    
    @Test
    public void logFileReader() {
        MailTester mailTester = new MailPOPTester();
        try {
            String objectToFileResult = FileSystemWorker.appendObjectToFile(fileForAppend, mailTester.testComplex());
            Assert.assertTrue(objectToFileResult.contains(fileForAppend.getName()));
            Files.deleteIfExists(fileForAppend.toPath().toAbsolutePath().normalize());
            
        }
        catch (MessagingException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            fileForAppend.deleteOnExit();
        }
    }
}