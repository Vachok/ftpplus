// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.services.WhoIsWithSRV;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.data.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.props.InitProperties;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 @see SshActsTest
 @since 29.11.2018 (13:01) */
@Service(ModelAttributeNames.ATT_SSH_ACTS)
@Scope(ConstantsFor.PROTOTYPE)
public class SshActs {


    private static final Pattern COMPILE = Pattern.compile("http://", Pattern.LITERAL);

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
    private String allowDomain;

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

    private boolean vipNet;

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

    public boolean isVipNet() {
        return vipNet;
    }

    public SshActs(String ipAddrOnly, String allowDomain) {
        this.ipAddrOnly = ipAddrOnly;
        this.allowDomain = allowDomain;
    }

    public SshActs() {

    }

    /**
     @see SshActsTest#testAllowDomainAdd()
     */
    public String allowDomainAdd() {
        String result;
        if (ipAddrOnly != null && ipAddrOnly.equalsIgnoreCase(ConstantsFor.DELETE)) {
            this.delDomain = allowDomain;
            result = allowDomainDel();
        }
        else {
            this.allowDomain = Objects.requireNonNull(checkDName());
            if (allowDomain.equalsIgnoreCase(ConstantsFor.ANS_DOMNAMEEXISTS)) {
                result = allowDomain;
            }
            else {
                String resolvedIp = resolveIp(allowDomain);
                String commandSSH = new StringBuilder()
                    .append(ConstantsFor.SSH_SUDO_GREP_V).append(Objects.requireNonNull(allowDomain, ConstantsFor.ANS_DNAMENULL))
                    .append(ConstantsFor.SSH_ALLOWDOM_ALLOWDOMTMP)
                    .append(ConstantsFor.SSH_SUDO_GREP_V).append(Objects.requireNonNull(resolvedIp, ConstantsFor.ANS_DNAMENULL))
                    .append(" #")
                    .append(allowDomain)
                    .append(ConstantsFor.SSH_ALLOWIP_ALLOWIPTMP)
                    .append(ConstantsFor.SSH_ALLOWDOMTMP_ALLOWDOM)
                    .append(ConstantsFor.SSH_ALLOWIPTMP_ALLOWIP)

                    .append(ConstantsFor.SSH_SUDO_ECHO).append("\"").append(Objects.requireNonNull(allowDomain, ConstantsFor.ANS_DNAMENULL)).append("\"")
                    .append(" >> /etc/pf/allowdomain;")
                    .append(ConstantsFor.SSH_SUDO_ECHO).append("\"").append(resolvedIp).append(" #").append(allowDomain).append("\"").append(" >> /etc/pf/allowip;")
                    .append(ConstantsFor.SSH_TAIL_ALLOWIPALLOWDOM)
                    .append(ConstantsFor.SSH_SQUID_RECONFIGURE)
                    .append(ConstantsFor.SSH_INITPF).toString();
                String call = "<b>" + new SSHFactory.Builder(whatSrvNeed(), commandSSH, getClass().getSimpleName()).build().call() + "</b>";
                try {
                    call = call + "<font color=\"gray\"><br><br>" + new WhoIsWithSRV().whoIs(resolvedIp) + "</font>";
                }
                catch (RuntimeException e) {
                    call = AbstractForms.fromArray(e);
                }
                FileSystemWorker.writeFile(allowDomain.replaceFirst("\\Q.\\E", "") + ".log", call);
                result = call.replace("\n", "<br>")
                    .replace(allowDomain, "<font color=\"yellow\">" + allowDomain + "</font>")
                    .replace(resolvedIp, "<font color=\"yellow\">" + resolvedIp + "</font>");
            }
        }
        return result;
    }

    /**
     @see SshActsTest#testAllowDomainDel()
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
                .append(ConstantsFor.SSH_SUDO_GREP_V)
                .append(x)
                .append(ConstantsFor.SSH_ALLOWDOM_ALLOWDOMTMP)
                .append(ConstantsFor.SSH_SUDO_GREP_V);
            resolvedIp.ifPresent(stringBuilder::append);
            sshComBuilder.append(" #")
                .append(x)
                .append(ConstantsFor.SSH_ALLOWIP_ALLOWIPTMP)
                .append(ConstantsFor.SSH_ALLOWDOMTMP_ALLOWDOM)
                .append(ConstantsFor.SSH_ALLOWIPTMP_ALLOWIP)
                .append(ConstantsFor.SSH_TAIL_ALLOWIPALLOWDOM)
                .append(ConstantsFor.SSH_SQUID_RECONFIGURE)
                .append(ConstantsFor.SSH_INITPF);

            String sshCom = sshComBuilder.toString();
            String resStr = new SSHFactory.Builder(whatSrvNeed(), sshCom, getClass().getSimpleName()).build().call();

            stringBuilder.append(resStr.replace("\n", "<br>\n"));
            stringBuilder.append(sshCom);
        });
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".log", stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String checkDName() {
        try {
            this.allowDomain = COMPILE.matcher(allowDomain).replaceAll(Matcher.quoteReplacement("."));
        }
        catch (NullPointerException e) {
            this.allowDomain = ConstantsFor.SITENAME_VELKOMFOODRU;
            this.allowDomain = COMPILE.matcher(allowDomain).replaceAll(Matcher.quoteReplacement("."));
        }
        if (allowDomain.contains(ConstantsFor.HTTPS)) {
            this.allowDomain = allowDomain.replace(ConstantsFor.HTTPS, ".");
        }
        if (allowDomain.contains("/")) {
            allowDomain = allowDomain.split("/")[0];
        }
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

    public String whatSrvNeed() {
        InitProperties.getTheProps().setProperty(PropertiesNames.THISPC, UsefulUtilities.thisPC());
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }

    private String checkDNameDel() {
        try {
            this.delDomain = delDomain.replace("http://", ".");
        }
        catch (NullPointerException e) {
            this.delDomain = ConstantsFor.SITENAME_VELKOMFOODRU;
            this.delDomain = delDomain.replace("http://", ".");
        }
        if (delDomain.contains(ConstantsFor.HTTPS)) {
            this.delDomain = delDomain.replace(ConstantsFor.HTTPS, ".");

        }
        if (delDomain.contains("/")) {
            this.delDomain = delDomain.split("/")[0];
        }

        String fromServerListDomains = getServerListDomains();
        boolean anyMatch = fromServerListDomains.toLowerCase().contains(delDomain.toLowerCase());

        if (!anyMatch) {
            this.delDomain = "No domain to delete.";
        }
        return delDomain;
    }

    @NotNull
    private String getServerListDomains() {
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
        return call;
    }

    public void setAllFalse() {
        this.squid = false;
        this.vipNet = false;
    }

    public void setSquid() {
        this.squid = true;
        this.vipNet = false;
    }

    public void setSquidLimited() {
        this.squid = false;
        this.vipNet = false;
    }

    public void setTempFull() {
        this.squid = false;
        this.vipNet = false;

    }

    public void setVipNet() {
        this.vipNet = true;
        this.squid = false;
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

    String execSSHCommand(String sshCommand) {
        return new SSHFactory.Builder(whatSrvNeed(), sshCommand, getClass().getSimpleName()).build().call();
    }

    /**
     @param server сервер, для подклчения
     @param command команда
     @return результат выполнения

     @see VpnHelper
     */
    String execSSHCommand(String server, String command) {
        return new SSHFactory.Builder(server, command, getClass().getSimpleName()).build().call();
    }
}
