package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.logic.DBMessenger;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @since 12.09.2018 (9:44)
 */
@Service("visitorSrv")
public class VisitorSrv {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static AnnotationConfigApplicationContext appCtx = IntoApplication.getAppCtx();

    public Visitor makeVisit(HttpServletRequest request) {
        Visitor visitor = appCtx.getBean(Visitor.class);
        visitor.setRemAddr(request.getRemoteAddr());
        visitor.setTimeSt(System.currentTimeMillis());
        new Thread(() -> {
            MessageToUser viMessageToDB = new DBMessenger();
            viMessageToDB.info(
                new Date(appCtx.getStartupDate()) + " you have a visit to: " + request.getLocalAddr(),
                "by: " + visitor.getRemAddr(),
                request.getRequestURL() + " getRequestURL\n" + request.getScheme());
            LOGGER.info(visitor.toString());
        });
        return visitor;
    }
}
