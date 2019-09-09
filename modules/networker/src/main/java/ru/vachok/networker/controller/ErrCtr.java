// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;


/**
 @see ru.vachok.networker.controller.ErrCtrTest
 */
@Controller
public class ErrCtr implements ErrorController {

    /**
     Центрировать (левый тэг)
     */
    private static final String H_2_CENTER = "<h2><center>";

    /**
     {@link LoggerFactory#getLogger(String)} as {@link ErrCtr#getClass()} {@code getSimpleName}.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrCtr.class.getSimpleName());

    /**
     Центрировать (правый тэг)
     */
    private static final String H_2_CENTER_CLOSE = "</h2></center>";

    /**
     Boiler Plate
     */
    private static final String MAPPING_ERROR = "/error";
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    /**
     <b>Страница обработчика ошибок</b>
     Отображает сообщения exception

     @param httpServletRequest the http servlet request
     @param model              {@link Model} . Аттрибуты - <i>eMessage</i>, <i>statcode</i>, <i>2</i>
     @return error.html
     @see TForms
     */
    @SuppressWarnings("SameReturnValue") @GetMapping(MAPPING_ERROR)
    public static String errHandle(HttpServletRequest httpServletRequest, Model model) {
        Thread.currentThread().setName("ErrCtr.errHandle");
        Visitor visitor = UsefulUtilities.getVis(httpServletRequest);
        Integer statCode = (Integer) httpServletRequest.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) httpServletRequest.getAttribute("javax.servlet.error.exception");
        model.addAttribute(ModelAttributeNames.ATT_E_MESSAGE, httpServletRequest
            .getRequestURL() +
            " тут нет того, что ищешь.<br>" +
            H_2_CENTER.replaceAll("2", "4") +
            httpServletRequest
                .getSession()
                .getServletContext() +
            H_2_CENTER_CLOSE.replaceAll("2", "4"));
        model.addAttribute(ModelAttributeNames.ATT_STATCODE, H_2_CENTER + statCode + H_2_CENTER_CLOSE);
        if (exception != null) {
            try {
                LOGGER.error(exception.getMessage(), exception);
                Thread.currentThread().setName(exception.getClass().getSimpleName());
            }
            catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
            }
            setExcept(model, exception, statCode, httpServletRequest);
        }
        return ConstantsFor.RETURN_ERROR;
    }
    
    /**
     @return путь к ошибке для броузера
     */
    @SuppressWarnings("SuspiciousGetterSetter") @Override
    public String getErrorPath() {
        return MAPPING_ERROR;
    }
    
    /**
     @param request для получения IP
     @return boolean авторизован или нет
     */
    private static boolean getPcAuth(HttpServletRequest request) {
        return request.getRemoteAddr().toLowerCase().contains("0:0:0:0") ||
            request.getRemoteAddr().contains("10.200.213") ||
            request.getRemoteAddr().contains("10.10.111") ||
            request.getRemoteAddr().contains("172.16.200");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ErrCtr{");
        sb.append("H_2_CENTER='").append(H_2_CENTER).append('\'');
        sb.append(", H_2_CENTER_CLOSE='").append(H_2_CENTER_CLOSE).append('\'');
        sb.append(", MAPPING_ERROR='").append(MAPPING_ERROR).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     <b>Модель ошибки</b>

     @param model              {@link Model} модель
     @param exception          {@link Exception} ошибка
     @param statCode           {@link Integer}. Статус ошибки http
     @param httpServletRequest {@link javax.servlet.http.HttpServletRequest} запрос со-стороны пользователя
     @see #errHandle(HttpServletRequest, Model)
     */
    private static void setExcept(Model model, Exception exception, Integer statCode, HttpServletRequest httpServletRequest) {
        Thread.currentThread().setName("ErrCtr.setExcept");
        String eMessage = H_2_CENTER + exception.getMessage() + H_2_CENTER_CLOSE;
        String eLocalizedMessage = H_2_CENTER + exception.getMessage() + H_2_CENTER_CLOSE;
        String err = statCode + " Научно-Исследовательский Институт Химии Удобрений и Ядов";
        String traceStr = new TForms().fromArray(exception, true);
    
        if (getPcAuth(httpServletRequest)) {
            model.addAttribute("stackTrace", traceStr);
        }
    
        model.addAttribute(ModelAttributeNames.ATT_E_MESSAGE, eMessage);
        model.addAttribute(ModelAttributeNames.ATT_STATCODE, H_2_CENTER + statCode + H_2_CENTER_CLOSE);
        model.addAttribute(ModelAttributeNames.TITLE, err);
        model.addAttribute("ref", httpServletRequest.getHeader(ConstantsFor.HEAD_REFERER));
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
    }
}
