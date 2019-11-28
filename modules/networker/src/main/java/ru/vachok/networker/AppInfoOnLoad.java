// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.exe.runnabletasks.OnStartTasksLoader;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.ssh.Tracerouting;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
 @see ru.vachok.networker.AppInfoOnLoadTest
 @since 19.12.2018 (9:40) */
@SuppressWarnings("ClassUnconnectedToPackage")
public class AppInfoOnLoad implements Runnable {


    private static final List<String> MINI_LOGGER = new ArrayList<>();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AppInfoOnLoad.class.getSimpleName());

    private final AppConfigurationLocal scheduleDefiner = AppConfigurationLocal.getInstance(AppConfigurationLocal.SCHEDULE_DEFINER);

    private final AppConfigurationLocal onStartTasksLoader = new OnStartTasksLoader();

    private static int thisDelay = UsefulUtilities.getScansDelay();

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        String avCharsetsStr = AbstractForms.fromArray(Charset.availableCharsets());
        FileSystemWorker.writeFile(FileNames.AVAILABLECHARSETS_TXT, avCharsetsStr);
        SyncData syncData = SyncData.getInstance(SyncData.INETSYNC);

        AppConfigurationLocal.getInstance().execute(scheduleDefiner);

        AppConfigurationLocal.getInstance().schedule(this::setCurrentProvider, (int) ConstantsFor.DELAY);

        AppConfigurationLocal.getInstance().execute(syncData::superRun);

        try {
            infoForU();
        }
        catch (RuntimeException e) {
            messageToUser.error("AppInfoOnLoad.run", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
        }
        finally {
            checkFileExitLastAndWriteMiniLog();
            if (Runtime.getRuntime().freeMemory() > (350 * ConstantsFor.MBYTE) && NetScanService.isReach(OtherKnownDevices.IP_SRVMYSQL_HOME)) {
                SyncData syncDataBcp = SyncData.getInstance(SyncData.BACKUPER);
                AppConfigurationLocal.getInstance().execute(syncDataBcp::superRun, 600);
            }
        }
    }

    private void setCurrentProvider() {
        try {
            NetKeeper.setCurrentProvider(new Tracerouting().call());
        }
        catch (Exception e) {
            NetKeeper.setCurrentProvider("<br><a href=\"/makeok\">" + e.getMessage() + "</a><br>");
            Thread.currentThread().interrupt();
        }
    }

    private void infoForU() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UsefulUtilities.getBuildStamp());
        String name = "AppInfoOnLoad.infoForU";
        messageToUser.info(name, ConstantsFor.STR_FINISH, " = " + stringBuilder);
        getMiniLogger().add("infoForU ends. now ftpUploadTask(). Result: " + stringBuilder);
        try {
            Runnable runInfoForU = ()->FileSystemWorker
                    .writeFile("inetstats.tables", InformationFactory.getInstance(InformationFactory.DATABASE_INFO).getInfoAbout(FileNames.DIR_INETSTATS));
            messageToUser.info(UsefulUtilities.getIISLogSize());
            AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(runInfoForU);
        }
        catch (NullPointerException e) {
            messageToUser.error(MessageFormat.format("AppInfoOnLoad.infoForU threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        finally {
            AppConfigurationLocal.getInstance().execute(onStartTasksLoader);
        }

    }

    private void checkFileExitLastAndWriteMiniLog() {
        StringBuilder exitLast = new StringBuilder();
        if (new File("exit.last").exists()) {
            exitLast.append(AbstractForms.fromArray(FileSystemWorker.readFileToList("exit.last")));
        }
        getMiniLogger().add(exitLast.toString());
        FileSystemWorker.writeFile(this.getClass().getSimpleName() + ".mini", getMiniLogger().stream());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AppInfoOnLoad{");
        sb.append(", thisDelay=").append(thisDelay);
        sb.append(", thisPC=").append(UsefulUtilities.thisPC());
        sb.append("<br>").append(new TForms().fromArray(getMiniLogger(), true));
        sb.append('}');
        return sb.toString();
    }

    @Contract(pure = true)
    public static List<String> getMiniLogger() {
        return MINI_LOGGER;
    }

}
