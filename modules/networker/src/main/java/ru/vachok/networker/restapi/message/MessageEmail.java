package ru.vachok.networker.restapi.message;


import com.sun.mail.smtp.SMTPMessage;
import org.jetbrains.annotations.Contract;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import java.text.MessageFormat;
import java.util.Properties;


/**
 @see MessageEmailTest
 @since 22.11.2019 (9:14) */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class MessageEmail extends MailMessages implements MessageToUser {
    
    
    private String headerMsg;
    
    private String titleMsg;
    
    private String bodyMsg;
    
    @Override
    public void setHeaderMsg(String headerMsg) {
        this.headerMsg = headerMsg;
    }
    
    @Contract(pure = true)
    MessageEmail(String headerMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = "Message from Networker ";
    }
    
    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        error(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        sendEmail();
    }
    
    @Override
    public void infoNoTitles(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.titleMsg = titleMsg + ": information";
        info(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void info(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.titleMsg = titleMsg + ": information";
        info(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.titleMsg = MessageFormat.format("{0}: ERROR", titleMsg);
        error(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void error(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        sendEmail();
    }
    
    @Override
    public void warn(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        warning(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warn(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.titleMsg = MessageFormat.format("{0}: Warning", titleMsg);
        warning(headerMsg, titleMsg, bodyMsg);
    }
    
    @Override
    public void warning(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        sendEmail();
    }
    
    @Override
    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        this.titleMsg = MessageFormat.format("{0}: Warning", titleMsg);
        warning(headerMsg, titleMsg, bodyMsg);
    }
    
    private void sendEmail() {
        Session session = Session.getDefaultInstance(getProps(), new AuthMail());
        SMTPMessage smtpMessage = new SMTPMessage(session);
        try {
            Address address = new InternetAddress("vachok@vachok.ru");
            Transport sessionTransport = session.getTransport();
            smtpMessage.setFrom("bot@chess.vachok.ru");
            smtpMessage.setSubject(MessageFormat.format("{0}: {1}", headerMsg, titleMsg));
            smtpMessage.setText(bodyMsg);
            smtpMessage.setRecipient(Message.RecipientType.TO, address);
            sessionTransport.connect("smtp.chess.vachok.ru", "bot@chess.vachok.ru", "S15cQFO8kk50FKj");
            sessionTransport.sendMessage(smtpMessage, new Address[]{address});
            sessionTransport.close();
        }
        catch (MessagingException e) {
            e.printStackTrace();
        }
        finally {
            System.out.println("smtpMessage = " + smtpMessage.getEnvelopeFrom());
        }
    }
    
    private Properties getProps() {
        InitProperties initProperties = new DBRegProperties("mail-regru");
        return initProperties.getProps();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageEmail{");
        sb.append("titleMsg='").append(titleMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", bodyMsg='").append(bodyMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    private class AuthMail extends Authenticator {
        
        
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("bot@chess.vachok.ru", "S15cQFO8kk50FKj");
        }
    }
}
