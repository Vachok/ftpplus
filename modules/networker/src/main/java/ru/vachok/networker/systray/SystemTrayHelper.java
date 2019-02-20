package ru.vachok.networker.systray;


import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppInfoOnLoad;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.ArchivesAutoCleaner;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.MyServer;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.Putty;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


/**
 Добавляет иконку приложения в System Tray
 <p>
 Если трэй доступен.

 @since 29.09.2018 (22:33) */
public final class SystemTrayHelper extends AppInfoOnLoad {

    /**
     Путь к папке со значками
     */
    private static final @NotNull String IMG_FOLDER_NAME = "/static/images/";

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
    private static final @NotNull SystemTrayHelper SYSTEM_TRAY_HELPER = new SystemTrayHelper(ConstantsFor.ICON_FILE_NAME);

    private static final boolean IS_MY_PC = THIS_PC.toLowerCase().contains(ConstantsFor.NO0027) || THIS_PC.equalsIgnoreCase("home");

    private static final String CLASS_NAME = SystemTrayHelper.class.getSimpleName();

    private static String iconFileName;

    private static @NotNull TrayIcon trayIcon;
    
    /**
     {@link MessageLocal}
     */
    private static MessageToUser messageToUser = new MessageLocal();

    private static @NotNull Image image = getImage(iconFileName);

    static TrayIcon getTrayIcon() throws ExceptionInInitializerError {
        if(ConstantsFor.IS_SYS_TRAY_AVAIL){
            return trayIcon;
        }
        else{
            throw new UnsupportedOperationException();
        }
    }

    /**
     Добавление компонентов в меню
     <p>
     1.5 {@link ArchivesAutoCleaner}
     */
    private static PopupMenu getMenu() {
        PopupMenu popupMenu = new PopupMenu();
        String classMeth = CLASS_NAME + ".getMenu";
        Thread thread = AppComponents.threadConfig().createThread(SystemTrayHelper::recOn);
        MenuItem defItem = new MenuItem();
        MenuItem gitStartWeb = new MenuItem();
        MenuItem toConsole = new MenuItem();
        MenuItem delFiles = new MenuItem();
        MenuItem logToFilesystem = new MenuItem();
        thread.start();
        defItem.setLabel("Exit");
        defItem.addActionListener(new ActionExit(classMeth));
        popupMenu.add(defItem);

        gitStartWeb.addActionListener(new ActionGITStart(AppComponents.threadConfig()));
        gitStartWeb.setLabel("GIT WEB ON");
        popupMenu.add(gitStartWeb);

        toConsole.setLabel("Console Back");
        toConsole.addActionListener((ActionEvent e) -> System.setOut(System.err));
        popupMenu.add(toConsole);
        if(!ConstantsFor.thisPC().toLowerCase().contains("home")){
            MenuItem puttyStarter = new MenuItem();
            puttyStarter.addActionListener((ActionEvent e) -> new Putty().start());
            puttyStarter.setLabel("Putty");
            popupMenu.add(puttyStarter);
        }
        else{
            MenuItem reloadContext = new MenuItem();
            reloadContext.addActionListener(new ActionTests());
            reloadContext.setLabel("Run tests");
            popupMenu.add(reloadContext);
        }

        delFiles.addActionListener(new ActionDelTMP(AppComponents.threadConfig().threadPoolTaskExecutor(), delFiles, popupMenu));
        delFiles.setLabel("Clean last year");
        popupMenu.add(delFiles);

        logToFilesystem.setLabel("Get some info");
        logToFilesystem.addActionListener(new ActionSomeInfo());
        popupMenu.add(logToFilesystem);
        return popupMenu;
    }

    /**
     Reconnect Socket, пока он открыт
     <p>
     1. {@link MyServer#setSocket(java.net.Socket)}. Создаём новый {@link Socket}. <br>
     2. {@link MyServer#getSocket()} - пока он не {@code isClosed}, 3. {@link MyServer#reconSock()} реконнект. <br><br>
     {@link IOException}, {@link InterruptedException}, {@link NullPointerException} : <br>
     4. {@link TForms#fromArray(java.lang.Exception, boolean)} - преобразуем исключение в строку. <br>
     5. {@link AppComponents#threadConfig()} , 6 {@link ThreadConfig#threadPoolTaskExecutor()} перезапуск {@link MyServer#getI()}
     */
    private static void recOn() {
        MyServer.setSocket(new Socket());
        while(!MyServer.getSocket().isClosed()){
            try{
                MyServer.reconSock();
            }
            catch(IOException | InterruptedException | NullPointerException e1){
                messageToUser.errorAlert(CLASS_NAME, "recOn", e1.getMessage());
                FileSystemWorker.error("SystemTrayHelper.recOn", e1);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     Конструктор по-умолчанию
     */
    private SystemTrayHelper(String iconFileName) {
        SystemTrayHelper.iconFileName = iconFileName;
    }

    static {
        iconFileName = checkImageName(ConstantsFor.ICON_FILE_NAME, IS_MY_PC);
        trayIcon = new TrayIcon(
            image,
            new StringBuilder()
                .append(AppComponents.versionInfo().getAppBuild()).append(" v. ")
                .append(AppComponents.versionInfo().getAppVersion()).append(" ")
                .append(AppComponents.versionInfo().getBuildTime()).toString(),
            getMenu());
    }

    /**
     Создаёт System Tray Icon
     */
    @SuppressWarnings ("FeatureEnvy")
    public void addTray() {
        String classMeth = "SystemTrayHelper.addTray";
        messageToUser.info("ICON_FILE_NAME = [" + ConstantsFor.ICON_FILE_NAME + "]", "input parameters. Returns:", "void");
        messageToUser.infoNoTitles(classMeth);

        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionDefault());

        boolean isTrayAdded = addTrayToSys();
        messageToUser.info("SystemTrayHelper.addTray", "isTrayAdded", String.valueOf(isTrayAdded));

        AppInfoOnLoad.runCommonScan();
    }

    private static boolean addTrayToSys() {
        boolean retBool = SystemTray.isSupported();
        try{
            if(retBool){
                SystemTray systemTray = SystemTray.getSystemTray();
                systemTray.add(trayIcon);
                retBool = systemTray.getTrayIcons().length > 0;
            }
            else{
                LOGGER.warn("Tray not supported!");
                retBool = false;
            }
        }
        catch(AWTException e){
            messageToUser.errorAlert(CLASS_NAME, "addTrayToSys", e.getMessage());
            FileSystemWorker.error("SystemTrayHelper.addTrayToSys", e);
        }

        return retBool;
    }

    /**
     @return {@link #SYSTEM_TRAY_HELPER}
     @param s
     */
    public static SystemTrayHelper getInstance(String s) {
        iconFileName = s;
        return SYSTEM_TRAY_HELPER;
    }

    static void delOldActions() {
        if(ConstantsFor.IS_SYS_TRAY_AVAIL){
            ActionListener[] actionListeners = trayIcon.getActionListeners();
            for(ActionListener actionListener : actionListeners){
                trayIcon.removeActionListener(actionListener);
            }
        }
        else{
            throw new UnsupportedOperationException();
        }
    }

    private static Image getImage(String iconFileName) {
        try{
            return Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(iconFileName));
        }
        catch(Exception e){
            messageToUser.errorAlert(CLASS_NAME, "getImage", e.getMessage());
            FileSystemWorker.error("SystemTrayHelper.getImage", e);
        }
        throw new IllegalArgumentException();
    }

    private static String checkImageName(String iconFileName, boolean myPC) {
        if(iconFileName==null){
            iconFileName = "icons8-ip-адрес-15.png";
        }
        else{
            if(myPC){
                iconFileName = "icons8-плохие-поросята-48.png";
            }
        }
        if(srvGitIs()){
            iconFileName = "icons8-отменить-2-20.png";
        }
        messageToUser.info("SystemTrayHelper.checkImageName", "IMG_FOLDER_NAME", IMG_FOLDER_NAME);
        messageToUser.info("SystemTrayHelper.checkImageName", "ICON_FILE_NAME", iconFileName);
        return new StringBuilder().append(IMG_FOLDER_NAME).append(iconFileName).toString();
    }

    /**
     Проверка доступности <a href="http://srv-git.eatmeat.ru:1234">srv-git.eatmeat.ru</a>
     <p>

     @return srv-git online
     */
    private static boolean srvGitIs() {
        try{
            return !InetAddress.getByName(ConstantsFor.SRV_GIT_EATMEAT_RU).isReachable(1000);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
            return true;
        }
    }
}
