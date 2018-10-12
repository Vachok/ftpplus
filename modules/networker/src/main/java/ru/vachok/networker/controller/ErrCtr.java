package ru.vachok.networker.controller;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.concurrent.TimeUnit;


/**
 The type Err control.
 */
@Controller
public class ErrCtr implements ErrorController {

    private static final String H_2_CENTER = "<h2><center>";

    private static final String H_2_CENTER_CLOSE = "</h2></center>";

    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     Err handle H_2_CENTER.

     @param httpServletRequest the http servlet request
     @return the H_2_CENTER
     */
    @GetMapping("/error")
    public String errHandle(HttpServletRequest httpServletRequest, Model model) {
        Integer statCode = (Integer) httpServletRequest.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) httpServletRequest.getAttribute("javax.servlet.error.exception");
        model.addAttribute("eMessage", httpServletRequest
            .getRequestURL() +
            " тут нет того, что ищешь.<br>" +
            H_2_CENTER.replaceAll("2", "4") +
            httpServletRequest
                .getSession()
                .getServletContext()
                .getVirtualServerName() +
            H_2_CENTER_CLOSE.replaceAll("2", "4"));
        model.addAttribute("statcode", H_2_CENTER + statCode + H_2_CENTER_CLOSE);

        if (exception != null) {
            String eMessage = H_2_CENTER + exception.getMessage() + H_2_CENTER_CLOSE;
            String eLocalizedMessage = H_2_CENTER + exception.getMessage() + H_2_CENTER_CLOSE;
            String err = "Научно-Исследовательский Институт Химии Удобрений и Ядов" + statCode;
            String traceStr = new TForms().fromArray(exception, false);

            long lastAccessedTime = httpServletRequest.getSession().getLastAccessedTime();

            if (!exception.getMessage().equals(exception.getLocalizedMessage())) eMessage = eMessage + eLocalizedMessage;

            if (ConstantsFor.getPcAuth(httpServletRequest)) model.addAttribute("stackTrace", traceStr);

            model.addAttribute("eMessage", eMessage);
            model.addAttribute("statcode", H_2_CENTER + statCode + H_2_CENTER_CLOSE);
            model.addAttribute("title", TimeUnit.MILLISECONDS
                .toSeconds(lastAccessedTime - httpServletRequest.getSession().getCreationTime()) + " sec. sess.");
            model.addAttribute("ref", httpServletRequest.getHeader("referer"));
            model.addAttribute("err", err);
            model.addAttribute("footer", new PageFooter().getFooterUtext());
        }
        return "error";
    }

    /**
     Exit app.

     @param httpServletRequest the http servlet request
     @throws IOException the io exception
     */
    @GetMapping("/stop")
    public String exitApp(HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {
        String q = httpServletRequest.getQueryString();
        String userIp = httpServletRequest.getRemoteAddr();
        if ((
            userIp.contains(ConstantsFor.NO0027) ||
                userIp.contains("10.10.111") ||
                userIp.contains("172.26.43") ||
                userIp.contains("0:0:0:0:0"))) {
            if (q != null) {
                if (q.contains("full")) Runtime.getRuntime().exec("shutdown /p /f");
                if (q.contains("restart")) Runtime.getRuntime().exec("shutdown /r /f");
            } else {
                System.exit(0);
                return "redirect:srv-sd.eatmeat.ru/otrs/index.pl?Action=AgentTicketSearch;Subaction=Search;TakeLastSearch=1;SaveProfile=1;Profile=Новые предъявы";

            }
        } else {
            response.setStatus(403);
            throw new AccessDeniedException("Access denied!");
        }

        return "redirect:/ok";
    }
}
