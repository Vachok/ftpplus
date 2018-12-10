package ru.vachok.networker.accesscontrol;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 SSH-actions class

 @since 29.11.2018 (13:01) */
@Service("sshActs")
public class SshActs {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String PAGE_NAME = "sshworks";

    private static final String AT_NAME_SSHDETAIL = "sshdetail";

    private static final String AT_NAME_SSHACTS = "sshActs";

    private String pcName;

    private String inet;

    private String allowDomain;

    private String comment;

    private String ipAddrOnly;

    public void setIpAddrOnly(String ipAddrOnly) {
        this.ipAddrOnly = ipAddrOnly;
    }

    public String getAllowDomain() {
        return allowDomain;
    }

    public void setAllowDomain(String allowDomain) {
        this.allowDomain = allowDomain;
    }

    private boolean squid;

    private boolean squidLimited;

    private boolean tempFull;

    private boolean vipNet;

    public String getPcName() {
        return pcName;
    }

    public void setPcName(String pcName) {
        if (pcName.contains(".eatmeat.ru")) this.pcName = pcName;
        else this.pcName = new NameOrIPChecker().checkPat(pcName);
    }

    public String getInet() {
        return inet;
    }

    public boolean isSquid() {
        return squid;
    }

    public boolean isSquidLimited() {
        return squidLimited;
    }

    public boolean isTempFull() {
        return tempFull;
    }

    public boolean isVipNet() {
        return vipNet;
    }

    /**
     Парсинг запроса HTTP
     <p>
     Usages: {@link SshActsCTRL#sshActsGET(Model, HttpServletRequest)} Uses: {@link #toString()}

     @param queryString {@link HttpServletRequest#getQueryString()}
     */
    private void parseReq(String queryString) {
        String qStr = " ";
        try {
            this.pcName = queryString.split("&")[0].replaceAll("pcName=", "");
            qStr = queryString.split("&")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            setAllFalse();
        }
        if (qStr.equalsIgnoreCase("inet=std")) setSquid();
        if (qStr.equalsIgnoreCase("inet=limit")) setSquidLimited();
        if (qStr.equalsIgnoreCase("inet=full")) setTempFull();
        if (qStr.equalsIgnoreCase("inet=nat")) setVipNet();
        String msg = toString();
        LOGGER.warn(msg);
    }

    private void setAllFalse() {
        this.squidLimited = false;
        this.squid = false;
        this.tempFull = false;
        this.vipNet = false;
    }

    private void setSquid() {
        this.squid = true;
        this.vipNet = false;
        this.tempFull = false;
        this.squidLimited = false;
    }

    private void setSquidLimited() {
        this.squid = false;
        this.vipNet = false;
        this.tempFull = false;
        this.squidLimited = true;
    }

    private void setTempFull() {
        this.tempFull = true;
        this.squid = false;
        this.vipNet = false;
        this.squidLimited = false;
    }

    private void setVipNet() {
        this.vipNet = true;
        this.squid = false;
        this.tempFull = false;
        this.squidLimited = false;
    }

    private void setInet(String queryString) {
        this.inet = queryString;
    }

    private String execByWhatListSwitcher(int whatList, boolean iDel) {
        if (iDel) {
            return "sudo grep -v '" + Objects.requireNonNull(pcName) + "' /etc/pf/vipnet > /etc/pf/vipnet_tmp;sudo grep -v '" + Objects.requireNonNull(ipAddrOnly) + "' /etc/pf/vipnet > /etc/pf/vipnet_tmp;sudo cp /etc/pf/vipnet_tmp /etc/pf/vipnet;sudo pfctl -f /etc/srv-nat.conf;sudo grep -v '" + Objects.requireNonNull(ipAddrOnly) + "' /etc/pf/squid > /etc/pf/squid_tmp;sudo cp /etc/pf/squid_tmp /etc/pf/squid;sudo grep -v '" + Objects.requireNonNull(ipAddrOnly) + "' /etc/pf/squidlimited > /etc/pf/squidlimited_tmp;sudo cp /etc/pf/squidlimited_tmp /etc/pf/squidlimited;sudo grep -v '" + Objects.requireNonNull(ipAddrOnly) + "' /etc/pf/tempfull > /etc/pf/tempfull_tmp;sudo cp /etc/pf/tempfull_tmp /etc/pf/tempfull;sudo squid -k reconfigure;sudo /etc/initpf.fw";
        } else {
            this.comment = Objects.requireNonNull(ipAddrOnly) + comment;
            String echoSudo = "sudo echo ";
            switch (whatList) {
                case 1:
                    return echoSudo + "\"" + comment + "\"" + " >> /etc/pf/vipnet;sudo /etc/initpf.fw;";
                case 2:
                    return echoSudo + "\"" + comment + "\"" + " >> /etc/pf/squid;sudo /etc/initpf.fw;sudo squid -k reconfigure;";
                case 3:
                    return echoSudo + "\"" + comment + "\"" + " >> /etc/pf/squidlimited;sudo /etc/initpf.fw;sudo squid -k reconfigure;";
                case 4:
                    return echoSudo + "\"" + comment + "\"" + " >> /etc/pf/tempfull;sudo /etc/initpf.fw;sudo squid -k reconfigure;";
                default:
                    return "ls";
            }
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshActs{");
        sb.append("allowDomain='").append(allowDomain).append('\'');
        sb.append(", AT_NAME_SSHACTS='").append(AT_NAME_SSHACTS).append('\'');
        sb.append(", AT_NAME_SSHDETAIL='").append(AT_NAME_SSHDETAIL).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", inet='").append(inet).append('\'');
        sb.append(", ipAddrOnly='").append(ipAddrOnly).append('\'');
        sb.append(", PAGE_NAME='").append(PAGE_NAME).append('\'');
        sb.append(", pcName='").append(pcName).append('\'');
        sb.append(", squid=").append(squid);
        sb.append(", squidLimited=").append(squidLimited);
        sb.append(", tempFull=").append(tempFull);
        sb.append(", vipNet=").append(vipNet);
        sb.append('}');
        return sb.toString();
    }

    private void allowDomainAct() {
        String ipResolved = null;
        try {
            ipResolved = InetAddress.getByName(allowDomain).toString();
        } catch (UnknownHostException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        String commandSSH = "sudo echo " + "\"" + allowDomain + "\"" + " >> /etc/pf/allowdomain;sudo echo " + "\"" + ipResolved + comment + "\"" + " >> /etc/pf/allowip;sudo /etc/initpf.fw;sudo squid -k reconfigure;sudo /etc/initpf.fw;sudo tail /etc/pf/allowdomain;exit";
        commandSSH = "ls";
        LOGGER.warn(new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandSSH).build().call());

    }


    /**
     {@link Controller}, для работы с SSH

     @since 01.12.2018 (9:58)
     */
    @Controller
    public class SshActsCTRL {

        /**
         {@link SshActs}
         */
        @Autowired
        private SshActs sshActs;

        @PostMapping("/sshacts")
        public String sshActsPOST(@ModelAttribute SshActs sshActs, Model model, HttpServletRequest request) throws AccessDeniedException {
            String pcReq = request.getRemoteAddr().toLowerCase();
            if (getAuthentic(pcReq)) {
                this.sshActs = sshActs;
                model.addAttribute(AT_NAME_SSHACTS, sshActs);
                model.addAttribute(AT_NAME_SSHDETAIL, sshActs.getPcName());
                return PAGE_NAME;
            } else throw new AccessDeniedException("NOT Allowed!");
        }

        @Autowired
        public SshActsCTRL(SshActs sshActs) {
            this.sshActs = sshActs;
        }

        private boolean getAuthentic(String pcReq) {
            return
                pcReq.contains("10.10.111.") ||
                    pcReq.contains("10.200.213.85") ||
                    pcReq.contains("0:0:0:0") ||
                    pcReq.contains("10.10.111");
        }

        @GetMapping("/sshacts")
        public String sshActsGET(Model model, HttpServletRequest request) throws AccessDeniedException {
            String pcReq = request.getRemoteAddr().toLowerCase();
            LOGGER.warn(pcReq);
            setInet(pcReq);
            if (getAuthentic(pcReq)) {
                model.addAttribute(ConstantsFor.TITLE, percToEnd());
                model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
                model.addAttribute(AT_NAME_SSHACTS, sshActs);
                if (request.getQueryString() != null) {
                    sshActs.parseReq(request.getQueryString());
                    model.addAttribute(ConstantsFor.TITLE, sshActs.getPcName());
                    sshActs.setPcName(sshActs.getPcName());
                    LOGGER.warn(request.getQueryString());
                }
                model.addAttribute(AT_NAME_SSHDETAIL, sshActs.toString());
                return PAGE_NAME;
            } else throw new AccessDeniedException("NOT Allowed! ");
        }

        private String percToEnd() {
            LocalTime endDay = LocalTime.parse("17:30");
            LocalDateTime localDateTime = endDay.atDate(LocalDate.now());
            return localDateTime.toString();
        }

        @PostMapping("/allowdomain")
        public String allowPOST(@ModelAttribute SshActs sshActs, Model model) {
            this.sshActs = sshActs;
            allowDomainAct();
            model.addAttribute(ConstantsFor.TITLE, sshActs.getAllowDomain());
            model.addAttribute(AT_NAME_SSHACTS, sshActs);
            model.addAttribute("ok", sshActs.toString());
            model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }
    }
}
