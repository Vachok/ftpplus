// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.SwitchesWiFi;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 Список-выгрузка с сервера доступа в интернет

 @since 10.09.2018 (11:49) */
@Service(ConstantsFor.BEANNAME_PFLISTSSRV)
public class PfListsSrv {


    private static final String DEFAULT_CONNECT_SRV = whatSrv();

    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
        .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, PfListsSrv.class.getSimpleName());

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
    @NotNull private String commandForNatStr = ConstantsFor.SSHCOM_GETALLOWDOMAINS;

    /**
     @return {@link #commandForNatStr}
     */
    @NotNull
    public String getCommandForNatStr() {
        return commandForNatStr;
    }

    /**
     @param commandForNatStr {@link #commandForNatStr}
     */
    @SuppressWarnings("unused")
    public void setCommandForNatStr(@NotNull String commandForNatStr) {
        this.commandForNatStr = commandForNatStr;
    }

    @Contract(pure = true)
    public static String getDefaultConnectSrv() {
        return DEFAULT_CONNECT_SRV;
    }

    /**
     {@code this.builderInst}
     <p>
     new {@link SSHFactory.Builder} ({@link SwitchesWiFi#IPADDR_SRVNAT} , {@link #commandForNatStr}).

     @param pfLists {@link #pfListsInstAW}
     */
    @Autowired
    public PfListsSrv(@NotNull PfLists pfLists) {
        this.pfListsInstAW = pfLists;
    }

    public String runCom() {
        return AppConfigurationLocal.getInstance()
            .submitAsString(new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName()).build(), 10);
    }

    /**
     Формирует списки <b>pf</b>

     @see PfListsSrv
     */
    public boolean makeListRunner() {
        try {
            buildFactory();
            return true;
        }
        catch (FileNotFoundException | ExecutionException | InterruptedException | TimeoutException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".makeListRunner", e));
            return false;
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
        int timeOusSec = 6;
        if (!new File(builderInst.getPem()).exists()) {
            throw new FileNotFoundException("NO CERTIFICATE a161.getPem...");
        }
        if (pfListsInstAW == null) {
            this.pfListsInstAW = new PfLists();
        }
        pfListsInstAW.setGitStatsUpdatedStampLong(System.currentTimeMillis());

        build.setCommandSSH("sudo cat /etc/pf/vipnet;sudo cat /etc/pf/24hrs && exit");

        pfListsInstAW.setVipNet(AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec));

        build.setCommandSSH(ConstantsFor.SSH_CAT_PFSQUID);
        pfListsInstAW.setStdSquid(AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec));

        build.setCommandSSH(ConstantsFor.SSH_CAT_PROXYFULL);
        pfListsInstAW.setFullSquid(AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec));

        build.setCommandSSH(ConstantsFor.SSH_SHOW_SQUIDLIMITED);
        pfListsInstAW.setLimitSquid(AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec));

        build.setCommandSSH("pfctl -s nat && exit");
        pfListsInstAW.setPfNat(AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec));

        build.setCommandSSH("pfctl -s rules && exit");
        pfListsInstAW.setPfRules(AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec));

        build.setCommandSSH("sudo cat /home/kudr/inet.log && exit");
        String inetLog = AppConfigurationLocal.getInstance().submitAsString(build, timeOusSec);
        pfListsInstAW.setInetLog(inetLog);
        Future<String> checkUniqueInListsFuture = AppComponents.threadConfig().getTaskExecutor().submit(new AccessListsCheckUniq());
        String inetUniqStr = checkUniqueInListsFuture.get(timeOusSec, TimeUnit.SECONDS);
        pfListsInstAW.setInetLog(inetLog + inetUniqStr.replace("<br>", "\n"));
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
        if (UsefulUtilities.thisPC().toLowerCase().contains("rups")) {
            return SwitchesWiFi.RUPSGATE;
        }
        else {
            return SwitchesWiFi.IPADDR_SRVGIT;
        }
    }
}
