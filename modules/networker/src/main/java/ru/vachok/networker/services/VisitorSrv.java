package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.logic.DBMessenger;

import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @since 12.09.2018 (9:44)
 */
@Service("visitorSrv")
public class VisitorSrv {

    private static final Logger LOGGER = AppComponents.getLogger();

    public Visitor getVisitor() {
        return visitor;
    }

    private Visitor visitor;

    @Autowired
    public VisitorSrv() {
        this.visitor = new Visitor();
    }

    public Visitor makeVisit(HttpServletRequest request) {
        visitor.setRemAddr(request.getRemoteAddr());
        visitor.setTimeSt(System.currentTimeMillis());
        Runnable visitMaker = () -> {
            MessageToUser viMessageToDB = new DBMessenger();
            viMessageToDB.info(
                new Date(ConstantsFor.START_STAMP) +
                    " by: " + visitor.getRemAddr(),
                request.getSession().getId() + " session ID\n<br>",
                request.getRequestURL() + " getRequestURL\n<br>" +
                    request.getMethod() + " method\n<br>" +
                    TimeUnit.MILLISECONDS
                        .toSeconds(request
                            .getSession().getLastAccessedTime() - request
                            .getSession().getCreationTime()) + " sec spend in application\n<br>" +
                    new TForms().fromEnum(request.getSession().getServletContext().getAttributeNames(), true));
        };
        visitor.setDbInfo(
            new Date(ConstantsFor.START_STAMP) + "\n" +
                " by: " + visitor.getRemAddr() + "\n" +
                request.getSession().getId() + "\n" +
                request.getRequestURL() + " getRequestURL\n" +
                request.getMethod() + " method\n" +
                TimeUnit.MILLISECONDS
                    .toSeconds(request
                        .getSession().getLastAccessedTime() - request
                        .getSession().getCreationTime()) + " sec spend.\n" +
                request.getSession());
        LOGGER.info(visitor.toString());
        ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) new ThreadConfig()
            .threadPoolTaskExecutor();
        threadPoolTaskExecutor.execute(visitMaker);
        return visitor;
    }

    public String uniqUsers() {
        DataConnectTo dataConnectTo = new RegRuMysql();
        String sql = "select distinct msgtype from ru_vachok_networker";
        Connection u0466446Webapp = dataConnectTo.getDefaultConnection("u0466446_webapp");
        try (PreparedStatement p = u0466446Webapp.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            String msg = "";
            while (r.next()) {
                if (r.last()) msg = r.getString("msgtype");
                LOGGER.info(msg);
            }
            return msg;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "Uniq - none.";
    }
}
