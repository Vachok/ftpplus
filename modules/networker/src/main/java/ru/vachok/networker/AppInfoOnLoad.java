// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.Contract;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherKnownDevices;
import ru.vachok.networker.data.synchronizer.SyncData;
import ru.vachok.networker.firebase.RealTimeChildListener;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.net.ssh.Tracerouting;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.AppInfoOnLoadTest
 @since 19.12.2018 (9:40) */
@SuppressWarnings("ClassUnconnectedToPackage")
public class AppInfoOnLoad implements Runnable {


    private static final AppInfoOnLoad INST = new AppInfoOnLoad();

    private static final String AVAILABLECHARSETS_TXT = "availableCharsets.txt";

    private static final List<String> MINI_LOGGER = new ArrayList<>();

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, AppInfoOnLoad.class.getSimpleName());

    private final AppConfigurationLocal scheduleDefiner = AppConfigurationLocal.getInstance(AppConfigurationLocal.SCHEDULE_DEFINER);

    private final AppConfigurationLocal onStartTasksLoader = AppConfigurationLocal.getInstance(AppConfigurationLocal.ON_START_LOADER);

    private static final int THIS_DELAY = UsefulUtilities.getScansDelay();

    public static Runnable getI() {
        return INST;
    }

    private AppInfoOnLoad() {
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        String avCharsetsStr = AbstractForms.fromArray(Charset.availableCharsets());
        FileSystemWorker.writeFile(AVAILABLECHARSETS_TXT, avCharsetsStr);
        SyncData syncData = SyncData.getInstance(SyncData.INETSYNC);

        AppConfigurationLocal.getInstance().execute(scheduleDefiner);

        AppConfigurationLocal.getInstance().schedule(this::setCurrentProvider, (int) ConstantsFor.DELAY);

        AppConfigurationLocal.getInstance().execute(syncData::superRun);

        try {
            infoForU();
        }
        catch (RuntimeException e) {
            messageToUser.warn(AppInfoOnLoad.class.getSimpleName(), e.getMessage(), " see line: 61 ***");
        }
        finally {
            boolean isMemOk = Runtime.getRuntime().freeMemory() > (350 * ConstantsFor.MBYTE);
            messageToUser.info(getClass().getSimpleName(), "isMemOk", isMemOk + ": " + Runtime.getRuntime().freeMemory() / ConstantsFor.MBYTE);
            if (NetScanService.isReach(OtherKnownDevices.IP_SRVMYSQL_HOME)) {
                SyncData syncDataBcp = SyncData.getInstance(SyncData.BACKUPER);
                AppConfigurationLocal.getInstance().execute(syncDataBcp::superRun, 3600);
            }
            else {
                MessageToUser.getInstance(MessageToUser.EMAIL, this.getClass().getSimpleName())
                    .error(this.getClass().getSimpleName(), "Sync not running", UsefulUtilities.getRunningInformation());
            }
            toFirebase();
            checkFileExitLastAndWriteMiniLog();
        }
    }

    private void setCurrentProvider() {
        try {
            String currentProviderName = (String) AppConfigurationLocal.getInstance().executeGet(new Tracerouting(), 10);
            NetKeeper.setCurrentProvider(currentProviderName);
        }
        catch (Exception e) {
            NetKeeper.setCurrentProvider("<br><a href=\"/makeok\">" + e.getMessage() + "</a><br>");
            Thread.currentThread().interrupt();
        }
    }

    private void infoForU() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UsefulUtilities.getBuildStamp());
        getMiniLogger().add("infoForU ends. now ftpUploadTask(). Result: " + stringBuilder);
        try {
            Runnable runInfoForU = ()->FileSystemWorker
                .writeFile("inetstats.tables", InformationFactory.getInstance(InformationFactory.DATABASE_INFO).getInfoAbout(FileNames.DIR_INETSTATS));
            messageToUser.info(UsefulUtilities.getIISLogSize());
            AppConfigurationLocal.getInstance().execute(runInfoForU);
        }
        catch (RuntimeException e) {
            messageToUser.warn(AppInfoOnLoad.class.getSimpleName(), e.getMessage(), " see line: 100 ***");
        }
        finally {
            AppConfigurationLocal.getInstance().execute(onStartTasksLoader);
        }

    }

    @Contract(pure = true)
    public static List<String> getMiniLogger() {
        return MINI_LOGGER;
    }

    private void toFirebase() {
        FirebaseApp app = AppComponents.getFirebaseApp();
        FirebaseDatabase.getInstance().getReference(UsefulUtilities.thisPC().replace(".", "_"))
            .setValue(MessageFormat.format("{0} : {1}", new Date().toString(), app.toString()), (error, ref)->{
                String s = ref.toString();
                System.out.println("s = " + s);
            });

        FirebaseDatabase.getInstance().getReference().addChildEventListener(new RealTimeChildListener());

        if (!UsefulUtilities.thisPC().contains("rups")) {
            FirebaseDatabase.getInstance().getReference("test")
                .removeValue((error, ref)->messageToUser
                    .error("AppInfoOnLoad.onComplete", error.toException().getMessage(), AbstractForms.networkerTrace(error.toException().getStackTrace())));
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
        sb.append(", thisDelay=").append(THIS_DELAY);
        sb.append(", thisPC=").append(UsefulUtilities.thisPC());
        sb.append("<br>").append(new TForms().fromArray(getMiniLogger(), true));
        sb.append('}');
        return sb.toString();
    }

}
