// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.SimpleCalculator;
import ru.vachok.networker.componentsrepo.services.WhoIsWithSRV;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.info.InformationFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 @see  ru.vachok.networker.controller.MatrixCtrTest
 @since 07.09.2018 (0:35) */
@Controller
public class MatrixCtr {
    
    /**
     /matrix
     */
    private static final String GET_MATRIX = "/matrix";
    
    /**
     dinner
     */
    private static final String ATT_DINNER = "dinner";
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TV);
    
    private static String mailIsOk = ConstantsFor.STR_FALSE;
    
    /**
     {@link MatrixSRV}
     */
    private MatrixSRV matrixSRV;
    
    /**
     {@link Visitor}
     */
    private Visitor visitorInst;
    
    /**
     {@link System#currentTimeMillis()}. Время инициализации класса.
     */
    private long metricMatrixStartLong = System.currentTimeMillis();
    
    @Autowired
    public MatrixCtr(MatrixSRV matrixSRV) {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        this.matrixSRV = matrixSRV;
    }
    
    public static void setMailIsOk(String mailIsOk) {
        MatrixCtr.mailIsOk = mailIsOk;
    }
    
    @GetMapping("/")
    public String getFirst(final HttpServletRequest request, Model model, @NotNull HttpServletResponse response) {
        this.visitorInst = UsefulUtilities.getVis(request);
        qIsNull(model, request);
        model.addAttribute(ModelAttributeNames.HEAD, PAGE_FOOTER.getFooter(ModelAttributeNames.HEAD));
        model.addAttribute(ModelAttributeNames.ATT_DEVSCAN,
            "Since: " + AppComponents.getUserPref().get(FileNames.PING_TV, "No date") + informationFactory
                .getInfoAbout("tv") + NetKeeper.getCurrentProvider() + "<br>" + mailIsOk);
        response.addHeader(ConstantsFor.HEAD_REFRESH, "120");
        return ConstantsFor.STARTING;
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @PostMapping(GET_MATRIX)
    public String getWorkPosition(@ModelAttribute(ConstantsFor.BEANNAME_MATRIX) MatrixSRV matrixSRV, Model model) {
        this.matrixSRV = matrixSRV;
        String workPos = matrixSRV.getWorkPos();
        if (workPos.toLowerCase().contains("whois:")) {
            return whoisStat(workPos, model);
        }
        else if (Stream.of(ConstantsFor.COMMAND_CALCTIME, ConstantsFor.COMMAND_CALCTIMES, "t:", "T:").anyMatch(s->workPos.toLowerCase().contains(s))) {
            timeStamp(new SimpleCalculator(), model, workPos);
        }
        else {
            return matrixAccess(workPos, model);
        }
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    /**
     Вывод результата.
     <p>
     1. {@link UsefulUtilities#getVis(HttpServletRequest)}. Запишем визит ({@link Visitor}) <br>
     2. {@link MatrixSRV#getWorkPos()}. Пользовательский ввод. <br>
     3. {@link PageGenerationHelper#getFooterUtext()}, 4. new {@link PageGenerationHelper}, 5. {@link Visitor#toString()}. Компонент модели {@link ModelAttributeNames#FOOTER} <br>
     6. {@link MatrixSRV#getCountDB()}. Компонент {@code headtitle}
     <p>
     
     @param request {@link HttpServletRequest}
     @param response {@link HttpServletResponse}
     @param model {@link Model}
     @return {@link ConstantsFor#BEANNAME_MATRIX}.html
     
     @throws IOException обработка {@link HttpServletResponse#sendError(int, java.lang.String)}
     */
    @GetMapping(GET_MATRIX)
    public String showResults(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        this.visitorInst = UsefulUtilities.getVis(request);
        model.addAttribute(ConstantsFor.BEANNAME_MATRIX, matrixSRV);
        String workPos;
        try {
            workPos = matrixSRV.getWorkPos();
        }
        catch (NullPointerException e) {
            response.sendError(139, "");
    
            throw new IllegalStateException("<br>Строка ввода должности не инициализирована!<br>" +
                this.getClass().getName() + "<br>");
        }
        model.addAttribute(ModelAttributeNames.WORKPOS, workPos);
        model.addAttribute(ModelAttributeNames.HEAD, PAGE_FOOTER.getFooter(ModelAttributeNames.HEAD));
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER) + "<p>" + visitorInst);
        model.addAttribute("headtitle", matrixSRV.getCountDB() + " позиций   " + TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - ConstantsFor.START_STAMP) + " getUpTime");
        metricMatrixStartLong = System.currentTimeMillis() - metricMatrixStartLong;
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MatrixCtr{");
        sb.append("currentProvider='").append(NetKeeper.getCurrentProvider()).append('\'');
        sb.append(", metricMatrixStartLong=").append(new Date(metricMatrixStartLong));
        sb.append('}');
        return sb.toString();
    }
    
    private static String getUserPC(@NotNull HttpServletRequest request) {
        return request.getRemoteAddr();
    }
    
    private void qIsNull(Model model, HttpServletRequest request) {
        String userIP = MessageFormat
            .format("{0}<-{1}|CPU {2}", UsefulUtilities.getUpTime(), TimeUnit.SECONDS
                .toDays((System.currentTimeMillis() / 1000) - UsefulUtilities.getMyTime()), UsefulUtilities.getTotCPUTime());
        if (!UsefulUtilities.isPingOK()) {
            userIP = "ping to srv-git.eatmeat.ru is " + false;
        }
        model.addAttribute("yourip", userIP);
        model.addAttribute(ConstantsFor.BEANNAME_MATRIX, new MatrixSRV());
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
        if (getUserPC(request).toLowerCase().contains(OtherKnownDevices.DO0213_KUDR) ||
                getUserPC(request).toLowerCase().contains("0:0:0:0")) {
            model.addAttribute(ModelAttributeNames.ATT_VISIT, "16.07.2019 (14:48) NOT READY");
        }
    }
    
    /**
     Перевод времени из long в {@link Date} и обратно.
     <p>
     1. {@link SimpleCalculator#getStampFromDate(java.lang.String)} метод перевода.
     <p>
 
     @param simpleCalculator {@link SimpleCalculator}
     @param model {@link Model}
     @param workPos {@link MatrixSRV#getWorkPos()}
     @return redirect:/calculate
     */
    @SuppressWarnings("UnusedReturnValue")
    private static @NotNull String timeStamp(@ModelAttribute SimpleCalculator simpleCalculator, @NotNull Model model, String workPos) {
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ATT_DINNER, simpleCalculator.getStampFromDate(workPos));
        return "redirect:/calculate";
    }
    
    private static String whoisStat(String workPos, @NotNull Model model) {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        workPos = workPos.split(": ")[1].trim();
        String attributeValue = whoIsWithSRV.whoIs(workPos);
        model.addAttribute(ModelAttributeNames.ATT_WHOIS, attributeValue);
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
        model.addAttribute(ModelAttributeNames.HEAD, PAGE_FOOTER.getFooter(ModelAttributeNames.HEAD));
        return ConstantsFor.BEANNAME_MATRIX;
    }
    
    private @NotNull String matrixAccess(String workPos, @NotNull Model model) {
        String workPosition = this.matrixSRV.searchAccessPrincipals(workPos);
        this.matrixSRV.setWorkPos(workPosition);
        model.addAttribute("ok", workPosition);
        model.addAttribute(ModelAttributeNames.HEAD, PAGE_FOOTER.getFooter(ModelAttributeNames.HEAD));
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
        return "ok";
    }
}