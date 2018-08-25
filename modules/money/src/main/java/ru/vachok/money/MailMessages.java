package ru.vachok.money;



import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <h1>Получает сообщения</h1>
 * <p>
 * {@link SpeedRunActualize}
 *
 * @since 25.06.2018 (2:41)
 */
public class MailMessages implements Callable<Message[]> {

    private static final String SOURCE_CLASS = MailMessages.class.getSimpleName();


    private boolean cleanMBox;


    /**
     * <b>Конструктор очистки ящика.</b>
     *
     * @param cleanMBox удалить сообщения = true
     */
    public MailMessages( boolean cleanMBox ) {
        this.cleanMBox = cleanMBox;
        MessageToUser messageToUser = new MessageCons();
        messageToUser.info(SOURCE_CLASS , "cleanMBox is" , cleanMBox + ".");
    }


    /**
     * <b>Конструктор default</b>
     */
    public MailMessages() {
        Logger.getLogger(SOURCE_CLASS).log(Level.INFO , this.getClass().getTypeName());
    }


    /**
     * <b>Получение массива сообщений из почтового ящика bot@</b>
     * <p>
     * {@link SpeedRunActualize}
     * <p>
     * В зависимости от {@link #cleanMBox}, или
     * 1.1 {@link Cleaner#saveToDiskAndDelete(Folder)}
     * 1.1 {@link #getInbox()}
     *
     * @return сообщения. {@link Message}[]
     */
    @Override
    public Message[] call() {
        Message[] messages = new Message[0];
        try {
            if (cleanMBox) Cleaner.saveToDiskAndDelete(getInbox());
            else {
                messages = getInbox().getMessages();
            }
        } catch (MessagingException e) {
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING , String.format("%s%n%n%s" , e.getMessage() , Arrays.toString(e.getStackTrace())));
        }
        return messages;
    }


    /**
     * {@link #call()}
     * <p>
     * Проверяет почту.
     * .1 {@link #getSessionProps()}
     *
     * @return папку Inbox. {@link Folder}
     */
    public Folder getInbox() {
        Properties mailProps = getSessionProps();
        Authenticator authenticator = new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProps.getProperty("user") , mailProps.getProperty("password"));
            }
        };
        Session chkSess = Session.getDefaultInstance(mailProps , authenticator);
        Store store = null;
        try {
            store = chkSess.getStore();
            store.connect(mailProps.getProperty("host") , mailProps.getProperty("user") , mailProps.getProperty("password"));
        } catch (MessagingException e) {
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING , String.format("%s%n%n%s" , e.getMessage() , Arrays.toString(e.getStackTrace())));
        }
        Folder inBox;
        try {
            inBox = Objects.requireNonNull(store).getFolder("Inbox");
            inBox.open(Folder.READ_WRITE);
            Logger.getLogger(Cleaner.class.getSimpleName()).log(Level.INFO , inBox.getMessageCount() + " inbox size");
            return inBox;
        } catch (MessagingException e) {
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING , String.format("%s%n%n%s" , e.getMessage() , Arrays.toString(e.getStackTrace())));
        }
        throw new UnsupportedOperationException("Inbox not available :(");
    }


    /**
     * <b>Получение настроек сессии</b>.
     * {@link #getInbox()}
     * <p>
     * {@link #saveProps(Properties)}
     *
     * @return {@link Properties} от {@link DBRegProperties} {@code mail-regru}
     */
    private Properties getSessionProps() {
        InitProperties initProperties = new DBRegProperties("mail-regru");
        Properties sessionProps = initProperties.getProps();
        sessionProps.setProperty("NewSessionStarted" , new Date().toString());
        saveProps(sessionProps);
        return sessionProps;
    }


    /**
     * <h2>Пробует сохранить настройки в файл и в БД.</h2> {@link #getSessionProps()} .
     * <p>
     * {@value ConstantsFor#APP_NAME} + {@link MailMessages#SOURCE_CLASS}
     *
     * @param sessionProps настройки сокдинения.
     * @see InitProperties
     */
    private void saveProps( Properties sessionProps ) {
        InitProperties initProperties = new FileProps(SOURCE_CLASS);
        initProperties.setProps(sessionProps);
        initProperties = new DBRegProperties("ru_vachok_sr-" + SOURCE_CLASS);
        initProperties.getProps();
        initProperties.setProps(sessionProps);
    }


    /**
     * <h1>Очистка ящика</h1>
     *
     * @since 28.07.2018 (2:55)
     */
    static class Cleaner extends MailMessages {


        static Message[] saveToDiskAndDelete( Folder inbox ) throws MessagingException {
            Message[] mailMes = inbox.getMessages();
            for (Message message : mailMes) {
                if (message.getSubject().toLowerCase().contains("speed:")) message.setFlag(Flags.Flag.DELETED , true);
            }
            Logger.getLogger(Cleaner.class.getSimpleName()).log(Level.INFO , inbox.getMessageCount() + " inbox size");
            inbox.close(true);
            return mailMes;
        }
    }
}