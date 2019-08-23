// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ssh;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.SwitchesWiFi;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.*;


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
    private PfLists pfListsInstAW;
    
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
     new {@link SSHFactory.Builder} ({@link SwitchesWiFi#IPADDR_SRVNAT} , {@link #commandForNatStr}).
     
     @param pfLists {@link #pfListsInstAW}
     */
    @Contract(pure = true)
    @Autowired
    public PfListsSrv(@NotNull PfLists pfLists) {
        this.pfListsInstAW = pfLists;
    }
    
    @Contract(pure = true)
    public static String getDefaultConnectSrv() {
        return DEFAULT_CONNECT_SRV;
    }
    
    public String runCom() {
        if (System.getProperty("os.name").toLowerCase().contains(PropertiesNames.PR_WINDOWSOS)) {
            return new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName()).build().call();
        }
        else {
            return "22.06.2019 (8:01)";
        }
    }
    
    /**
     @return {@link #commandForNatStr}
     */
    @SuppressWarnings("WeakerAccess")
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
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfListsSrv{");
        sb.append("commandForNatStr='").append(commandForNatStr).append('\'');
        //noinspection DuplicateStringLiteralInspection
        sb.append(", pfListsInstAW=").append(pfListsInstAW);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Формирует списки <b>pf</b>
     
     @see PfListsCtr
     */
    void makeListRunner() {
        try {
            buildFactory();
        }
        catch (FileNotFoundException | ExecutionException | InterruptedException | TimeoutException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".makeListRunner", e));
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
    private void buildFactory() throws FileNotFoundException, ExecutionException, InterruptedException, TimeoutException {
        SSHFactory.Builder builderInst = new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName());
        SSHFactory build = builderInst.build();
        if (!new File(builderInst.getPem()).exists()) {
            throw new FileNotFoundException("NO CERTIFICATE a161.getPem...");
        }
        if (pfListsInstAW == null) {
            this.pfListsInstAW = new PfLists();
        }
        pfListsInstAW.setGitStatsUpdatedStampLong(System.currentTimeMillis());
        
        build.setCommandSSH("sudo cat /etc/pf/vipnet;sudo cat /etc/pf/24hrs && exit");
        pfListsInstAW.setVipNet(build.call());
        
        build.setCommandSSH(ConstantsFor.SSH_SHOW_PFSQUID);
        pfListsInstAW.setStdSquid(build.call());
        
        build.setCommandSSH(ConstantsFor.SSH_SHOW_PROXYFULL);
        pfListsInstAW.setFullSquid(build.call());
        
        build.setCommandSSH(ConstantsFor.SSH_SHOW_SQUIDLIMITED);
        pfListsInstAW.setLimitSquid(build.call());
        
        build.setCommandSSH("pfctl -s nat && exit");
        pfListsInstAW.setPfNat(build.call());
        
        build.setCommandSSH("pfctl -s rules && exit");
        pfListsInstAW.setPfRules(build.call());
        
        build.setCommandSSH("sudo cat /home/kudr/inet.log && exit");
        String inetLog = build.call();
        pfListsInstAW.setInetLog(inetLog);
        
        Future<String> checkUniqueInListsFuture = AppComponents.threadConfig().getTaskExecutor().submit(new AccessListsCheckUniq());
        String inetUniqStr = checkUniqueInListsFuture.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        pfListsInstAW.setInetLog(inetLog + inetUniqStr.replace("<br>", "\n"));
    }
    
    private static String whatSrv() {
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }
}
