package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.HttpServletRequest;


/**
 {@link ErrorController}
 */
@Controller
public class ErrCtr implements ErrorController {

    /*Fields*/
    /**
     Центрировать (левый тэг)
     */
    private static final String H_2_CENTER = "<h2><center>";

    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Центрировать (правый тэг)
     */
    private static final String H_2_CENTER_CLOSE = "</h2></center>";

    /**
     @return путь к ошибке для броузера
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     <b>Страница обработчика ошибок</b>
     Отображает сообщения exception

     @param httpServletRequest the http servlet request
     @param model              {@link Model} . Аттрибуты - <i>eMessage</i>, <i>statcode</i>, <i>2</i>
     @return error.html
     @see TForms
     */
    @GetMapping ("/error")
    public String errHandle(HttpServletRequest httpServletRequest, Model model) {
        Visitor visitor = ConstantsFor.getVis(httpServletRequest);
        Integer statCode = ( Integer ) httpServletRequest.getAttribute("javax.servlet.error.status_code");
        Exception exception = ( Exception ) httpServletRequest.getAttribute("javax.servlet.error.exception");
        model.addAttribute(ConstantsFor.ATT_E_MESSAGE, httpServletRequest
            .getRequestURL() +
            " тут нет того, что ищешь.<br>" +
            H_2_CENTER.replaceAll("2", "4") +
            httpServletRequest
                .getSession()
                .getServletContext()
                .getVirtualServerName() +
            H_2_CENTER_CLOSE.replaceAll("2", "4"));
        model.addAttribute(ConstantsFor.ATT_STATCODE, H_2_CENTER + statCode + H_2_CENTER_CLOSE);
        if(exception!=null){
            MessageToUser eMail = new ESender(ConstantsFor.GMAIL_COM);
            try{
                eMail.errorAlert(exception.toString(), exception.getMessage(), new TForms().fromArray(exception, false) + "\n\n" + visitor.toString());
                LOGGER.error(exception.getMessage(), exception);
            }
            catch(Exception e){
                LOGGER.error(e.getMessage(), e);
            }
            setExcept(model, exception, statCode, httpServletRequest);
        }
        return "error";
    }

    /**
     <b>Модель ошибки</b>

     @param model              {@link Model} модель
     @param exception          {@link Exception} ошибка
     @param statCode           {@link Integer}. Статус ошибки http
     @param httpServletRequest {@link javax.servlet.http.HttpServletRequest} запрос со-стороны пользователя
     @see #errHandle(HttpServletRequest, Model)
     */
    private void setExcept(Model model, Exception exception, Integer statCode, HttpServletRequest httpServletRequest) {
        String eMessage = H_2_CENTER + exception.getMessage() + H_2_CENTER_CLOSE;
        String eLocalizedMessage = H_2_CENTER + exception.getMessage() + H_2_CENTER_CLOSE;
        String err = statCode + " Научно-Исследовательский Институт Химии Удобрений и Ядов";
        String traceStr = new TForms().fromArray(exception, true);

        if(!exception.getMessage().equals(exception.getLocalizedMessage())){
            eMessage = eMessage + eLocalizedMessage;
        }
        if(ConstantsFor.getPcAuth(httpServletRequest)){
            model.addAttribute("stackTrace", traceStr);
        }

        model.addAttribute(ConstantsFor.ATT_E_MESSAGE, eMessage);
        model.addAttribute(ConstantsFor.ATT_STATCODE, H_2_CENTER + statCode + H_2_CENTER_CLOSE);
        model.addAttribute(ConstantsFor.ATT_TITLE, err);
        model.addAttribute("ref", httpServletRequest.getHeader("referer"));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
    }
}
