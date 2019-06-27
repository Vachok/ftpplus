// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.mailserver.testserver;


import com.sun.mail.smtp.SMTPMessage;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.controller.MatrixCtr;
import ru.vachok.networker.services.MessageLocal;

import javax.mail.*;
import javax.mail.event.TransportAdapter;
import javax.mail.event.TransportEvent;
import javax.mail.internet.InternetAddress;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @see ru.vachok.networker.mailserver.testserver.MailPOPTesterTest
 @since 27.06.2019 (10:41) */
public class MailPOPTester implements MailTester, Runnable {
    
    
    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());
    
    private StringBuilder stringBuilder = new StringBuilder();
    
    protected static final String MAIL_IKUDRYASHOV = "ikudryashov@eatmeat.ru";
    
    protected static final String INBOX_FOLDER = "inbox";
    
    @SuppressWarnings("FeatureEnvy") @Override
    public void run() {
        try {
            String complexResult = testComplex();
            if (complexResult.contains("from: ikudryashov@eatmeat.ru, ; Subj: test SMTP")) {
                MatrixCtr.setMailIsOk("<center><font color=\"green\">MailServer is ok</font></center> Tested at: " + new Date());
            }
            else {
                MatrixCtr.setMailIsOk("<center><font color=\"red\">MailServer isn't ok</font></center> Tested at: " + new Date());
            }
        }
        catch (MessagingException e) {
            messageToUser.error(e.getMessage());
            MatrixCtr.setMailIsOk("<center><font color=\"red\">MailServer isn't ok</font></center> Tested at: " + new Date());
        }
    }
    
    @Override public String testInput() throws MessagingException {
        stringBuilder.append("POP3").append("\n\n");
        Store mailSessionStore = null;
        try {
            mailSessionStore = MAIL_SESSION.getStore();
            mailSessionStore.connect(ConstantsFor.SRV_MAIL3, ConstantsFor.USER_SCANNER, ConstantsFor.USER_SCANNER);
        }
        catch (MessagingException e) {
            messageToUser.error(e.getMessage());
        }
        Folder defaultFolder = mailSessionStore != null ? mailSessionStore.getDefaultFolder() : null;
        URLName folderURLName = defaultFolder != null ? defaultFolder.getURLName() : null;
        stringBuilder.append(folderURLName).append("\n");
        Folder[] folders = defaultFolder != null ? defaultFolder.list() : new Folder[0];
        for (Folder folder : folders) {
            if (folder.getName().equalsIgnoreCase(MailPOPTester.INBOX_FOLDER)) {
                defaultFolder = folder;
            }
        }
    
        if (defaultFolder != null) {
            defaultFolder.open(Folder.READ_WRITE);
        }
        for (Message message : defaultFolder != null ? defaultFolder.getMessages() : new Message[0]) {
            if (new TForms().fromArray(message.getFrom()).contains(MAIL_IKUDRYASHOV)) {
                stringBuilder.append(message.getSentDate()).append("; from: ").append(new TForms().fromArray(message.getFrom())).append("; Subj: ")
                    .append(message.getSubject()).append("\n");
            }
            message.setFlag(Flags.Flag.DELETED, true);
        }
    
        if (defaultFolder != null) {
            defaultFolder.close(true);
        }
        return stringBuilder.append("\n\n").toString();
    }
    
    @Override public String testOutput() throws MessagingException {
        stringBuilder.append("SMTP").append("\n\n");
        
        SMTPMessage testMessage = new SMTPMessage(MAIL_SESSION);
        Transport sessionTransport = MAIL_SESSION.getTransport();
        sessionTransport.addTransportListener(new TransportAdapter() {
            @Override public void messageNotDelivered(TransportEvent e) {
                stringBuilder.append(e.getSource()).append(" : ").append(e.getMessage()).append("\n");
            }
        });
    
        testMessage.setFrom(MAIL_IKUDRYASHOV);
        testMessage.setSubject("test SMTP " + new Date());
        testMessage.setText(stringBuilder.toString());
        stringBuilder.append(testMessage.getSender()).append("\n");
        sessionTransport.connect();
        sessionTransport.sendMessage(testMessage, new InternetAddress[]{new InternetAddress("scanner@eatmeat.ru")});
        return stringBuilder.append("\n\n").toString();
    }
    
    @Override public String testComplex() throws MessagingException {
        this.stringBuilder = new StringBuilder();
        Preferences preferences = AppComponents.getUserPref();
        MAIL_SESSION.getProperties().forEach((k, v)->{
            preferences.put(k.toString(), v.toString());
        });
        try {
            preferences.sync();
        }
        catch (BackingStoreException e) {
            messageToUser.error(e.getMessage());
        }
        stringBuilder.append(testOutput()).append(" ***SMTP").append("\n\n");
        stringBuilder.append(testInput()).append(" ***POP3").append("\n\n");
        return stringBuilder.toString();
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("MailPOPTester{");
        sb.append("MAIL_IKUDRYASHOV=").append(MAIL_IKUDRYASHOV);
        sb.append(", INBOX_FOLDER=").append(INBOX_FOLDER);
        sb.append(", stringBuilder=").append(stringBuilder);
        sb.append('}');
        return sb.toString();
    }
}
