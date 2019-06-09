// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.accesscontrol.PfLists;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.AccessListsCheckUniq;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SystemRuntime;

import java.io.File;
import java.io.FileNotFoundException;

import static ru.vachok.networker.IntoApplication.reloadConfigurableApplicationContext;


/**
 Список-выгрузка с сервера доступа в интернет
 
 @since 10.09.2018 (11:49) */
@Service
public class PfListsSrv {
    
    
    private static final String DEFAULT_CONNECT_SRV = whatSrv();
    
    private static MessageToUser messageToUser = new MessageLocal(PfListsSrv.class.getSimpleName());
    
    /**
     {@link PfLists}
     */
    @SuppressWarnings("CanBeFinal") private PfLists pfListsInstAW;
    
    /**
     SSH-команда.
     <p>
     При инициализации: {@code uname -a && exit}.
 
     @see PfListsCtr#runCommand(org.springframework.ui.Model, PfListsSrv)
     @see #runCom()
     */
    private @NotNull String commandForNatStr = "sudo cat /etc/pf/allowdomain && exit";
    
    /**
     {@code this.builderInst}
     <p>
     new {@link SSHFactory.Builder} ({@link ConstantsFor#IPADDR_SRVNAT} , {@link #commandForNatStr}).
 
     @param pfLists {@link #pfListsInstAW}
     */
    @Autowired
    public PfListsSrv(@NotNull PfLists pfLists) {
        this.pfListsInstAW = pfLists;
    }
    
    public static String getDefaultConnectSrv() {
        return DEFAULT_CONNECT_SRV;
    }
    
    /**
     @return {@link #commandForNatStr}
     */
    public @NotNull String getCommandForNatStr() {
        return commandForNatStr;
    }
    
    
    /**
     @param commandForNatStr {@link #commandForNatStr}
     */
    @SuppressWarnings("unused")
    public void setCommandForNatStr(@NotNull String commandForNatStr) {
        this.commandForNatStr = commandForNatStr;
    }
    
    public String runCom() {
        if (System.getProperty("os.name").toLowerCase().contains(ConstantsFor.PR_WINDOWSOS)) {
            return new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName()).build().call();
        }
        else {
            return new SystemRuntime(commandForNatStr).call();
        }
    }
    
    /**
     Формирует списки <b>pf</b>
     
     @see PfListsCtr
     */
    public void makeListRunner() {
        try {
            buildFactory();
        }
        catch (FileNotFoundException | NullPointerException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".makeListRunner", e));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfListsSrv{");
        sb.append("commandForNatStr='").append(commandForNatStr).append('\'');
        //noinspection DuplicateStringLiteralInspection
        sb.append(", pfListsInstAW=").append(pfListsInstAW);
        sb.append('}');
        return sb.toString();
    }
    
    private static String whatSrv() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return ConstantsFor.IPADDR_SRVNAT;
        }
        else {
            return ConstantsFor.IPADDR_SRVGIT;
        }
    }
    
    /**
     <b>Заполнение форм списка PF</b>
     <p>
     Тащит информацию с сервера pf. Заполняет поля {@link PfListsSrv#pfListsInstAW}
     <p>
     Списки : <br>
     <i>vipnet</i> <br>
     <i>squid</i> <br>
     <i>tempfull</i> <br>
     <i>squidlimited</i> <br>
     <p>
     Также отдаёт информацию напрямую от firewall <br>
     <i>NAT current</i> <br>
     <i>rules current</i> <br>
     <i>/home/kudr/inet.log</i>
     */
    private void buildFactory() throws FileNotFoundException, NullPointerException {
        SSHFactory.Builder builderInst = new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName());
        SSHFactory build = builderInst.build();
        if (!new File(builderInst.getPem()).exists()) {
            throw new FileNotFoundException("NO CERTIFICATE a161.getPem...");
        }
        if (pfListsInstAW == null) {
            new IntoApplication();
            pfListsInstAW = (PfLists) reloadConfigurableApplicationContext().getBeanFactory().getBean(ConstantsFor.BEANNAME_PFLISTS);
        }
        pfListsInstAW.setGitStatsUpdatedStampLong(System.currentTimeMillis());
    
        build.setCommandSSH("sudo cat /etc/pf/vipnet;sudo cat /etc/pf/24hrs && exit");
        pfListsInstAW.setVipNet(build.call());
    
        build.setCommandSSH("sudo cat /etc/pf/squid && exit");
        pfListsInstAW.setStdSquid(build.call());
    
        build.setCommandSSH("sudo cat /etc/pf/tempfull && exit");
        pfListsInstAW.setFullSquid(build.call());
    
        build.setCommandSSH("sudo cat /etc/pf/squidlimited && exit");
        pfListsInstAW.setLimitSquid(build.call());
    
        build.setCommandSSH("pfctl -s nat && exit");
        pfListsInstAW.setPfNat(build.call());
    
        build.setCommandSSH("pfctl -s rules && exit");
        pfListsInstAW.setPfRules(build.call());
    
        build.setCommandSSH("sudo cat /home/kudr/inet.log && exit");
        String inetLog = build.call();
        pfListsInstAW.setInetLog(inetLog);
    
        String inetUniqStr = new AccessListsCheckUniq().connectTo();
        pfListsInstAW.setInetLog(inetLog + inetUniqStr.replace("<br>", "\n"));
    }
}
