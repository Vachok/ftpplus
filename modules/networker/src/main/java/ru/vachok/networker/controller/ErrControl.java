package ru.vachok.networker.controller;


import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.AccessDeniedException;


/**
 * The type Err control.
 */
@Controller
public class ErrControl implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * Err handle string.
     *
     * @param httpServletRequest the http servlet request
     * @return the string
     */
    @GetMapping("/error")
    public String errHandle(HttpServletRequest httpServletRequest, Model model) {
        Integer statCode = (Integer) httpServletRequest.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) httpServletRequest.getAttribute("javax.servlet.error.exception");
        String eMessage = "Скорее всего, этой страницы просто нет, " + httpServletRequest.getRemoteAddr() + ".";
        String err = "К сожалению, вынужден признать, тут ошибка... " + statCode;
        if (exception != null) {
            eMessage = exception.getMessage();
            StackTraceElement[] stackTrace = exception.getStackTrace();
            model.addAttribute("eMessage", eMessage);
            model.addAttribute("stackTrace", stackTrace);
        }
        model.addAttribute("eMessage", eMessage);
        model.addAttribute("err", err);
        return "error";
    }

    /**
     * Exit app.
     *
     * @param httpServletRequest the http servlet request
     * @throws IOException the io exception
     */
    @GetMapping("/stop")
    public String exitApp(HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {
        String q = httpServletRequest.getQueryString();
        String userIp = httpServletRequest.getRemoteAddr();
        if ((
            userIp.contains(ConstantsFor.no0027EatmeatRu) ||
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
