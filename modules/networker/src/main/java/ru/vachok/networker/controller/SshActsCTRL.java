// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.accesscontrol.TemporaryFullInternet;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


/**
 {@link Controller}, для работы с SSH
 
 @since 01.12.2018 (9:58) */
@SuppressWarnings("SameReturnValue") @Controller public class SshActsCTRL {
    
    
    private static final String URL_SSHACTS = "/sshacts";
    
    private PfLists pfLists;
    
    /**
     {@link SshActs}
     */
    private SshActs sshActs;
    
    @Autowired public SshActsCTRL(PfLists acts, SshActs sshActs) {
        this.pfLists = acts;
        this.sshActs = sshActs;
    }
    
    @PostMapping(URL_SSHACTS) public String sshActsPOST(@ModelAttribute SshActs sshActs, Model model, HttpServletRequest request) throws AccessDeniedException {
        this.sshActs = sshActs;
        String pcReq = request.getRemoteAddr().toLowerCase();
        if (getAuthentic(pcReq)) {
            model.addAttribute("head", new PageFooter().getHeaderUtext());
            model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
            model.addAttribute(ConstantsFor.ATT_SSHDETAIL, sshActs.getPcName());
            return "sshworks";
        }
        else {
            throw new AccessDeniedException("NOT Allowed!");
        }
    }
    
    @GetMapping(URL_SSHACTS) public String sshActsGET(Model model, HttpServletRequest request) throws AccessDeniedException {
        Visitor visitor = ConstantsFor.getVis(request);
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
            model.addAttribute(ConstantsFor.ATT_TITLE, visitor.getTimeSpend());
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
            if (request.getQueryString() != null) {
                parseReq(request.getQueryString());
                model.addAttribute(ConstantsFor.ATT_TITLE, sshActs.getPcName());
                sshActs.setPcName(sshActs.getPcName());
            }
            model.addAttribute(ConstantsFor.ATT_SSHDETAIL, sshActs.toString());
            return "sshworks";
        }
        else {
            throw new AccessDeniedException("NOT Allowed!");
        }
    }
    
    @PostMapping("/allowdomain") public String allowPOST(@ModelAttribute SshActs sshActs, Model model) throws NullPointerException {
        this.sshActs = sshActs;
        model.addAttribute(ConstantsFor.ATT_TITLE, sshActs.getAllowDomain() + " добавлен");
        model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
        model.addAttribute("ok", Objects.requireNonNull(sshActs.allowDomainAdd(), "No address: " + sshActs.getAllowDomain()));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
    
    @PostMapping("/deldomain") public String delDomPOST(@ModelAttribute SshActs sshActs, Model model) throws NullPointerException {
        this.sshActs = sshActs;
        model.addAttribute(ConstantsFor.ATT_TITLE, sshActs.getDelDomain() + " удалён");
        model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
        model.addAttribute("ok", Objects.requireNonNull(sshActs.allowDomainDel(), "Error. No address: " + sshActs.getDelDomain()));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
    
    @PostMapping("/tmpfullnet") public String tempFullInetAccess(@ModelAttribute SshActs sshActs, Model model) {
        this.sshActs = sshActs;
        long timeToApply = Long.parseLong(sshActs.getNumOfHours());
        model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
        model.addAttribute(ConstantsFor.ATT_TITLE, ConstantsFor.getMemoryInfo());
        model.addAttribute("ok", new TemporaryFullInternet(sshActs.getUserInput(), timeToApply).doAdd());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
    
    public void parseReq(String queryString) {
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
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("SshActsCTRL{");
        sb.append("sshActs=").append(sshActs.hashCode());
        sb.append('}');
        return sb.toString();
    }
    
    private boolean getAuthentic(String pcReq) {
        return Stream.of("10.10.111.", "10.200.213.85", "10.200.213.200", "0:0:0:0", "172.16.200.", "10.200.214.80").anyMatch(pcReq::contains);
    }
}