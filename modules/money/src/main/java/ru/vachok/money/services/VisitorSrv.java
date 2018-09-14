package ru.vachok.money.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.Visitor;
import ru.vachok.money.config.AppComponents;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 14.09.2018 (19:48) */
@Service
public class VisitorSrv {

    /*Fields*/

    /**
     {@link }
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Simple Name класса, для поиска настроек
     */
    private static Visitor visitor = new Visitor();

    private HttpServletRequest request;

    private HttpServletResponse response;

    public void makeVisit(HttpServletRequest request, HttpServletResponse response) {
        visitor.setRequest(request);
        visitor.setResponse(response);
        LOGGER.info("OK");
        AnnotationConfigApplicationContext ctx = ConstantsFor.CONTEXT;
        MailMessages mailBean = ctx.getBean(MailMessages.class);
        new Thread(() -> mailBean.getSenderToGmail().infoNoTitles(request + "\n" + response)).start();
        Thread.currentThread().interrupt();
    }

}