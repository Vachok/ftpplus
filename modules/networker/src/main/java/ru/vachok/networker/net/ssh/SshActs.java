// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.WhoIsWithSRV;
import ru.vachok.networker.data.enums.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 SSH-actions class
 
 @since 29.11.2018 (13:01) */
@SuppressWarnings({"ClassWithTooManyFields"})
@Service(ModelAttributeNames.ATT_SSH_ACTS)
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
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
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
    
    public void setPcName(@NotNull String pcName) {
        if (pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            this.pcName = pcName;
        }
        else {
            this.pcName = new NameOrIPChecker(this.pcName).resolveInetAddress().getHostName();
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
    public String allowDomainAdd() {
        this.allowDomain = Objects.requireNonNull(checkDName());
        if (allowDomain.equalsIgnoreCase(ConstantsFor.ANS_DOMNAMEEXISTS)) {
            return allowDomain;
        }
        
        String resolvedIp = resolveIp(allowDomain);
        
        String commandSSH = new StringBuilder()
            .append(SSH_SUDO_GREP_V).append(Objects.requireNonNull(allowDomain, ConstantsFor.ANS_DNAMENULL)).append(ConstantsFor.SSH_ALLOWDOM_ALLOWDOMTMP)
            .append(SSH_SUDO_GREP_V).append(Objects.requireNonNull(resolvedIp, ConstantsFor.ANS_DNAMENULL))
            .append(" #")
            .append(allowDomain)
            .append(ConstantsFor.SSH_ALLOWIP_ALLOWIPTMP)
            
            .append(ConstantsFor.SSH_ALLOWDOMTMP_ALLOWDOM)
            .append(ConstantsFor.SSH_ALLOWIPTMP_ALLOWIP)
            
            .append(SUDO_ECHO).append("\"").append(Objects.requireNonNull(allowDomain, ConstantsFor.ANS_DNAMENULL)).append("\"").append(" >> /etc/pf/allowdomain;")
            .append(SUDO_ECHO).append("\"").append(resolvedIp).append(" #").append(allowDomain).append("\"").append(" >> /etc/pf/allowip;")
            .append(ConstantsFor.SSH_TAIL_ALLOWIPALLOWDOM)
            .append(SSH_SQUID_RECONFIGURE)
            .append(SSH_INITPF).toString();
        
        String call = "<b>" + new SSHFactory.Builder(whatSrvNeed(), commandSSH, getClass().getSimpleName()).build().call() + "</b>";
        call = call + "<font color=\"gray\"><br><br>" + new WhoIsWithSRV().whoIs(resolvedIp) + "</font>";
        FileSystemWorker.writeFile(allowDomain.replaceFirst("\\Q.\\E", "") + ".log", call);
        return call.replace("\n", "<br>")
            .replace(allowDomain, "<font color=\"yellow\">" + allowDomain + "</font>").replace(resolvedIp, "<font color=\"yellow\">" + resolvedIp + "</font>");
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
        if (allowDomain.contains(ConstantsFor.HTTPS)) {
            this.allowDomain = allowDomain.replace(ConstantsFor.STR_HTTPS, ".");
        }
        if (allowDomain.contains("/")) {
            allowDomain = allowDomain.split("/")[0];
        }
        this.allowDomain = allowDomain;
        SSHFactory.Builder allowDomainsBuilder = new SSHFactory.Builder(whatSrvNeed(), ConstantsFor.SSH_COM_CATALLOWDOMAIN, getClass().getSimpleName());
        String[] domainNamesFromSSH = {"No name"};
        try {
            Future<String> submit = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().submit(allowDomainsBuilder.build());
            domainNamesFromSSH = submit.get(1, TimeUnit.MINUTES).split("\n");
        }
        catch (NullPointerException | ExecutionException | TimeoutException e) {
            throw new InvokeIllegalException(MessageFormat.format("domainNamesFromSSH is {0} \n{1}", domainNamesFromSSH, new TForms().fromArray(e)));
        }
        catch (InterruptedException e) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        for (String domainNameFromSSH : domainNamesFromSSH) {
            if (domainNameFromSSH.contains(allowDomain)) {
                return ConstantsFor.ANS_DOMNAMEEXISTS;
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
 
     @throws NullPointerException
     */
    private String resolveIp(String domainName) {
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
     Определяет, где запущен.
     
     @return адрес нужного сервака
     */
    public String whatSrvNeed() {
        AppComponents.getProps().setProperty(PropertiesNames.PR_THISPC, UsefulUtilities.thisPC());
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }
    
    /**
     Удаление домена из разрешенных
     
     @return результат выполнения
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public String allowDomainDel() {
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
                .append(ConstantsFor.SSH_ALLOWDOM_ALLOWDOMTMP)
                .append(SSH_SUDO_GREP_V);
            resolvedIp.ifPresent(stringBuilder::append);
            sshComBuilder.append(" #")
                .append(x)
                .append(ConstantsFor.SSH_ALLOWIP_ALLOWIPTMP)
                .append(ConstantsFor.SSH_ALLOWDOMTMP_ALLOWDOM)
                .append(ConstantsFor.SSH_ALLOWIPTMP_ALLOWIP)
                .append(ConstantsFor.SSH_TAIL_ALLOWIPALLOWDOM)
                .append(SSH_SQUID_RECONFIGURE)
                .append(SSH_INITPF).toString();
            
            String resStr = new SSHFactory.Builder(whatSrvNeed(), sshComBuilder.toString(), getClass().getSimpleName()).build().call();
            
            stringBuilder.append(resStr.replace("\n", "<br>\n"));
        });
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", stringBuilder.toString());
        return stringBuilder.toString();
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
        if (delDomain.contains(ConstantsFor.STR_HTTPS)) {
            this.delDomain = delDomain.replace(ConstantsFor.STR_HTTPS, ".");
            
        }
        if (delDomain.contains("/")) {
            this.delDomain = delDomain.split("/")[0];
        }
        
        String[] fromServerListDomains = getServerListDomains();
        boolean anyMatch = Arrays.stream(fromServerListDomains).allMatch((domStr)->delDomain.contains(domStr));
        
        if (!anyMatch) {
            this.delDomain = "No domain to delete.";
        }
        return delDomain;
    }
    
    private @NotNull String[] getServerListDomains() {
        SSHFactory.Builder delDomBuilder = new SSHFactory.Builder(whatSrvNeed(), ConstantsFor.SSH_COM_CATALLOWDOMAIN, getClass().getSimpleName());
        Callable<String> factory = delDomBuilder.build();
        Future<String> future = Executors.newSingleThreadExecutor().submit(factory);
        String call;
        try {
            call = future.get(60, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            call = MessageFormat.format("SshActs.getServerListDomains:<br>\n {0}, ({1})", e.getMessage(), e.getClass().getName());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
        }
        return call.split("<br>\n");
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
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private @NotNull String execByWhatListSwitcher(int whatList, boolean iDel) {
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
