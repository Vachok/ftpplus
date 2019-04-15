package ru.vachok.networker.systray;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;


/**
 Добавляет иконку приложения в System Tray
 <p>
 Если трэй доступен.

 @since 29.09.2018 (22:33) */
@SuppressWarnings ("InjectedReferences") public class SystemTrayHelper {

    /**
     Путь к папке со значками
     */
    @SuppressWarnings ("InjectedReferences")
    private static final @NotNull String IMG_FOLDER_NAME = "/static/images/";

    private static final String CLASS_NAME = SystemTrayHelper.class.getSimpleName();

    private static final SystemTrayHelper SYSTEM_TRAY_HELPER = new SystemTrayHelper();

    private @NotNull TrayIcon trayIcon;

    private static MessageToUser messageToUser = new MessageCons(SystemTrayHelper.class.getSimpleName());

    public static SystemTrayHelper getI() {
        return SYSTEM_TRAY_HELPER;
    }

    /**
     Конструктор по-умолчанию
     */
    private SystemTrayHelper() {
        if (!IntoApplication.TRAY_SUPPORTED) throw new UnsupportedOperationException(System.getProperty("os.name"));
    }

    TrayIcon getTrayIcon() throws ExceptionInInitializerError {
        if (SystemTray.isSupported()) {
            return trayIcon;
        }
        else{
            throw new UnsupportedOperationException("System tray unavailable");
        }
    }

    @SuppressWarnings ("StaticMethodOnlyUsedInOneClass")
    public void addTray(String iconFileName) {
        addTray(iconFileName, true);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemTrayHelper{");
        sb.append("IMG_FOLDER_NAME='").append(IMG_FOLDER_NAME).append('\'');
        sb.append(ConstantsFor.TOSTRING_CLASS_NAME).append(CLASS_NAME).append('\'');
        sb.append(", trayIcon=").append(trayIcon.hashCode());
        sb.append('}');
        return sb.toString();
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
        }
        throw new IllegalArgumentException();
    }


    /**
     Добавление компонентов в меню
     <p>
     @return {@link PopupMenu}
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

        if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
            MenuItem reloadContext = new MenuItem();
            reloadContext.addActionListener(new ActionTests());
            reloadContext.setLabel("Run tests");
            popupMenu.add(reloadContext);
        }

        delFiles.addActionListener(new ActionDelTMP(Executors.newSingleThreadExecutor(), delFiles, popupMenu));
        delFiles.setLabel("Clean last year");
        popupMenu.add(delFiles);

        logToFilesystem.setLabel("Get some info");
        logToFilesystem.addActionListener(new ActionSomeInfo());
        popupMenu.add(logToFilesystem);
        return popupMenu;
    }

    private boolean addTrayToSys(boolean isNeedTray) {
        try{
            if (isNeedTray && SystemTray.isSupported()) {
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
            return InetAddress.getByName(ConstantsFor.HOSTNAME_SRVGITEATMEATRU).isReachable(1000);
        }
        catch(IOException e){
            throw new IllegalStateException("***Network Problems Detected***");
        }
    }

    void delOldActions() {
        ActionListener[] actionListeners;
        if(trayIcon.getActionListeners()!=null){
            actionListeners = trayIcon.getActionListeners();
            for(ActionListener actionListener : actionListeners){
                trayIcon.removeActionListener(actionListener);
            }
        }
    }


    /**
     * Создаёт System Tray Icon
     *
     * @param imageFileName имя файла-картинки
     * @param isNeedTray    если трэй не нужен.
     */
    private void addTray( String imageFileName , boolean isNeedTray ) {
        trayIcon = new TrayIcon(getImage(imageFileName) , ConstantsFor.DELAY + " delay" , getMenu());
        trayIcon.setImage(getImage(imageFileName));
        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionDefault());

        boolean isTrayAdded = addTrayToSys(isNeedTray);
        messageToUser.info("SystemTrayHelper.addTray" , "isTrayAdded" , String.valueOf(isTrayAdded));
    }
}
