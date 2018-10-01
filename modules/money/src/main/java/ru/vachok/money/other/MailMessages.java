package ru.vachok.money.other;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.MessageFromInbox;
import ru.vachok.money.services.TForms;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 <h1>Получает сообщения</h1>
 <p>
 {@link SpeedRunActualizeLOC}

 @since 25.06.2018 (2:41) */
public class MailMessages implements Callable<Message[]> {

    /*Fields*/
    private static final String SOURCE_CLASS = MailMessages.class.getSimpleName();

    private static final org.slf4j.Logger LOGGER = ConstantsFor.getLogger();

    private boolean cleanMBox;

    private ESender senderToGmail = new ESender("143500@gmail.com");

    public ESender getSenderToGmail() {
        return senderToGmail;
    }

    /*Instances*/

    /**
     <b>Конструктор очистки ящика.</b>

     @param cleanMBox удалить сообщения = true
     */
    public MailMessages(boolean cleanMBox) {
        this.cleanMBox = cleanMBox;
        MessageToUser messageToUser = new MessageCons();
        messageToUser.info(SOURCE_CLASS, "cleanMBox is", cleanMBox + ".");
    }

    /**
     <b>Конструктор default</b>
     */
    public MailMessages() {
        this.cleanMBox = false;
        getMailBox(cleanMBox);
        Logger.getLogger(SOURCE_CLASS).log(Level.INFO, this.getClass().getTypeName());
    }

    public String getMailBox(boolean cleanMBox) {
        MessageFromInbox messageFromInbox = new MessageFromInbox();
        StringBuilder stringBuilder = new StringBuilder();
        try{
            Folder folder = getInbox();
            Message[] messages = folder.getMessages();
            for(Message m : messages){
                String fromHeader = new TForms().toStringFromArray(m.getFrom());
                stringBuilder.append(fromHeader);
                messageFromInbox.setFrom(fromHeader);
                Date receivedDate = m.getReceivedDate();
                stringBuilder.append(" ").append(receivedDate).append("<br>");
                messageFromInbox.setWhen(receivedDate.getTime());
                String mSubject = m.getSubject();
                stringBuilder.append(mSubject).append("<br>");
                messageFromInbox.setSubject(mSubject);
                messageFromInbox.setContent(m.getContent().toString());
                if(cleanMBox){
                    m.setFlag(Flags.Flag.DELETED, true);
                }

            }
            folder.close(true);
        }
        catch(MessagingException | IOException e){
            LOGGER.error(SOURCE_CLASS + ".getMailbox\n" + e.getMessage(), e);
            return e.getMessage() + "<br>" + TForms.toStringFromArray(e.getStackTrace());
        }

        return stringBuilder.toString();
    }

    /**
     {@link #call()}
     <p>
     Проверяет почту.
     .1 {@link #getSessionProps()}

     @return папку Inbox. {@link Folder}
     */
    public Folder getInbox() {
        Properties mailProps = getSessionProps();
        Authenticator authenticator = new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProps.getProperty("user"), mailProps.getProperty("password"));
            }
        };
        Session chkSess = Session.getDefaultInstance(mailProps, authenticator);
        Store store = null;
        try{
            store = chkSess.getStore();
            store.connect(mailProps.getProperty("host"), mailProps.getProperty("user"), mailProps.getProperty("password"));
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format("%s%n%n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        Folder inBox = null;
        try{
            inBox = Objects.requireNonNull(store).getFolder("Inbox");
            inBox.open(Folder.READ_WRITE);
            Logger.getLogger(Cleaner.class.getSimpleName()).log(Level.INFO, inBox.getMessageCount() + " inbox size");
            return inBox;
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format("%s%n%n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        return inBox;
    }


    /**
     <b>Получение настроек сессии</b>.
     {@link #getInbox()}
     <p>
     {@link #saveProps(Properties)}

     @return {@link Properties} от {@link DBRegProperties} {@code mail-regru}
     */
    private Properties getSessionProps() {
        InitProperties initProperties = new DBRegProperties("mail-regru");
        Properties sessionProps = initProperties.getProps();
        sessionProps.setProperty("NewSessionStarted", new Date().toString());
        saveProps(sessionProps);
        return sessionProps;
    }


    /**
     <b>Пробует сохранить настройки в файл и в БД.</h2> {@link #getSessionProps()} </b>.
     <p>
     + {@link MailMessages#SOURCE_CLASS}

     @param sessionProps настройки сокдинения.
     @see InitProperties
     */
    private void saveProps(Properties sessionProps) {
        InitProperties initProperties = new FileProps(SOURCE_CLASS);
        initProperties.setProps(sessionProps);
        initProperties = new DBRegProperties("ru_vachok_sr-" + SOURCE_CLASS);
        initProperties.getProps();
        initProperties.setProps(sessionProps);
    }

    /**
     <b>Получение массива сообщений из почтового ящика bot@</b>
     <p>
     {@link SpeedRunActualizeLOC}
     <p>
     В зависимости от {@link #cleanMBox}, или
     1.1 {@link Cleaner#saveToDiskAndDelete(Folder)}
     1.1 {@link #getInbox()}

     @return сообщения. {@link Message}[]
     */
    @Override
    public Message[] call() {
        Message[] messages = new Message[0];
        try{
            if(cleanMBox){
                Cleaner.saveToDiskAndDelete(getInbox());
            }
            else{
                messages = getInbox().getMessages();
            }
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format("%s%n%n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        if(messages.length > 0){
            for(Message m : messages){
                try{
                    senderToGmail.info(new TForms().toStringFromArray(m.getFrom()), m.getSubject(), m.getContent().toString());
                }
                catch(MessagingException | IOException e){
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return messages;
    }

    /*END FOR CLASS*/

    /**
     <h1>Очистка ящика</h1>

     @since 28.07.2018 (2:55)
     */
    static class Cleaner extends MailMessages {


        static Message[] saveToDiskAndDelete(Folder inbox) throws MessagingException {
            Message[] mailMes = inbox.getMessages();
            for(Message message : mailMes){
                if(message.getSubject().toLowerCase().contains("speed:")){
                    message.setFlag(Flags.Flag.DELETED, true);
                }
            }
            Logger.getLogger(Cleaner.class.getSimpleName()).log(Level.INFO, inbox.getMessageCount() + " inbox size");
            inbox.close(true);
            return mailMes;
        }
    }
}