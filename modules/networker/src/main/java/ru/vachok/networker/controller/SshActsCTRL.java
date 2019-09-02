// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.net.ssh.*;
import ru.vachok.networker.restapi.MessageToUser;

import javax.servlet.http.HttpServletRequest;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Stream;


/**
 {@link Controller}, для работы с SSH
 
 @since 01.12.2018 (9:58) */
@SuppressWarnings("SameReturnValue")
@Controller
public class SshActsCTRL {
    
    
    private static final String URL_SSHACTS = "/sshacts";
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    private PfLists pfLists;
    
    /**
     {@link SshActs}
     */
    private SshActs sshActs;
    
    @Contract(pure = true)
    @Autowired
    public SshActsCTRL(PfLists acts, SshActs sshActs) {
        this.pfLists = acts;
        this.sshActs = sshActs;
    }
    
    @PostMapping(URL_SSHACTS)
    public String sshActsPOST(@ModelAttribute SshActs sshActsL, Model model, @NotNull HttpServletRequest request) throws AccessDeniedException {
        this.sshActs = sshActsL;
        String pcReq = request.getRemoteAddr().toLowerCase();
        if (getAuthentic(pcReq)) {
            model.addAttribute(ModelAttributeNames.HEAD, pageFooter.getFooter(ModelAttributeNames.HEAD));
            model.addAttribute(ModelAttributeNames.ATT_SSH_ACTS, sshActsL);
            model.addAttribute(ModelAttributeNames.ATT_SSHDETAIL, sshActsL.getPcName());
            return "sshworks";
        }
        else {
            throw new AccessDeniedException(ConstantsFor.NOT_ALLOWED);
        }
    }
    
    @GetMapping(URL_SSHACTS)
    public String sshActsGET(Model model, HttpServletRequest request) throws AccessDeniedException {
        Visitor visitor = UsefulUtilities.getVis(request);
        String pcReq = request.getRemoteAddr().toLowerCase();
        long abs = Math.abs(TimeUnit.SECONDS.toHours((long) LocalTime.parse("18:30").toSecondOfDay() - LocalTime.now().toSecondOfDay()));
        if (0 >= abs) {
            abs = 1;
        }
        
        sshActs.setAllowDomain("");
        sshActs.setDelDomain("");
        sshActs.setUserInput("");
        sshActs.setNumOfHours(String.valueOf(abs));
        sshActs.setInet(pcReq);
        
        if (getAuthentic(pcReq)) {
            model.addAttribute(ModelAttributeNames.TITLE, visitor.getTimeSpend());
            model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
            model.addAttribute(ModelAttributeNames.ATT_SSH_ACTS, sshActs);
            if (request.getQueryString() != null) {
                parseReq(request.getQueryString());
                model.addAttribute(ModelAttributeNames.TITLE, sshActs.getPcName());
                sshActs.setPcName(sshActs.getPcName());
            }
            model.addAttribute(ModelAttributeNames.ATT_SSHDETAIL, sshActs.toString());
            return "sshworks";
        }
        else {
            throw new AccessDeniedException(ConstantsFor.NOT_ALLOWED);
        }
    }
    
    @PostMapping("/allowdomain")
    public String allowPOST(@NotNull @ModelAttribute SshActs sshActsL, @NotNull Model model) throws NullPointerException {
        this.sshActs = sshActsL;
        model.addAttribute(ModelAttributeNames.TITLE, sshActsL.getAllowDomain() + " добавлен");
        model.addAttribute(ModelAttributeNames.ATT_SSH_ACTS, sshActsL);
        model.addAttribute("ok", Objects.requireNonNull(sshActsL.allowDomainAdd(), "No address: " + sshActsL.getAllowDomain()));
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        return "ok";
    }
    
    @PostMapping("/deldomain")
    public String delDomPOST(@NotNull @ModelAttribute SshActs sshActsL, @NotNull Model model) throws NullPointerException {
        this.sshActs = sshActsL;
        model.addAttribute(ModelAttributeNames.TITLE, sshActsL.getDelDomain() + " удалён");
        model.addAttribute(ModelAttributeNames.ATT_SSH_ACTS, sshActsL);
        model.addAttribute("ok", Objects.requireNonNull(sshActsL.allowDomainDel(), "Error. No address: " + sshActsL.getDelDomain()));
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        return "ok";
    }
    
    @PostMapping("/tmpfullnet")
    public String tempFullInetAccess(@NotNull @ModelAttribute SshActs sshActsL, @NotNull Model model) throws UnknownHostException {
        this.sshActs = sshActsL;
        long timeToApply = Long.parseLong(sshActsL.getNumOfHours());
        Future<String> callFuture = Executors.newSingleThreadExecutor().submit((Callable<String>) new TemporaryFullInternet(sshActsL.getUserInput(), timeToApply, "add"));
        String tempInetAnswer = "null";
        try {
            tempInetAnswer = callFuture.get(ConstantsFor.INIT_DELAY, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, this.getClass().getSimpleName());
            messageToUser.error(MessageFormat.format("SshActsCTRL.tempFullInetAccess: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        model.addAttribute(ModelAttributeNames.ATT_SSH_ACTS, sshActsL);
        model.addAttribute(ModelAttributeNames.TITLE, UsefulUtilities.getRuntime());
        model.addAttribute("ok", tempInetAnswer);
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        return "ok";
    }
    
    public void parseReq(@NotNull String queryString) {
        String qStr = " ";
        try {
            sshActs.setPcName(queryString.split("&")[0].replaceAll("pcName=", ""));
            qStr = queryString.split("&")[1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sshActs.setAllFalse();
        }
        if (qStr.equalsIgnoreCase("inet=std")) {
            sshActs.setSquid();
        }
        if (qStr.equalsIgnoreCase("inet=limit")) {
            sshActs.setSquidLimited();
        }
        if (qStr.equalsIgnoreCase("inet=full")) {
            sshActs.setTempFull();
        }
        if (qStr.equalsIgnoreCase("inet=nat")) {
            sshActs.setVipNet();
        }
        String msg = toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshActsCTRL{");
        sb.append("sshActs=").append(sshActs.hashCode());
        sb.append('}');
        return sb.toString();
    }
    
    private boolean getAuthentic(@NotNull String pcReq) {
        return Stream.of("10.10.111.", "10.200.213.85", "10.200.213.200", "0:0:0:0", "172.16.200.", "10.200.214.80", "10.200.213.86").anyMatch(pcReq::contains);
    }
}