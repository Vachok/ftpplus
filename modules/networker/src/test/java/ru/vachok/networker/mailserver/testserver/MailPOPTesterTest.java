package ru.vachok.networker.mailserver.testserver;


import com.sun.mail.smtp.SMTPMessage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.util.Date;


/**
 @see MailPOPTester
 @since 27.06.2019 (10:42) */
public class MailPOPTesterTest {
    
    
    private TestConfigure testConfigure = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());
    
    private static final Session MAIL_SESSION = Session.getDefaultInstance(new FileProps("mail").getProps());
    
    private String userName;
    
    private char[] passChars;
    
    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigure.beforeClass();
    }
    
    @AfterClass
    public void tearDown() {
        testConfigure.afterClass();
    }
    
    @Test
    public void realTesting() {
        MailTester mailTester = new MailPOPTester();
        try {
            System.out.println("mailTester = " + mailTester.testComplex());
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
            mailSessionStore.connect("srv-mail3.eatmeat.ru", "ikudryashov", "netzero0912");
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
                if (folder.getName().equalsIgnoreCase("inbox")) {
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
        testMessage.setSubject("test SMTP " + new Date());
        testMessage.setText(sessionTransport.toString());
        sessionTransport.connect();
        sessionTransport.sendMessage(testMessage, new InternetAddress[]{new InternetAddress("scanner@eatmeat.ru")});
    }
    
    @Test(enabled = false)
    public void testTestComplex() throws MessagingException {
        testTestOutput();
        testTestInput();
    }
}