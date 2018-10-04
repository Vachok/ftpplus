package ru.vachok.networker.logic;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.*;


/**
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {

    private static final String IMG_FOLDER_NAME = "/static/images/";

    private static SystemTrayHelper s = new SystemTrayHelper();

    private SystemTrayHelper() {
    }

    public static SystemTrayHelper getInstance() {
        return s;
    }

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayHelper.class.getSimpleName());

    public static void addTray(String iconFileName) {
        SystemTray systemTray = SystemTray.getSystemTray();
        boolean b = ConstantsFor.thisPC().toLowerCase().contains("no0027") || ConstantsFor.thisPC().equalsIgnoreCase("home");
        if (iconFileName == null) {
            iconFileName = "icons8-ip-адрес-15.png";
        }
        if (b) {
            iconFileName = "icons8-плохие-поросята-48.png";
        }
        iconFileName = IMG_FOLDER_NAME + iconFileName;
        Image image = Toolkit.getDefaultToolkit().getImage(SystemTrayHelper.class.getResource(iconFileName));
        PopupMenu popupMenu = new PopupMenu();
        MenuItem defItem = new MenuItem();
        TrayIcon trayIcon = new TrayIcon(image, AppComponents.versionInfo().getAppBuild() + " v. " +
            AppComponents.versionInfo().getAppVersion() + " " + AppComponents.versionInfo().getBuildTime(), popupMenu);

        ActionListener actionListener = e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8880"));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        ActionListener exitApp = e -> System.exit(0);

        additionalItems(popupMenu);
        trayIcon.setImageAutoSize(true);
        defItem.setLabel("Exit");
        defItem.addActionListener(exitApp);
        popupMenu.add(defItem);
        trayIcon.addActionListener(actionListener);
        try{
            if(SystemTray.isSupported()){
                systemTray.add(trayIcon);
            }
            else{
                LOGGER.warn("Tray not supported!");
            }
        }
        catch(AWTException e){
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static void additionalItems(PopupMenu popupMenu) {
        ThreadConfig threadConfig = new ThreadConfig();
        ThreadPoolTaskExecutor executor = threadConfig.threadPoolTaskExecutor();
        MenuItem gitStartWeb = new MenuItem();
        gitStartWeb.addActionListener(actionEvent -> {
            Callable<String> sshStr = () -> new SSHFactory
                .Builder(ConstantsFor
                .SRV_GIT, "sudo git instaweb;" +
                "sudo cd /usr/home/dpetrov/;" +
                "sudo git instaweb -p 11111;" +
                "sudo cd /usr/home/kudr/;" +
                "sudo git instaweb -p 9999;" +
                "exit").build().call();
            Future<String> submit = executor.submit(sshStr);
            try {
                LOGGER.info(submit.get(30, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Thread.currentThread().interrupt();
            }
        });
        gitStartWeb.setLabel("GIT WEB ON");
        popupMenu.add(gitStartWeb);
    }
}