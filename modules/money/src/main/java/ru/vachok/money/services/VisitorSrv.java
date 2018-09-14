package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.other.FileMessages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 14.09.2018 (19:48) */
@Service
public class VisitorSrv {

    /*Fields*/

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private static final AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;

    /**
     Simple Name класса, для поиска настроек
     */

    private HttpServletRequest request;

    private HttpServletResponse response;

    public void makeVisit(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = request.getSession().getId();
        String msg = "SET NEW Session ID" + request.getSession().getId() + " sessionID = " + sessionId;
        LOGGER.warn(msg);
    }

    private void toFile() {
        MessageToUser messageToUser = new FileMessages();
        messageToUser.info("http", new Date(System.currentTimeMillis()).toString() + " " +
            (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - ConstantsFor.START_STAMP)) +
            " elapsed", request.getRemoteAddr() +
            ":" +
            response.getStatus() +
            new TForms().toStringFromArray(response.getHeaderNames()));
    }

    private void toMail() {
        AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;
        MailMessages mailBean = ctx.getBean(MailMessages.class);
        new Thread(() -> mailBean.getSenderToGmail().infoNoTitles(
            request.getRemoteAddr() +
                ":" +
                response.getStatus() +
                new TForms().toStringFromArray(response.getHeaderNames()))).start();
        Thread.currentThread().interrupt();
    }

}