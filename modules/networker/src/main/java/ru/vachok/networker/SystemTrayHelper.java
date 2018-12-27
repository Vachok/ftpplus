package ru.vachok.networker;


import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.accesscontrol.common.ArchivesAutoCleaner;
import ru.vachok.networker.accesscontrol.common.CommonRightsChecker;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.services.MyCalen;
import ru.vachok.networker.services.Putty;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

import static java.lang.System.err;


/**
 Добавляет иконку приложения в System Tray
 <p>
 Если трэй доступен.

 @since 29.09.2018 (22:33) */
@SuppressWarnings("InjectedReferences")
public final class SystemTrayHelper {

    /**
     Путь к папке со значками
     */
    private static final String IMG_FOLDER_NAME = "/static/images/";

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     Имя ПК, где запущено приложение
     <p>
     {@link ConstantsFor#thisPC()}
     */
    private static final String THIS_PC = ConstantsFor.thisPC();

    /**
     Instance
     */
    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = new SystemTrayHelper();

    /**
     {@link DBMessenger}
     */
    private static final MessageToUser MESSAGE_TO_USER = new DBMessenger();

    /**
     @return {@link #SYSTEM_TRAY_HELPER}
     */
    public static SystemTrayHelper getInstance() {
        return SYSTEM_TRAY_HELPER;
    }

    /**
     Конструктор по-умолчанию
     */
    private SystemTrayHelper() {

    }

    /**
     Создаёт System Tray Icon
     <p>
     Usages: {@link IntoApplication#main(String[])} <br> Uses: 1.1 {@link #srvGitIs()}, 1.2 {@link AppComponents#versionInfo()}, 1.3 {@link AppComponents#versionInfo()}, 1.4 {@link
    AppComponents#versionInfo()}, 1.5 {@link ConstantsFor#saveProps(Properties)}, 1.6 {@link FileSystemWorker#delTemp()}, 1.7 {@link #addItems(PopupMenu)} .

     @param iconFileName имя файла-иконки.
     */
    @SuppressWarnings("FeatureEnvy")
    static void addTray(String iconFileName) {
        boolean myPC;
        runCommonScan(false);
        myPC = THIS_PC.toLowerCase().contains(ConstantsFor.NO0027) || THIS_PC.equalsIgnoreCase("home");
        if (iconFileName == null) {
            iconFileName = "icons8-ip-адрес-15.png";
        } else {
            if (myPC) {
                iconFileName = "icons8-плохие-поросята-48.png";
            }
        }
        if (srvGitIs()) {
            iconFileName = "icons8-отменить-2-20.png";
        }
        iconFileName = new StringBuilder().append(IMG_FOLDER_NAME).append(iconFileName).toString();

        Image image = Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(iconFileName));
        PopupMenu popupMenu = new PopupMenu();
        MenuItem defItem = new MenuItem();
        TrayIcon trayIcon = new TrayIcon(image,
            new StringBuilder().append(AppComponents.versionInfo().getAppBuild()).append(" v. ").append(AppComponents.versionInfo().getAppVersion()).append(" ").append(AppComponents.versionInfo().getBuildTime()).toString(), popupMenu);
        ActionListener actionListener = e -> {
            try {
                Desktop.getDesktop().browse(URI.create("http://localhost:8880"));
            } catch (IOException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        ActionListener exitApp = e -> new ThreadConfig().threadPoolTaskExecutor()
            .execute(new ExitApp(SystemTrayHelper.class.getSimpleName()));

        addItems(popupMenu);
        trayIcon.setImageAutoSize(true);
        defItem.setLabel("Exit");
        defItem.addActionListener(exitApp);
        popupMenu.add(defItem);
        trayIcon.addActionListener(actionListener);
        try {
            if (SystemTray.isSupported()) {
                SystemTray systemTray = SystemTray.getSystemTray();
                systemTray.add(trayIcon);
            } else {
                LOGGER.warn("Tray not supported!");
                Thread.currentThread().interrupt();
            }
        } catch (AWTException e) {
            LOGGER.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     Запускает сканнер прав Common
     <p>
     Usages: {@link #addItems(PopupMenu)}
     */
    private static void runCommonScan(boolean runNow) {
        Runnable r = () -> {
            try {
                CommonRightsChecker commonRightsChecker = new CommonRightsChecker();
                Files.walkFileTree(Paths.get("\\\\srv-fs.eatmeat.ru\\common_new"), commonRightsChecker);
            } catch (IOException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        };
        Date startTime = MyCalen.getNextSat(0, 1);
        long delay = TimeUnit.DAYS.toMillis(ConstantsFor.ONE_MONTH_DAYS);
        if (runNow) Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(r);
        else {
            ScheduledFuture<?> scheduleWithFixedDelay = new ThreadConfig().threadPoolTaskScheduler().scheduleWithFixedDelay(
                r, startTime, delay);
            new Thread(() -> {
                try {
                    scheduleWithFixedDelay.get();
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     Проверка доступности <a href="http://srv-git.eatmeat.ru:1234">srv-git.eatmeat.ru</a>
     <p>
     Usages: {@link #addTray(String)} <br> Uses: -

     @return srv-git online
     */
    private static boolean srvGitIs() {
        try {
            return !InetAddress.getByName(ConstantsFor.SRV_GIT_EATMEAT_RU).isReachable(1000);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return true;
        }
    }

    /**
     Добавление компонентов в меню
     <p>
     Usages: {@link #addTray(String)} <br> Uses: 1.1 {@link ThreadConfig#threadPoolTaskExecutor()}, 1.2 {@link SSHFactory.Builder#build()}, 1.3 {@link SSHFactory.Builder} 1.4 {@link SSHFactory#call()},
     1.5 {@link ArchivesAutoCleaner}

     @param popupMenu {@link PopupMenu}
     */
    private static void addItems(PopupMenu popupMenu) {
        ThreadConfig threadConfig = new ThreadConfig();
        ThreadPoolTaskExecutor executor = threadConfig.threadPoolTaskExecutor();
        Thread thread = executor.createThread(SystemTrayHelper::recOn);
        thread.start();
        int timeOut30 = 30;
        MenuItem gitStartWeb = new MenuItem();
        gitStartWeb.addActionListener(actionEvent -> {
            Callable<String> sshStr = () -> new SSHFactory.Builder(ConstantsFor
                .SRV_GIT, new StringBuilder()
                .append("sudo git instaweb;")
                .append("sudo cd /usr/home/dpetrov/;")
                .append("sudo git instaweb -p 11111;")
                .append("sudo cd /usr/home/kudr/;")
                .append("sudo git instaweb -p 9999;")
                .append("exit;")
                .toString()).build().call();
            Future<String> submit = executor.submit(sshStr);
            try {
                LOGGER.info(submit.get(timeOut30, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Thread.currentThread().interrupt();
            }
        });
        gitStartWeb.setLabel("GIT WEB ON");
        popupMenu.add(gitStartWeb);

        MenuItem toConsole = new MenuItem();
        toConsole.setLabel("Console Back");
        toConsole.addActionListener(e -> System.setOut(err));
        popupMenu.add(toConsole);

        if (!ConstantsFor.thisPC().toLowerCase().contains("home")) {
            MenuItem puttyStarter = new MenuItem();
            puttyStarter.addActionListener(e -> new Putty().start());
            puttyStarter.setLabel("Putty");
            popupMenu.add(puttyStarter);
        } else {
            MenuItem noPutty = new MenuItem();
            noPutty.addActionListener(e -> {
                IntoApplication.getConfigurableApplicationContext().close();
                Set<Object> allSources = IntoApplication.getSpringApplication().getAllSources();
                executor.getThreadPoolExecutor().shutdown();
                threadConfig.killAll();
                boolean writeArray = new TForms().writeArray(allSources, SystemTrayHelper.class.getSimpleName());
                String msg = new StringBuilder().append("Write allSources set is ").append(writeArray).toString();
                LOGGER.warn(msg);
                Executors.unconfigurableExecutorService(Executors.newSingleThreadScheduledExecutor())
                    .execute(() -> IntoApplication.main(new String[0]));
            });
            noPutty.setLabel("Refresh App Context");
            popupMenu.add(noPutty);
        }

        MenuItem delFiles = new MenuItem();
        delFiles.addActionListener(e -> {
            Date date = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(ConstantsFor.ONE_YEAR));
            String msg = (new StringBuilder().append("starting clean for ").append(date).toString()).toUpperCase();

            executor.setThreadGroup(new ThreadGroup(("CLR")));
            executor.setThreadNamePrefix("CLEAN");
            executor.setThreadNamePrefix(date + "-");
            executor.execute(new ArchivesAutoCleaner());

            delFiles.setLabel("Autoclean");
            popupMenu.add(delFiles);

            LOGGER.info(msg);
        });
        delFiles.setLabel("Clean last year");
        popupMenu.add(delFiles);

        MenuItem logToFilesystem = new MenuItem();
        logToFilesystem.setLabel("Common Rights");
        logToFilesystem.addActionListener(e -> new ThreadConfig().threadPoolTaskExecutor().execute(SystemTrayHelper::runCommonScan));
        popupMenu.add(logToFilesystem);
    }

    /**
     Reconnect Socket, пока он открыт
     <p>
     Usages: {@link #addItems(PopupMenu)} <br> Uses: 1.1 {@link ConstantsFor#checkDay()}, 1.2 {@link MyServer#reconSock()}, 1.3 {@link TForms#fromArray(Exception, boolean)}, 1.4 {@link
    ThreadConfig#threadPoolTaskExecutor()}
     */
    private static void recOn() {
        String bSTR = ConstantsFor.checkDay() + " pcuserauto truncated";
        LOGGER.warn(bSTR);
        MyServer.setSocket(new Socket());
        while (!MyServer.getSocket().isClosed()) {
            try {
                MyServer.reconSock();
            } catch (IOException | InterruptedException | NullPointerException e1) {
                MESSAGE_TO_USER.errorAlert(SystemTrayHelper.class.getSimpleName(), e1.getMessage(), new TForms().fromArray(e1, false));
                new ThreadConfig().threadPoolTaskExecutor().submit(MyServer.getI());
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void runCommonScan() {
        runCommonScan(true);
    }
}
