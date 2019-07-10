// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.sshactions;


import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.IllegalAnswerSSH;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.WhoIsWithSRV;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 SSH-actions class

 @since 29.11.2018 (13:01) */
@SuppressWarnings({"ClassWithTooManyFields"})
@Service(ConstantsFor.ATT_SSH_ACTS)
@Scope("prototype")
public class SshActs {
    
    
    /**
     SSH-command
     */
    public static final String SUDO_ECHO = "sudo echo ";

    /**
     SSH-command
     */
    public static final String SSH_SUDO_GREP_V = "sudo grep -v '";
    
    public static final String SSH_ETCPF = " /etc/pf/";
    
    private static final Pattern COMPILE = Pattern.compile("http://", Pattern.LITERAL);
    
    /**
     sshworks.html
     */
    private static final String PAGE_NAME = "sshworks";
    
    private static final String STR_HTTPS = "https://";
    
    private static final String SSH_SQUID_RECONFIGURE = "sudo squid && sudo squid -k reconfigure;";
    
    private static final String SSH_PING5_200_1 = "ping -c 5 10.200.200.1;";
    
    private static final String SSH_INITPF = "sudo /etc/initpf.fw && exit;";
    
    /**
     Имя ПК для разрешения
     */
    private String pcName;
    
    private String userInput;
    
    /**
     Уровень доступа к инету
     */
    private String inet;
    
    private String numOfHours =
        String.valueOf(Math.abs(TimeUnit.SECONDS.toHours((long) LocalTime.parse("18:30").toSecondOfDay() - LocalTime.now().toSecondOfDay())));
    
    /**
     Разрешить адрес
     */
    @NonNull private String allowDomain;
    
    private String ipAddrOnly;
    
    /**
     Комментарий
     */
    private String comment;
    
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
    
    public String getNumOfHours() {
        try {
            long l = Long.parseLong(numOfHours);
            return String.valueOf(Math.abs(l));
        }
        catch (Exception e) {
            return String.valueOf(Math.abs(Integer.MAX_VALUE));
        }
    }
    
    public void setNumOfHours(String numOfHours) {
        this.numOfHours = numOfHours;
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
    
    public void setPcName(String pcName) {
        if (pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            this.pcName = pcName;
        }
        else {
            this.pcName = new NameOrIPChecker(this.pcName).checkPat(pcName);
        }
    }
    
    public String getUserInput() {
        return userInput;
    }
    
    public void setUserInput(String userInput) {
        this.userInput = userInput;
    }
    
    public String getInet() {
        return inet;
    }
    
    public void setInet(String queryString) {
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
    
    /**
     Добавить домен в разрешенные
     
     @return результат выполненния
     */
    public String allowDomainAdd() throws NullPointerException {
        this.allowDomain = Objects.requireNonNull(checkDName());
        if (allowDomain.equalsIgnoreCase("Domain is exists!")) {
            return allowDomain;
        }
    
        String resolvedIp = resolveIp(allowDomain);
    
        String commandSSH = new StringBuilder()
            .append(SSH_SUDO_GREP_V).append(Objects.requireNonNull(allowDomain, "allowdomain string is null")).append("' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp;")
            .append(SSH_SUDO_GREP_V).append(Objects.requireNonNull(resolvedIp, "allowdomain string is null"))
            .append(" #")
            .append(allowDomain)
            .append("' /etc/pf/allowip > /etc/pf/allowip_tmp;")
        
            .append("sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain;")
            .append("sudo cp /etc/pf/allowip_tmp /etc/pf/allowip;")
        
            .append(SUDO_ECHO).append("\"").append(Objects.requireNonNull(allowDomain, "allowdomain string is null")).append("\"").append(" >> /etc/pf/allowdomain;")
            .append(SUDO_ECHO).append("\"").append(resolvedIp).append(" #").append(allowDomain).append("\"").append(" >> /etc/pf/allowip;")
            .append("sudo tail /etc/pf/allowdomain;sudo tail /etc/pf/allowip;")
            .append(SSH_SQUID_RECONFIGURE)
            .append(SSH_INITPF).toString();
    
        String call = "<b>" + new SSHFactory.Builder(whatSrvNeed(), commandSSH, getClass().getSimpleName()).build().call() + "</b>";
        call = call + "<font color=\"gray\"><br><br>" + new WhoIsWithSRV().whoIs(resolvedIp) + "</font>";
        FileSystemWorker.writeFile(allowDomain.replaceFirst("\\Q.\\E", "") + ".log", call);
        return call.replace("\n", "<br>")
            .replace(allowDomain, "<font color=\"yellow\">" + allowDomain + "</font>").replace(resolvedIp, "<font color=\"yellow\">" + resolvedIp + "</font>");
    }
    
    /**
     Удаление домена из разрешенных
     
     @return результат выполнения
     */
    @SuppressWarnings("DuplicateStringLiteralInspection") public String allowDomainDel() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(delDomain).append("<p>");
        this.delDomain = checkDNameDel();
        if (delDomain.equalsIgnoreCase("No domain to delete.")) {
            return delDomain;
        }
        Optional<String> delDomainOpt = Optional.of(delDomain);
        delDomainOpt.ifPresent(x->{
            Optional<String> resolvedIp = Optional.of(resolveIp(x));
            StringBuilder sshComBuilder = new StringBuilder()
                .append(SSH_SUDO_GREP_V)
                .append(x)
                .append("' /etc/pf/allowdomain > /etc/pf/allowdomain_tmp;")
                .append(SSH_SUDO_GREP_V);
            resolvedIp.ifPresent(stringBuilder::append);
            sshComBuilder.append(" #")
                .append(x)
                .append("' /etc/pf/allowip > /etc/pf/allowip_tmp;")
                .append("sudo cp /etc/pf/allowdomain_tmp /etc/pf/allowdomain;")
                .append("sudo cp /etc/pf/allowip_tmp /etc/pf/allowip;")
                .append("sudo tail /etc/pf/allowdomain;sudo tail /etc/pf/allowip;")
                .append(SSH_SQUID_RECONFIGURE)
                .append(SSH_INITPF).toString();
    
            String resStr = new SSHFactory.Builder(whatSrvNeed(), sshComBuilder.toString(), getClass().getSimpleName()).build().call();
    
            stringBuilder.append(resStr.replace("\n", "<br>\n"));
        });
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", stringBuilder.toString());
        return stringBuilder.toString();
    }
    
    /**
     Установить все списки на <b>false</b>
     */
    public void setAllFalse() {
        this.squidLimited = false;
        this.squid = false;
        this.tempFull = false;
        this.vipNet = false;
    }
    
    public void setSquid() {
        this.squid = true;
        this.vipNet = false;
        this.tempFull = false;
        this.squidLimited = false;
    }
    
    public void setSquidLimited() {
        this.squid = false;
        this.vipNet = false;
        this.tempFull = false;
        this.squidLimited = true;
    }
    
    public void setTempFull() {
        this.tempFull = true;
        this.squid = false;
        this.vipNet = false;
        this.squidLimited = false;
    }
    
    public void setVipNet() {
        this.vipNet = true;
        this.squid = false;
        this.tempFull = false;
        this.squidLimited = false;
    }
    
    /**
     Определяет, где запущен.
     
     @return адрес нужного сервака
     */
    public String whatSrvNeed() {
        AppComponents.getProps().setProperty(ConstantsFor.PR_THISPC, ConstantsFor.thisPC());
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshActs{");
        sb.append("allowDomain='").append(allowDomain).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", delDomain='").append(delDomain).append('\'');
        sb.append(", inet='").append(inet).append('\'');
        sb.append(", ipAddrOnly='").append(ipAddrOnly).append('\'');
        sb.append(", numOfHours='").append(numOfHours).append('\'');
        sb.append(", pcName='").append(pcName).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Приведение имени домена в нужный формат
     <p>
     
     @return имя домена для применения в /etc/pf/allowdomain
     */
    private String checkDName() {
        try {
            this.allowDomain = COMPILE.matcher(allowDomain).replaceAll(Matcher.quoteReplacement("."));
        }
        catch (NullPointerException e) {
            this.allowDomain = ConstantsFor.SITENAME_VELKOMFOODRU;
            this.allowDomain = COMPILE.matcher(allowDomain).replaceAll(Matcher.quoteReplacement("."));
        }
        if (allowDomain.contains("https")) {
            this.allowDomain = allowDomain.replace(STR_HTTPS, ".");
        }
        if (allowDomain.contains("/")) {
            allowDomain = allowDomain.split("/")[0];
        }
        this.allowDomain = allowDomain;
        SSHFactory.Builder allowDomainsBuilder = new SSHFactory.Builder(whatSrvNeed(), ConstantsFor.SSH_COM_CATALLOWDOMAIN, getClass().getSimpleName());
        String[] domainNamesFromSSH = null;
        try {
            domainNamesFromSSH = allowDomainsBuilder.build().call().split("\n");
        }
        catch (NullPointerException e) {
            throw new IllegalAnswerSSH(domainNamesFromSSH, e);
        }
        for (String domainNameFromSSH : domainNamesFromSSH) {
            if (domainNameFromSSH.contains(allowDomain)) {
                return "Domain is exists!";
            }
            else if (allowDomain.toLowerCase().contains(domainNameFromSSH)) {
                allowDomain = "# " + allowDomain;
            }
        }
        return allowDomain;
    }
    
    /**
     Резолвит ip-адрес
     <p>
     
     @param domainName домен для проверки
     @return ip-адрес
     */
    private String resolveIp(String domainName) throws NullPointerException {
        InetAddress inetAddress = null;
        try {
            domainName = domainName.replaceFirst("\\Q.\\E", "");
            if (domainName.contains("/")) {
                domainName = domainName.split("/")[0];
            }
            if (domainName.contains("# ")) {
                domainName = domainName.split("# ")[1];
            }
            inetAddress = InetAddress.getByName(domainName);
        }
        catch (UnknownHostException e) {
            String msg = "SshActs" + ".resolveIp\n" + e.getMessage();
            System.err.println(e.getMessage() + " " + getClass().getSimpleName() + ".resolveIp");
            FileSystemWorker.error("SshActs.resolveIp", e);
        }
        if (!(inetAddress == null)) {
            return inetAddress.getHostAddress();
        }
        else {
            return "192.168.13.42";
        }
    }
    
    /**
     @return имя домена, для удаления.
     */
    private String checkDNameDel() {
        try {
            this.delDomain = delDomain.replace("http://", ".");
        }
        catch (NullPointerException e) {
            this.delDomain = ConstantsFor.SITENAME_VELKOMFOODRU;
            this.delDomain = delDomain.replace("http://", ".");
        }
        if (delDomain.contains(STR_HTTPS)) {
            this.delDomain = delDomain.replace(STR_HTTPS, ".");
    
        }
        if (delDomain.contains("/")) {
            this.delDomain = delDomain.split("/")[0];
        }
        SSHFactory.Builder delDomBuilder = new SSHFactory.Builder(whatSrvNeed(), ConstantsFor.SSH_COM_CATALLOWDOMAIN, getClass().getSimpleName());
        for (String domainNameFromSSH : delDomBuilder.build().call().split("\n")) {
            if (domainNameFromSSH.toLowerCase().contains(delDomain) || delDomain.toLowerCase().contains(domainNameFromSSH)) {
                return delDomain;
            }
        }
        return "No domain to delete.";
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private String execByWhatListSwitcher(int whatList, boolean iDel) {
        if (iDel) {
            return new StringBuilder()
                .append(SSH_SUDO_GREP_V)
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
        }
        else {
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
    
}
