// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mail.testserver;


import com.sun.mail.smtp.SMTPMessage;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.controller.MatrixCtr;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.DBMessenger;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.mail.*;
import javax.mail.event.TransportAdapter;
import javax.mail.event.TransportEvent;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.mail.testserver.MailPOPTesterTest
 @since 27.06.2019 (10:41) */
public class MailPOPTester implements MailTester, Runnable {
    
    
    private static final Session MAIL_SESSION = Session.getInstance(AppComponents.getMailProps());
    
    private static final String MAIL_IKUDRYASHOV = "ikudryashov@eatmeat.ru";
    
    protected static final String INBOX_FOLDER = "inbox";
    
    private String mailIsNotOk = "MailServer isn't ok";
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private StringBuilder stringBuilder = new StringBuilder();
    
    private File fileForAppend = new File("err" + System.getProperty(PropertiesNames.PRSYS_SEPARATOR) + "mail.err");
    
    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try {
            setWebString();
        }
        catch (MessagingException e) {
            this.messageToUser = MessageToUser.getInstance(MessageToUser.DB, getClass().getSimpleName());
            messageToUser.error(e.getMessage());
            MatrixCtr.setMailIsOk(mailIsNotOk);
            mailIsNotOk = UsefulUtilities.getHTMLCenterColor("red", mailIsNotOk);
            mailIsNotOk = MessageFormat.format("{3}: {0}{1}\n{2}", mailIsNotOk, e.getMessage(), new TForms().fromArray(e, false), new Date());
            FileSystemWorker.appendObjectToFile(fileForAppend, mailIsNotOk);
            AppComponents.threadConfig().getTaskScheduler().scheduleWithFixedDelay(new MailPOPTester(), TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        }
    }
    
    private void setWebString() throws MessagingException {
        
        String complexResult = testComplex();
        if (complexResult.contains("from: ikudryashov@eatmeat.ru, ; Subj: test SMTP")) {
            MatrixCtr.setMailIsOk(UsefulUtilities.getHTMLCenterColor(ConstantsFor.GREEN, "MailServer is ok! ") + new Date());
        }
        else {
            MatrixCtr.setMailIsOk(mailIsNotOk);
            FileSystemWorker.appendObjectToFile(fileForAppend, mailIsNotOk);
        }
    }
    
    @Override
    public String testInput() throws MessagingException {
        stringBuilder.append("POP3").append("\n\n");
        Folder defaultFolder = getInboxFolder();
        defaultFolder.open(Folder.READ_WRITE);
        
        for (Message message : defaultFolder.getMessages()) {
            if (new TForms().fromArray(message.getFrom()).contains(MAIL_IKUDRYASHOV)) {
                stringBuilder.append(message.getSentDate()).append("; from: ").append(new TForms().fromArray(message.getFrom())).append("; Subj: ")
                        .append(message.getSubject()).append("\n");
            }
            message.setFlag(Flags.Flag.DELETED, true);
        }
        
        defaultFolder.close(true);
        return stringBuilder.append("\n\n").toString();
    }
    
    @Override
    public String testOutput() throws MessagingException {
        stringBuilder.append("SMTP").append("\n\n");
        
        SMTPMessage testMessage = new SMTPMessage(MAIL_SESSION);
        Transport sessionTransport = MAIL_SESSION.getTransport();
        sessionTransport.addTransportListener(new NotDeliveredAdapter());
        
        testMessage.setFrom(MAIL_IKUDRYASHOV);
        testMessage.setSubject("test SMTP " + new Date());
        testMessage.setText(stringBuilder.toString());
        
        stringBuilder.append(testMessage.getSender()).append("\n");
        
        sessionTransport.connect();
        sessionTransport.sendMessage(testMessage, new InternetAddress[]{new InternetAddress("scanner@eatmeat.ru")});
        return stringBuilder.append("\n\n").toString();
    }
    
    @Override
    public String testComplex() throws MessagingException {
        this.stringBuilder = new StringBuilder();
        Preferences preferences = AppComponents.getUserPref();
        MAIL_SESSION.getProperties().forEach((k, v)->preferences.put(k.toString(), v.toString()));
        try {
            preferences.sync();
        }
        catch (BackingStoreException e) {
            messageToUser = DBMessenger.getInstance(this.getClass().getSimpleName());
            messageToUser.error(e.getMessage());
        }
        stringBuilder.append(testOutput()).append(" ***SMTP").append("\n\n");
        stringBuilder.append(testInput()).append(" ***POP3").append("\n\n");
        return stringBuilder.toString();
    }
    
    private @NotNull Folder getInboxFolder() throws MessagingException {
        Store mailSessionStore = getMailStore();
        Folder defaultFolder = mailSessionStore.getDefaultFolder();
        URLName folderURLName = defaultFolder.getURLName();
        stringBuilder.append(folderURLName).append("\n");
        for (Folder folder : defaultFolder.list()) {
            if (folder.getName().equalsIgnoreCase(MailPOPTester.INBOX_FOLDER)) {
                defaultFolder = folder;
            }
        }
        return defaultFolder;
    }
    
    private @NotNull Store getMailStore() throws MessagingException {
        Store mailSessionStore = MAIL_SESSION.getStore();
        mailSessionStore.connect(ConstantsFor.SRV_MAIL3, ConstantsFor.USER_SCANNER, ConstantsFor.USER_SCANNER);
        return mailSessionStore;
    }
    
    @Override
    public String toString() {
        
        final StringBuilder sb = new StringBuilder("MailPOPTester{");
        sb.append("MAIL_IKUDRYASHOV=").append(MAIL_IKUDRYASHOV);
        sb.append(", INBOX_FOLDER=").append(INBOX_FOLDER);
        sb.append(", stringBuilder=").append(stringBuilder);
        if (fileForAppend.exists()) {
            sb.append(", mailLog=").append(FileSystemWorker.readFile(fileForAppend.getAbsolutePath()));
        }
        sb.append('}');
        return sb.toString();
    }
    


    private class NotDeliveredAdapter extends TransportAdapter {
        
        
        @Override
        public void messageNotDelivered(TransportEvent e) {
            stringBuilder.append(e.getSource()).append(" : ").append(e.getMessage()).append("\n");
        }
    }
}