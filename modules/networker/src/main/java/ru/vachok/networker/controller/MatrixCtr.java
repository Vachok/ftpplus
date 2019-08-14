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
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.abstr.NetKeeper;
import ru.vachok.networker.accesscontrol.MatrixSRV;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.info.HTMLGeneration;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.info.TvPcInformation;
import ru.vachok.networker.services.SimpleCalculator;
import ru.vachok.networker.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    
    private InformationFactory informationFactory = new TvPcInformation();
    
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
        this.matrixSRV = matrixSRV;
    }
    
    public static void setMailIsOk(String mailIsOk) {
        MatrixCtr.mailIsOk = mailIsOk;
    }
    
    @GetMapping("/")
    public String getFirst(final HttpServletRequest request, Model model, @NotNull HttpServletResponse response) {
        this.visitorInst = UsefulUtilities.getVis(request);
        qIsNull(model, request);
        model.addAttribute(ModelAttributeNames.ATT_HEAD, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_HEAD));
        model.addAttribute(ModelAttributeNames.ATT_DEVSCAN,
            "Since: " + AppComponents.getUserPref().get(FileNames.FILENAME_PTV, "No date") + informationFactory
                .getInfoAbout("tv") + NetKeeper.getCurrentProvider() + "<br>" + mailIsOk);
        response.addHeader(ConstantsFor.HEAD_REFRESH, "120");
        return "starting";
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
     3. {@link PageGenerationHelper#getFooterUtext()}, 4. new {@link PageGenerationHelper}, 5. {@link Visitor#toString()}. Компонент модели {@link ModelAttributeNames#ATT_FOOTER} <br>
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
        model.addAttribute(ModelAttributeNames.ATT_HEAD, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_HEAD));
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER) + "<p>" + visitorInst);
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
    
    private static String getUserPC(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
    
    private static String whoisStat(String workPos, Model model) {
        WhoIsWithSRV whoIsWithSRV = new WhoIsWithSRV();
        workPos = workPos.split(": ")[1].trim();
        String attributeValue = whoIsWithSRV.whoIs(workPos);
        model.addAttribute(ModelAttributeNames.ATT_WHOIS, attributeValue);
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        model.addAttribute(ModelAttributeNames.ATT_HEAD, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_HEAD));
        return ConstantsFor.BEANNAME_MATRIX;
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
    private static String timeStamp(@ModelAttribute SimpleCalculator simpleCalculator, Model model, String workPos) {
        model.addAttribute(ConstantsFor.BEANNAME_CALCULATOR, simpleCalculator);
        model.addAttribute(ATT_DINNER, simpleCalculator.getStampFromDate(workPos));
        return "redirect:/calculate";
    }
    
    private void qIsNull(Model model, HttpServletRequest request) {
        String userPC = getUserPC(request);
        String userIP = userPC + ":" + request.getRemotePort() + "<-" + TimeUnit.SECONDS.toDays((System.currentTimeMillis() / 1000) - UsefulUtilities.getMyTime());
        if (!UsefulUtilities.isPingOK()) {
            userIP = "ping to srv-git.eatmeat.ru is " + false;
        }
        model.addAttribute("yourip", userIP);
        model.addAttribute(ConstantsFor.BEANNAME_MATRIX, new MatrixSRV());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        if (getUserPC(request).toLowerCase().contains(OtherKnownDevices.DO0213_KUDR) ||
            getUserPC(request).toLowerCase().contains("0:0:0:0")) {
            model.addAttribute(ModelAttributeNames.ATT_VISIT, "16.07.2019 (14:48) NOT READY");
        }
    }
    
    private String matrixAccess(String workPos, Model model) {
        String workPosition = this.matrixSRV.searchAccessPrincipals(workPos);
        this.matrixSRV.setWorkPos(workPosition);
        model.addAttribute("ok", workPosition);
        model.addAttribute(ModelAttributeNames.ATT_HEAD, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_HEAD));
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        return "ok";
    }
}