// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.OtherKnownDevices;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.SwitchesWiFi;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.message.DBMessenger;
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
 @see ru.vachok.networker.systray.SystemTrayHelperTest
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {
    
    
    private static final String TOSTRING_CLASS_NAME = ", CLASS_NAME='";
    
    /**
     Путь к папке со значками
     */
    @SuppressWarnings("InjectedReferences")
    private static final @NotNull String IMG_FOLDER_NAME = "/static/images/";
    
    private static final String CLASS_NAME = SystemTrayHelper.class.getSimpleName();
    
    private static final MessageToUser messageToUser = new MessageCons(SystemTrayHelper.class.getSimpleName());
    
    private final TrayIcon trayIcon;
    
    private static SystemTrayHelper trayHelper = new SystemTrayHelper();
    
    private static InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_LOGS);
    
    private String imageFileName = FileNames.ICON_DEFAULT;
    
    private boolean isNeedTray = true;
    
    /**
     Конструктор по-умолчанию
     */
    private SystemTrayHelper() {
        if (!System.getProperty("os.name").toLowerCase().contains(PropertiesNames.PR_WINDOWSOS)) {
            System.err.println(System.getProperty("os.name"));
            this.trayIcon = null;
        }
        else {
            this.trayIcon = new TrayIcon(getImage(), ConstantsFor.DELAY + " delay", getMenu());
            trayIcon.addActionListener(new ActionDefault());
        }
    }
    
    private Image getImage() {
        if (!NetScanService.isReach("10.200.200.1")) {
            this.imageFileName = "icons8-disconnected-24.png";
        }
        else {
            this.imageFileName = FileNames.ICON_DEFAULT;
        }
        try {
            return Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(IMG_FOLDER_NAME + this.imageFileName));
        }
        catch (RuntimeException e) {
            messageToUser.errorAlert(CLASS_NAME, "getImage", e.getMessage());
            return Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(IMG_FOLDER_NAME + FileNames.ICON_DEFAULT));
        }
    }
    
    /**
     Добавление компонентов в меню
     <p>
     
     @return {@link PopupMenu}
     */
    private static @NotNull PopupMenu getMenu() {
        PopupMenu popupMenu = new PopupMenu();
        String classMeth = CLASS_NAME + ".getMenu";
        MenuItem defItem = new MenuItem();
        MenuItem openSite = new MenuItem();
        MenuItem toConsole = new MenuItem();
        MenuItem openFolder = new MenuItem();
        MenuItem oldFilesGenerator = new MenuItem();
        MenuItem testActions = new MenuItem();
        
        defItem.setLabel("Exit");
        defItem.addActionListener(new ActionExit(classMeth));
        popupMenu.add(defItem);
    
        openSite.addActionListener(new ActionDefault());
        openSite.setLabel("Open site");
        popupMenu.add(openSite);
        
        toConsole.setLabel("Console Back");
        toConsole.addActionListener(e->System.setOut(System.err));
        popupMenu.add(toConsole);
    
        testActions.setLabel("Renew InetStats");
        testActions.addActionListener(e->new Thread(()->DBMessenger.getInstance("Renew InetStats").info(informationFactory.getInfo())).start());
        popupMenu.add(testActions);
    
        openFolder.addActionListener(new ActionOpenProgFolder());
        openFolder.setLabel("Open root program folder");
        popupMenu.add(openFolder);
    
        ActionMakeInfoAboutOldCommonFiles makeOldFilesInfoAct = new ActionMakeInfoAboutOldCommonFiles();
        makeOldFilesInfoAct.setTimeoutSeconds(TimeUnit.HOURS.toSeconds(9));
        oldFilesGenerator.addActionListener(makeOldFilesInfoAct);
        oldFilesGenerator.setLabel("Generate files.old");
        popupMenu.add(oldFilesGenerator);
        
        return popupMenu;
    }
    
    @Contract(pure = true)
    public static Optional getI() {
        return Optional.ofNullable(trayHelper);
    }
    
    public void trayAdd() {
        if (UsefulUtilities.thisPC().toLowerCase().contains(OtherKnownDevices.DO0213_KUDR)) {
            this.imageFileName = "icons8-плохие-поросята-32.png";
            this.addTray();
        }
        else {
            if (UsefulUtilities.thisPC().toLowerCase().contains("home")) {
                this.imageFileName = "icons8-house-26.png";
                this.addTray();
            }
            else {
                try {
                    this.addTray();
                }
                catch (RuntimeException ignore) {
                    //
                }
            }
        }
    }
    
    private void addTray() {
        synchronized(this.trayIcon) {
            this.trayIcon.setImage(getImage());
            this.trayIcon.setImageAutoSize(true);
            this.trayIcon.addActionListener(new ActionDefault());
        }
        addTrayToSys();
    }
    
    private boolean addTrayToSys() {
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
    
    public TrayIcon getTrayIcon() throws ExceptionInInitializerError {
        if (SystemTray.isSupported() && this.trayIcon != null) {
            synchronized(trayIcon) {
                return trayIcon;
            }
        }
        else {
            throw new UnsupportedOperationException("System tray unavailable");
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemTrayHelper{");
        sb.append("trayIcon=").append(trayIcon.hashCode());
        sb.append(", isNeedTray=").append(isNeedTray);
        sb.append('}');
        return sb.toString();
    }
    
    void delOldActions() {
        for (ActionListener actionListener : this.trayIcon.getActionListeners()) {
            trayIcon.removeActionListener(actionListener);
        }
    }
    
    private void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
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
}
