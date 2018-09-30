package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.EMailAndDB.MailMessages;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.MailMessage;
import ru.vachok.networker.config.ThreadConfig;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 @since 27.09.2018 (9:24) */
@Service("mailsrv")
public class MailSRV {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailSRV.class.getSimpleName());

    private MailMessage beanMealMessage;

    @Autowired
    public MailSRV(MailMessage beanMealMessage) {
        this.beanMealMessage = beanMealMessage;
        try {
            makeMessage();
        } catch (MessagingException | ExecutionException | TimeoutException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.error(new TForms().fromArray(e.getSuppressed()), e);
            Thread.currentThread().interrupt();
        }
    }

    private void makeMessage() throws MessagingException, InterruptedException, ExecutionException, TimeoutException {
        MailMessage bufferMessage = new MailMessage();
        Message[] messages = mailMesagesDownloader();
        bufferMessage.setChkDate(new Date());
        for (Message m : messages) {
            bufferMessage.setAllRecepHead(new TForms().fromArray(m.getAllRecipients(), true));
            bufferMessage.setFromHead(new TForms().fromArray(m.getFrom(), true));
            bufferMessage.setSentDate(m.getSentDate());
            bufferMessage.setSubj(m.getSubject());
        }
        beanMealMessage.getAllMail().add(bufferMessage);
        LOGGER.info(bufferMessage.toString());
    }

    private Message[] mailMesagesDownloader() throws InterruptedException, ExecutionException, TimeoutException {
        MailMessages mailMessages = new MailMessages();
        ThreadPoolTaskExecutor executor = new ThreadConfig().threadPoolTaskExecutor();
        Future<Message[]> submit = executor.submit(mailMessages);
        Message[] messages = submit.get(30, TimeUnit.SECONDS);
        beanMealMessage.setChkDate(new Date());
        return messages;
    }

    @Override
    public int hashCode() {
        return beanMealMessage != null ? beanMealMessage.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MailSRV mailSRV = (MailSRV) o;

        return beanMealMessage != null ? beanMealMessage.equals(mailSRV.beanMealMessage) : mailSRV.beanMealMessage == null;
    }

    public MailMessage getBeanMealMessage() {
        return beanMealMessage;
    }
}
