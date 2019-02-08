package ru.vachok.networker.systray;


import org.slf4j.Logger;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.*;
import ru.vachok.networker.accesscontrol.common.ArchivesAutoCleaner;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.services.DBMessenger;
import ru.vachok.networker.services.Putty;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;


/**
 Добавляет иконку приложения в System Tray
 <p>
 Если трэй доступен.

 @since 29.09.2018 (22:33) */
public final class SystemTrayHelper extends AppInfoOnLoad {

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
    private static final MessageToUser MESSAGE_TO_USER = new MessageCons();

    private static TrayIcon trayIcon = null;

    /**
     Конструктор по-умолчанию
     */
    private SystemTrayHelper() {

    }

    /**
     @return {@link #SYSTEM_TRAY_HELPER}
     */
    public static SystemTrayHelper getInstance() {
        return SYSTEM_TRAY_HELPER;
    }

    /**
     Создаёт System Tray Icon
     <p>
     Usages: {@link IntoApplication#main(String[])} <br> Uses: 1.1 {@link #srvGitIs()}, 1.2 {@link AppComponents#versionInfo()}, 1.3 {@link AppComponents#versionInfo()}, 1.4 {@link
    AppComponents#versionInfo()}, 1.5 {@link ConstantsFor#saveProps(Properties)}, 1.6 {@link FileSystemWorker#delTemp()}, 1.7 {@link #addItems(PopupMenu)} .

     @param iconFileName имя файла-иконки.
     */
    @SuppressWarnings("FeatureEnvy")
    public static void addTray(String iconFileName) {
        String classMeth = "SystemTrayHelper.addTray";
        new MessageCons().info("iconFileName = [" + iconFileName + "]", "input parameters. Returns:", "void");
        new MessageCons().errorAlert(classMeth);
        boolean myPC;
        AppInfoOnLoad.runCommonScan();
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
        trayIcon = new TrayIcon(image,
            new StringBuilder().append(AppComponents.versionInfo().getAppBuild()).append(" v. ")
                .append(AppComponents.versionInfo().getAppVersion()).append(" ")
                .append(AppComponents.versionInfo().getBuildTime()).toString(), popupMenu);
        trayIcon.addActionListener(new ActionDefault());
        addItems(popupMenu);
        trayIcon.setImageAutoSize(true);
        defItem.setLabel("Exit");
        defItem.addActionListener(new ActionExit(classMeth));
        popupMenu.add(defItem);
        trayIcon.addActionListener(new ActionDefault());
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

    static void delOldActions() {
        LOGGER.warn("SystemTrayHelper.delOldActions");
        for (ActionListener actionListener : trayIcon.getActionListeners()) {
            trayIcon.removeActionListener(actionListener);
        }
    }

    /**
     Проверка доступности <a href="http://srv-git.eatmeat.ru:1234">srv-git.eatmeat.ru</a>
     <p>
     Usages: {@link #addTray(String)} <br> Uses: -

     @return srv-git online
     */
    @SuppressWarnings("MethodWithMultipleReturnPoints")
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
        Thread thread = AppComponents.threadConfig().createThread(SystemTrayHelper::recOn);
        thread.start();
        MenuItem gitStartWeb = new MenuItem();
        gitStartWeb.addActionListener(new ActionGITStart(AppComponents.threadConfig()));
        gitStartWeb.setLabel("GIT WEB ON");
        popupMenu.add(gitStartWeb);
        MenuItem toConsole = new MenuItem();
        toConsole.setLabel("Console Back");
        toConsole.addActionListener((ActionEvent e) -> System.setOut(System.err));
        popupMenu.add(toConsole);
        if (!ConstantsFor.thisPC().toLowerCase().contains("home")) {
            MenuItem puttyStarter = new MenuItem();
            puttyStarter.addActionListener((ActionEvent e) -> new Putty().start());
            puttyStarter.setLabel("Putty");
            popupMenu.add(puttyStarter);
        } else {
            MenuItem reloadContext = new MenuItem();
            reloadContext.addActionListener(new ActionTests());
            reloadContext.setLabel("Run tests");
            popupMenu.add(reloadContext);
        }
        MenuItem delFiles = new MenuItem();
        delFiles.addActionListener(new ActionDelTMP(AppComponents.threadConfig().threadPoolTaskExecutor(), delFiles, popupMenu));
        delFiles.setLabel("Clean last year");
        popupMenu.add(delFiles);
        MenuItem logToFilesystem = new MenuItem();
        logToFilesystem.setLabel("Get some info");
        logToFilesystem.addActionListener(new ActionSomeInfo());
        popupMenu.add(logToFilesystem);
    }

    /**
     Reconnect Socket, пока он открыт
     <p>
     Usages: {@link #addItems(PopupMenu)} <br> Uses: 1.1 {@link AppInfoOnLoad#checkDay()}, 1.2 {@link MyServer#reconSock()}, 1.3 {@link TForms#fromArray(Exception, boolean)}, 1.4 {@link
    ThreadConfig#threadPoolTaskExecutor()}
     */
    private static void recOn() {
        MyServer.setSocket(new Socket());
        while (!MyServer.getSocket().isClosed()) {
            try {
                MyServer.reconSock();
            } catch (IOException | InterruptedException | NullPointerException e1) {
                MESSAGE_TO_USER.errorAlert(SystemTrayHelper.class.getSimpleName(), e1.getMessage(), new TForms().fromArray(e1, false));
                AppComponents.threadConfig().threadPoolTaskExecutor().submit(MyServer.getI());
                Thread.currentThread().interrupt();
            }
        }
    }

    static TrayIcon getTrayIcon() {
        return trayIcon;
    }
}
