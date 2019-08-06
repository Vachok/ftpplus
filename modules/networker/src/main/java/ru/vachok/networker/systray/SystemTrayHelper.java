// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.SwitchesWiFi;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.actions.ActionSomeInfo;
import ru.vachok.networker.systray.actions.ActionExit;
import ru.vachok.networker.systray.actions.ActionMakeInfoAboutOldCommonFiles;
import ru.vachok.networker.systray.actions.ActionOpenProgFolder;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 Добавляет иконку приложения в System Tray
 <p>
 Если трэй доступен.
 
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {
    
    
    public static final String TOSTRING_CLASS_NAME = ", CLASS_NAME='";
    
    /**
     Путь к папке со значками
     */
    @SuppressWarnings("InjectedReferences")
    private static final @NotNull String IMG_FOLDER_NAME = "/static/images/";
    
    private static final String CLASS_NAME = SystemTrayHelper.class.getSimpleName();
    
    private static final MessageToUser messageToUser = new MessageCons(SystemTrayHelper.class.getSimpleName());
    
    private static SystemTrayHelper trayHelper = new SystemTrayHelper();
    
    private TrayIcon trayIcon;
    
    /**
     Конструктор по-умолчанию
     */
    protected SystemTrayHelper() {
        if (!IntoApplication.TRAY_SUPPORTED) {
            System.err.println(System.getProperty("os.name"));
        }
    }
    static {
        try {
            trayHelper = new SystemTrayHelper();
        }
        catch (RuntimeException e) {
            messageToUser.error(FileSystemWorker.error(SystemTrayHelper.class.getSimpleName() + ConstantsFor.STATIC_INITIALIZER, e));
        }
    }
    
    
    @Contract(pure = true)
    public static Optional getI() {
        return Optional.ofNullable(trayHelper);
    }
    
    public TrayIcon getTrayIcon() throws ExceptionInInitializerError {
        if (SystemTray.isSupported()) {
            return trayIcon;
        }
        else {
            throw new UnsupportedOperationException("System tray unavailable");
        }
    }
    
    public void addTray(String iconFileName) {
        addTray(iconFileName, true);
    }
    
    public void delOldActions() {
        ActionListener[] actionListeners;
        if (trayIcon.getActionListeners() != null) {
            actionListeners = trayIcon.getActionListeners();
            for (ActionListener actionListener : actionListeners) {
                trayIcon.removeActionListener(actionListener);
            }
        }
    }
    
    public static void trayAdd(SystemTrayHelper systemTrayHelper) {
        if (ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.HOSTNAME_DO213)) {
            systemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        }
        else {
            if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
                systemTrayHelper.addTray("icons8-house-26.png");
            }
            else {
                try {
                    systemTrayHelper.addTray(FileNames.FILENAME_ICON);
                }
                catch (Exception ignore) {
                    //
                }
            }
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemTrayHelper{");
        sb.append("IMG_FOLDER_NAME='").append(IMG_FOLDER_NAME).append('\'');
        sb.append(TOSTRING_CLASS_NAME).append(CLASS_NAME).append('\'');
        sb.append(", trayIcon=").append(trayIcon.hashCode());
        sb.append('}');
        return sb.toString();
    }
    
    private static Image getImage(String iconFileName) {
        if (!isSrvGitOK()) {
            iconFileName = "icons8-disconnected-24.png";
        }
        try {
            return Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(IMG_FOLDER_NAME + iconFileName));
        }
        catch (Exception e) {
            messageToUser.errorAlert(CLASS_NAME, "getImage", e.getMessage());
        }
        throw new IllegalArgumentException();
    }
    
    /**
     Добавление компонентов в меню
     <p>
     
     @return {@link PopupMenu}
     */
    @SuppressWarnings("OverlyLongMethod")
    private static @NotNull PopupMenu getMenu() {
        PopupMenu popupMenu = new PopupMenu();
        String classMeth = CLASS_NAME + ".getMenu";
        MenuItem defItem = new MenuItem();
        MenuItem openSite = new MenuItem();
        MenuItem toConsole = new MenuItem();
        MenuItem openFolder = new MenuItem();
        MenuItem logToFilesystem = new MenuItem();
        MenuItem oldFilesGenerator = new MenuItem();
        
        defItem.setLabel("Exit");
        defItem.addActionListener(new ActionExit(classMeth));
        popupMenu.add(defItem);
    
        openSite.addActionListener(new ActionDefault());
        openSite.setLabel("Open site");
        popupMenu.add(openSite);
        
        toConsole.setLabel("Console Back");
        toConsole.addActionListener(e->System.setOut(System.err));
        popupMenu.add(toConsole);
        
        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            MenuItem testActions = new MenuItem();
            testActions.setLabel("Run tests");
            popupMenu.add(testActions);
        }
    
        openFolder.addActionListener(new ActionOpenProgFolder());
        openFolder.setLabel("Open root program folder");
        popupMenu.add(openFolder);
        
        logToFilesystem.setLabel("Get some info");
        logToFilesystem.addActionListener(new ActionSomeInfo());
        popupMenu.add(logToFilesystem);
    
        ActionMakeInfoAboutOldCommonFiles makeOldFilesInfoAct = new ActionMakeInfoAboutOldCommonFiles();
        makeOldFilesInfoAct.setTimeoutSeconds(TimeUnit.HOURS.toSeconds(9));
        oldFilesGenerator.addActionListener(makeOldFilesInfoAct);
        oldFilesGenerator.setLabel("Generate files.old");
        popupMenu.add(oldFilesGenerator);
        
        return popupMenu;
    }
    
    private boolean addTrayToSys(boolean isNeedTray) {
        try {
            if (isNeedTray && SystemTray.isSupported()) {
                SystemTray systemTray = SystemTray.getSystemTray();
                systemTray.add(trayIcon);
                isNeedTray = systemTray.getTrayIcons().length > 0;
            }
            else {
                messageToUser.warn("Tray not supported!");
                isNeedTray = false;
            }
        }
        catch (AWTException e) {
            messageToUser.errorAlert(CLASS_NAME, "addTrayToSys", e.getMessage());
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
        try {
            return InetAddress.getByName(SwitchesWiFi.HOSTNAME_SRVGITEATMEATRU).isReachable(1000);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("SystemTrayHelper.isSrvGitOK: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    /**
     Создаёт System Tray Icon
 
     @param imageFileName имя файла-картинки
     @param isNeedTray если трэй не нужен.
     */
    private void addTray(String imageFileName, boolean isNeedTray) {
        trayIcon = new TrayIcon(getImage(imageFileName), ConstantsFor.DELAY + " delay", getMenu());
        trayIcon.setImage(getImage(imageFileName));
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionDefault());
    
        boolean isTrayAdded = addTrayToSys(isNeedTray);
        messageToUser.info("SystemTrayHelper.addTray", "isTrayAdded", String.valueOf(isTrayAdded));
    }
}
