package ru.vachok.networker.systray;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.common.ArchivesAutoCleaner;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.Putty;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;


/**
 Добавляет иконку приложения в System Tray
 <p>
 Если трэй доступен.

 @since 29.09.2018 (22:33) */
@SuppressWarnings ("InjectedReferences")
public final class SystemTrayHelper {

    /**
     Путь к папке со значками
     */
    @SuppressWarnings ("InjectedReferences")
    private static final @NotNull String IMG_FOLDER_NAME = "/static/images/";

    private static final String CLASS_NAME = SystemTrayHelper.class.getSimpleName();

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = new SystemTrayHelper();

    private static @NotNull TrayIcon trayIcon = null;

    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal();

    public static SystemTrayHelper getI() {
        return SYSTEM_TRAY_HELPER;
    }

    /**
     Конструктор по-умолчанию
     */
    private SystemTrayHelper() {
    }

    TrayIcon getTrayIcon() throws ExceptionInInitializerError {
        if(ConstantsFor.IS_SYSTRAY_AVAIL){
            return trayIcon;
        }
        else{
            throw new IllegalComponentStateException("System tray unavailable");
        }
    }

    @SuppressWarnings ("StaticMethodOnlyUsedInOneClass")
    public void addTray(String iconFileName) {
        addTray(iconFileName, true);
    }

    /**
     Создаёт System Tray Icon
     */
    private void addTray(String imageFileName, boolean isNeedTray) {
        trayIcon = new TrayIcon(
            getImage(imageFileName),
            new StringBuilder()
                .append(AppComponents.versionInfo().getAppBuild()).append(" v. ")
                .append(AppComponents.versionInfo().getAppVersion()).append(" ")
                .append(AppComponents.versionInfo().getBuildTime()).toString(),
            getMenu());
        trayIcon.setImage(getImage(imageFileName));
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionDefault());

        boolean isTrayAdded = addTrayToSys(isNeedTray);
        messageToUser.info("SystemTrayHelper.addTray", "isTrayAdded", String.valueOf(isTrayAdded));
    }

    private static Image getImage(String iconFileName) {
        if(!isSrvGitOK()){
            iconFileName = "icons8-disconnected-24.png";
        }
        try{
            return Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(IMG_FOLDER_NAME + iconFileName));
        }
        catch(Exception e){
            messageToUser.errorAlert(CLASS_NAME, "getImage", e.getMessage());
            FileSystemWorker.error("SystemTrayHelper.getImage", e);
        }
        throw new IllegalArgumentException();
    }

    /**
     Добавление компонентов в меню
     <p>
     1.5 {@link ArchivesAutoCleaner}
     */
    @SuppressWarnings ("OverlyLongMethod")
    private static PopupMenu getMenu() {
        PopupMenu popupMenu = new PopupMenu();
        String classMeth = CLASS_NAME + ".getMenu";
        MenuItem defItem = new MenuItem();
        MenuItem gitStartWeb = new MenuItem();
        MenuItem toConsole = new MenuItem();
        MenuItem delFiles = new MenuItem();
        MenuItem logToFilesystem = new MenuItem();

        defItem.setLabel("Exit");
        defItem.addActionListener(new ActionExit(classMeth));
        popupMenu.add(defItem);

        gitStartWeb.addActionListener(new ActionGITStart());
        gitStartWeb.setLabel("GIT WEB ON");
        popupMenu.add(gitStartWeb);

        toConsole.setLabel("Console Back");
        toConsole.addActionListener((ActionEvent e) -> System.setOut(System.err));
        popupMenu.add(toConsole);

        if(ConstantsFor.thisPC().toLowerCase().contains("home")){
            MenuItem reloadContext = new MenuItem();
            reloadContext.addActionListener(new ActionTests());
            reloadContext.setLabel("Run tests");
            popupMenu.add(reloadContext);
        }
        else{
            MenuItem puttyStarter = new MenuItem();
            puttyStarter.addActionListener((ActionEvent e) -> new Putty().start());
            puttyStarter.setLabel("Putty");
            popupMenu.add(puttyStarter);
        }

        delFiles.addActionListener(new ActionDelTMP(AppComponents.threadConfig().getTaskExecutor(), delFiles, popupMenu));
        delFiles.setLabel("Clean last year");
        popupMenu.add(delFiles);

        logToFilesystem.setLabel("Get some info");
        logToFilesystem.addActionListener(new ActionSomeInfo());
        popupMenu.add(logToFilesystem);
        return popupMenu;
    }

    private boolean addTrayToSys(boolean isNeedTray) {
        try{
            if(isNeedTray && ConstantsFor.IS_SYSTRAY_AVAIL){
                SystemTray systemTray = SystemTray.getSystemTray();
                systemTray.add(trayIcon);
                isNeedTray = systemTray.getTrayIcons().length > 0;
            }
            else{
                messageToUser.warn("Tray not supported!");
                isNeedTray = false;
            }
        }
        catch(AWTException e){
            messageToUser.errorAlert(CLASS_NAME, "addTrayToSys", e.getMessage());
            FileSystemWorker.error("SystemTrayHelper.addTrayToSys", e);
            isNeedTray = false;
        }
        return isNeedTray;
    }

    /**
     Проверка доступности <a href="http://srv-git.eatmeat.ru:1234">srv-git.eatmeat.ru</a>
     <p>

     @return srv-git online
     */
    private static boolean isSrvGitOK() {
        try{
            return InetAddress.getByName(ConstantsFor.HOSTNAME_SRVGIT_EATMEATRU).isReachable(1000);
        }
        catch(IOException e){
            throw new IllegalStateException("***Network Problems Detected***");
        }
    }

    void delOldActions() {
        Thread.currentThread().setName(CLASS_NAME + ".delOldActions");
        ActionListener[] actionListeners;
        if(trayIcon.getActionListeners()!=null){
            actionListeners = trayIcon.getActionListeners();
            for(ActionListener actionListener : actionListeners){
                trayIcon.removeActionListener(actionListener);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemTrayHelper{");
        sb.append("IMG_FOLDER_NAME='").append(IMG_FOLDER_NAME).append('\'');
        sb.append(", CLASS_NAME='").append(CLASS_NAME).append('\'');
        sb.append(", trayIcon=").append(trayIcon.hashCode());
        sb.append(", messageToUser=").append(messageToUser.toString());
        sb.append('}');
        return sb.toString();
    }
}
