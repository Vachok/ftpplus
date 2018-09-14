package ru.vachok.money.ctrls;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.MailMessages;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.other.CookieMaker;
import ru.vachok.money.services.TForms;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;


/**
 * @since 20.08.2018 (17:08)
 */
@Controller
public class Index {

    /*Fields*/
    private static final String SOURCE_CLASS = Index.class.getSimpleName();

    private static final Connection u0466446Webapp = new RegRuMysql().getDefaultConnection("u0466446_webapp");

    private static final Logger LOGGER = AppComponents.getLogger();

    private static DataConnectTo dataConnectTo = new RegRuMysql();

    private static CookieMaker cookieMaker = new CookieMaker();

    @GetMapping("/")
    public String indexString(HttpServletRequest request, HttpServletResponse response, Model model) {
        model.addAttribute("title", request.getRemoteAddr() + " " + response.getStatus());
        return "index-start";
    }

    private String getMailBox(HttpServletRequest request) {
        MailMessages mailMessages = new MailMessages();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Folder folder = mailMessages.getInbox();
            Message[] messages = folder.getMessages();
            for (Message m : messages) {
                stringBuilder.append(new TForms().toStringFromArray(m.getFrom()));
                stringBuilder.append(" ").append(m.getReceivedDate()).append("<br>");
                stringBuilder.append(m.getSubject()).append("<br>");
                if (request.getQueryString() != null) {
                    if (request.getQueryString().contains("clean")) {
                        m.setFlag(Flags.Flag.DELETED, true);
                    }
                }
            }
            folder.close(true);
        } catch (MessagingException e) {
            LOGGER.error(SOURCE_CLASS + ".getMailbox\n" + e.getMessage(), e);
            return e.getMessage() + "<br>" + TForms.toStringFromArray(e.getStackTrace());
        }

        return stringBuilder.toString();
    }
    /*Private metsods*/
}
