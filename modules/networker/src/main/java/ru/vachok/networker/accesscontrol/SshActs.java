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
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.WhoIsWithSRV;

import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.util.Objects;
import java.util.Optional;


/**
 SSH-actions class

 @since 29.11.2018 (13:01) */
@Service(ConstantsFor.ATT_SSH_ACTS)
public class SshActs {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger(SshActs.class.getSimpleName());

    /**
     sshworks.html
     */
    private static final String PAGE_NAME = "sshworks";

    /**
     SSH-command
     */
    static final String SUDO_ECHO = "sudo echo ";

    /**
     SSH-command
     */
    static final String SUDO_GREP_V = "sudo grep -v '";

    /**
     Имя ПК для разрешения
     */
    private String pcName;

    private String userInput;

    public void setPcName(String pcName) {
        if (pcName.contains(ConstantsNet.DOMAIN_EATMEATRU)) {
            this.pcName = pcName;
        } else {
            this.pcName = new NameOrIPChecker(this.pcName).checkPat(pcName);
        }
    }

    public String getUserInput() {
        return userInput;
    }

    /**
     Уровень доступа к инету
     */
    private String inet;

    /**
     Разрешить адрес
     */
    private String allowDomain;

    private String ipAddrOnly;

    /**
     Комментарий
     */
    private String comment;

    private String tRoute;

    /**
     Имя домена для удаления.
     */
    private String delDomain;

    private boolean squid;

    private boolean squidLimited;

    private boolean tempFull;

    private boolean vipNet;

    public void setIpAddrOnly(String ipAddrOnly) {
        this.ipAddrOnly = ipAddrOnly;
    }

    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }

    public String gettRoute() {
        return tRoute;
    }

    public String getDelDomain() {
        return delDomain;
    }

    public void setDelDomain(String delDomain) {
        this.delDomain = delDomain;
    }

    public String getAllowDomain() {
        return allowDomain;
    }

    public void setAllowDomain(String allowDomain) {
        this.allowDomain = allowDomain;
    }

    public String getPcName() {
        return pcName;
    }

    public void settRoute(String tRoute) {
        this.tRoute = tRoute;
    }

    public String getInet() {
        return inet;
    }

    private void setInet(String queryString) {
        this.inet = queryString;
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

    String providerTraceStr() {
        SSHFactory.Builder builder = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVGIT, "traceroute 8.8.8.8;exit");
        SSHFactory build = builder.build();
        String call = build.call();
        this.tRoute = call;
        return call;
    }

    /**
     Добавить домен в разрешенные

     @return результат выполненния
     */
    private String allowDomainAdd() {
        this.allowDomain = checkDName();
        Objects.requireNonNull(allowDomain, "allowdomain string is null");
        String commandSSH = new StringBuilder()
            .append(SUDO_GREP_V).append(Objects.requireNonNull(allowDomain)).append("' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp;")
            .append(SUDO_GREP_V).append(Objects.requireNonNull(resolveIp(allowDomain))).append(" #").append(allowDomain).append("' /etc/pf/allowip > " +
                "/etc/pf/allowip_tmp;")

            .append("sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain;")
            .append("sudo cp /etc/pf/allowip_tmp /etc/pf/allowip;")

            .append(SUDO_ECHO).append("\"").append(Objects.requireNonNull(allowDomain, "allowdomain string is null")).append("\"").append(" >> " +
                "/etc/pf/allowdomain;")
            .append(SUDO_ECHO).append("\"").append(resolveIp(allowDomain)).append(" #").append(allowDomain).append("\"").append(" >> /etc/pf/allowip;")

            .append("sudo /etc/initpf.fw;")
            .append("ping -c 5 10.200.200.1;")
            .append("sudo squid -k reconfigure;")

            .append("exit;").toString();
        String call = "<b>" + new SSHFactory.Builder(ConstantsFor.IPADDR_SRVNAT, commandSSH).build().call() + "</b>";
        call = call + "<font color=\"gray\"><br><br>" + new WhoIsWithSRV().whoIs(resolveIp(allowDomain)) + "</font>";
        writeToLog(new String((call + "\n\n" + toString()).getBytes(), Charset.defaultCharset()));
        return call;
    }

    /**
     Приведение имени домена в нужный формат
     <p>

     @return имя домена для применения в /etc/pf/allowdomain
     */
    private String checkDName() {
        this.allowDomain = allowDomain.replace("http://", ".");
        if (allowDomain.contains("https")) {
            this.allowDomain = allowDomain.replace("https://", ".");
        }
        char[] chars = allowDomain.toCharArray();
        try {
            Character lastChar = chars[chars.length - 1];
            if (lastChar.equals('/')) {
                chars[chars.length - 1] = ' ';
                this.allowDomain = new String(chars).trim();
            } else {
                this.allowDomain = new String(allowDomain.getBytes(), Charset.defaultCharset());
            }
            return allowDomain;
        } catch (ArrayIndexOutOfBoundsException e) {
            return e.getMessage();
        }
    }

    /**
     Резолвит ip-адрес
     <p>

     @param s домен для проверки
     @return ip-адрес
     */
    private String resolveIp(String s) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(s.replaceFirst("\\Q.\\E", ""));
        } catch (UnknownHostException e) {
            String msg = "SshActs" + ".resolveIp\n" + e.getMessage();
            LOGGER.error(msg);
            FileSystemWorker.error("SshActs.resolveIp", e);
        }
        return Objects.requireNonNull(inetAddress).getHostAddress();
    }

    /**
     Удаление домена из разрешенных

     @return результат выполнения
     */
    private String allowDomainDel() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(delDomain).append(" del domain raw<br>");
        this.delDomain = checkDNameDel();
        Optional<String> delDomainOpt = Optional.of(delDomain);
        delDomainOpt.ifPresent(x -> {
            String sshCom = new StringBuilder()
                .append(SUDO_GREP_V).append(x).append("' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp;")
                .append(SUDO_GREP_V).append(Objects.requireNonNull(resolveIp(x))).append(" #").append(x).append("' /etc/pf/allowip > /etc/pf/allowip_tmp;")

                .append("sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain;")
                .append("sudo cp /etc/pf/allowip_tmp /etc/pf/allowip;")

                .append("sudo /etc/initpf.fw;")
                .append("ping -c 5 10.200.200.1;")
                .append("sudo squid -k reconfigure;")

                .append("exit;").toString();
            String resStr = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVNAT, sshCom).build().call();
            stringBuilder.append(resStr);
        });
        writeToLog(stringBuilder.toString());
        return stringBuilder.toString();
    }

    /**
     Запись результата в лог
     <p>
     {@code this.getClass().getSimpleName() + ".log"}

     @param s лог, для записи
     */
    private void writeToLog(String s) {
        try (OutputStream outputStream = new FileOutputStream(this.getClass().getSimpleName() + ".log")) {
            outputStream.write(s.getBytes());
        } catch (IOException e) {
            String msg = "SshActs" + ".writeToLog\n" + e.getMessage();
            LOGGER.error(msg);
            FileSystemWorker.error("SshActs.writeToLog", e);
        }
    }

    /**
     @return имя домена, для удаления.
     */
    private String checkDNameDel() {
        this.delDomain = delDomain.replace("http://", ".");
        if (delDomain.contains("https")) {
            this.delDomain = delDomain.replace("https://", ".");
        }
        char[] chars = delDomain.toCharArray();
        try {
            Character lastChar = chars[chars.length - 1];
            if (lastChar.equals('/')) {
                chars[chars.length - 1] = ' ';
                this.delDomain = new String(chars).trim();
            } else {
                this.delDomain = new String(delDomain.getBytes(), Charset.defaultCharset());
            }
            return delDomain;
        } catch (ArrayIndexOutOfBoundsException e) {
            return e.getMessage();
        }
    }

    private String execByWhatListSwitcher(int whatList, boolean iDel) {
        if (iDel) {
            return new StringBuilder()
                .append(SUDO_GREP_V)
                .append(Objects.requireNonNull(pcName))
                .append("' /etc/pf/vipnet > /etc/pf/vipnet_tmp;sudo grep -v '")
                .append(Objects.requireNonNull(ipAddrOnly))
                .append("' /etc/pf/vipnet > /etc/pf/vipnet_tmp;sudo cp /etc/pf/vipnet_tmp /etc/pf/vipnet;sudo pfctl -f /etc/srv-nat.conf;sudo grep -v '")
                .append(Objects.requireNonNull(ipAddrOnly))
                .append("' /etc/pf/squid > /etc/pf/squid_tmp;sudo cp /etc/pf/squid_tmp /etc/pf/squid;sudo grep -v '")
                .append(Objects.requireNonNull(ipAddrOnly))
                .append("' /etc/pf/squidlimited > /etc/pf/squidlimited_tmp;sudo cp /etc/pf/squidlimited_tmp /etc/pf/squidlimited;sudo grep -v '")
                .append(Objects.requireNonNull(ipAddrOnly))
                .append("' /etc/pf/tempfull > /etc/pf/tempfull_tmp;sudo cp /etc/pf/tempfull_tmp /etc/pf/tempfull;sudo squid -k reconfigure;sudo " +
                    "/etc/initpf.fw").toString();
        } else {
            this.comment = Objects.requireNonNull(ipAddrOnly) + comment;
            String echoSudo = SUDO_ECHO;
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

    /**
     Установить все списки на <b>false</b>
     */
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

    @Override
    public int hashCode() {
        return Objects.hash(pcName, ipAddrOnly, comment, delDomain);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SshActs sshActs = (SshActs) o;

        return Objects.equals(pcName, sshActs.pcName) &&
            Objects.equals(ipAddrOnly, sshActs.ipAddrOnly) &&
            Objects.equals(comment, sshActs.comment) &&
            Objects.equals(delDomain, sshActs.delDomain);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshActs{");
        sb.append("allowDomain='").append(allowDomain).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", delDomain='").append(delDomain).append('\'');
        sb.append(", inet='").append(inet).append('\'');
        sb.append(", ipAddrOnly='").append(ipAddrOnly).append('\'');
        sb.append(", pcName='").append(pcName).append('\'');
        sb.append(", squid=").append(squid);
        sb.append(", squidLimited=").append(squidLimited);
        sb.append(", tempFull=").append(tempFull);
        sb.append(", tRoute='").append(tRoute).append('\'');
        sb.append(", vipNet=").append(vipNet);
        sb.append('}');
        return sb.toString();
    }

    /**
     {@link Controller}, для работы с SSH

     @since 01.12.2018 (9:58)
     */
    @Controller
    public class SshActsCTRL {

        private static final String URL_SSHACTS = "/sshacts";

        /**
         {@link SshActs}
         */
        private SshActs sshActs;

        @Autowired
        public SshActsCTRL(SshActs sshActs) {
            this.sshActs = sshActs;
        }

        @PostMapping(URL_SSHACTS)
        public String sshActsPOST(@ModelAttribute SshActs sshActs, Model model, HttpServletRequest request) throws AccessDeniedException {
            String pcReq = request.getRemoteAddr().toLowerCase();
            if (getAuthentic(pcReq)) {
                this.sshActs = sshActs;
                model.addAttribute("head", new PageFooter().getHeaderUtext());
                model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
                model.addAttribute(ConstantsFor.ATT_SSHDETAIL, sshActs.getPcName());
                return PAGE_NAME;
            } else {
                throw new AccessDeniedException("NOT Allowed!");
            }
        }

        @GetMapping(URL_SSHACTS)
        public String sshActsGET(Model model, HttpServletRequest request) throws AccessDeniedException {
            Visitor visitor = ConstantsFor.getVis(request);
            sshActs.setAllowDomain("");
            sshActs.setDelDomain("");
            String pcReq = request.getRemoteAddr().toLowerCase();
            LOGGER.warn(pcReq);
            setInet(pcReq);
            if (getAuthentic(pcReq)) {
                model.addAttribute(ConstantsFor.ATT_TITLE, visitor.getTimeSpend());
                model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
                model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
                if (request.getQueryString() != null) {
                    parseReq(request.getQueryString());
                    model.addAttribute(ConstantsFor.ATT_TITLE, sshActs.getPcName());
                    sshActs.setPcName(sshActs.getPcName());
                    LOGGER.warn(request.getQueryString());
                }
                model.addAttribute(ConstantsFor.ATT_SSHDETAIL, sshActs.toString());
                return PAGE_NAME;
            } else {
                throw new AccessDeniedException("NOT Allowed! ");
            }
        }

        @PostMapping("/allowdomain")
        public String allowPOST(@ModelAttribute SshActs sshActs, Model model) {
            this.sshActs = sshActs;
            model.addAttribute(ConstantsFor.ATT_TITLE, sshActs.getAllowDomain() + " добавлен");
            model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
            model.addAttribute("ok", sshActs.toString() + "<p>" + sshActs.allowDomainAdd());
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }

        @PostMapping("/deldomain")
        public String delDomPOST(@ModelAttribute SshActs sshActs, Model model) {
            this.sshActs = sshActs;
            model.addAttribute(ConstantsFor.ATT_TITLE, sshActs.getDelDomain() + " удалён");
            model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
            model.addAttribute("ok", sshActs.toString() + "<p>" + sshActs.allowDomainDel());
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }

        @PostMapping("/tmpfullnet")
        public String tempFullInetAccess(@ModelAttribute SshActs sshActs, Model model) {
            this.sshActs = sshActs;
            TemporaryFullInternet temporaryFullInternet = new TemporaryFullInternet(sshActs.getUserInput());
            model.addAttribute("sshActs", sshActs);
            model.addAttribute(ConstantsFor.ATT_TITLE, ConstantsFor.getMemoryInfo());
            model.addAttribute("ok", temporaryFullInternet.doDidDoes());
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
            return "ok";
        }

        private boolean getAuthentic(String pcReq) {
            return
                pcReq.contains("10.10.111.") ||
                    pcReq.contains("10.200.213.85") ||
                    pcReq.contains("10.200.213.200") ||
                    pcReq.contains("0:0:0:0") ||
                    pcReq.contains("10.10.111");
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
                sshActs.setPcName(queryString.split("&")[0].replaceAll("pcName=", ""));
                qStr = queryString.split("&")[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                setAllFalse();
            }
            if (qStr.equalsIgnoreCase("inet=std")) {
                setSquid();
            }
            if (qStr.equalsIgnoreCase("inet=limit")) {
                setSquidLimited();
            }
            if (qStr.equalsIgnoreCase("inet=full")) {
                setTempFull();
            }
            if (qStr.equalsIgnoreCase("inet=nat")) {
                setVipNet();
            }
            String msg = toString();
            LOGGER.warn(msg);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("SshActsCTRL{");
            sb.append("sshActs=").append(sshActs.hashCode());
            sb.append('}');
            return sb.toString();
        }
    }
}
