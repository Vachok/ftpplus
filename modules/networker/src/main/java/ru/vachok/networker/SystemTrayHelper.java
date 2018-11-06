package ru.vachok.networker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.net.MyServer;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.*;

import static java.lang.System.err;


/**
 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {

    private static final String IMG_FOLDER_NAME = "/static/images/";

    private static SystemTrayHelper s = new SystemTrayHelper();

    /*Fields*/

    private SystemTrayHelper() {
    }

    public static SystemTrayHelper getInstance() {
        return s;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayHelper.class.getSimpleName());

    public static void addTray(String iconFileName) {
        SystemTray systemTray = SystemTray.getSystemTray();
        boolean myPC = ConstantsFor.thisPC().toLowerCase().contains("no0027") || ConstantsFor.thisPC().equalsIgnoreCase("home");
        if (iconFileName == null) {
            iconFileName = "icons8-ip-адрес-15.png";
        }
        else
            if(myPC){
            iconFileName = "icons8-плохие-поросята-48.png";
        }
        if(!srvGitIs()){
            iconFileName = "icons8-отменить-2-20.png";
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
        ActionListener exitApp = e -> {
            ConstantsFor.saveProps();
            System.exit(0);
        };

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
            Callable<String> sshStr = () -> new SSHFactory.Builder(ConstantsFor
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
        Thread thread = executor.createThread(MyServer.getI());
        thread.start();
        popupMenu.add(gitStartWeb);
        MenuItem conToSoc = new MenuItem();
        conToSoc.setLabel("To telnet");
        conToSoc.addActionListener(e -> {
            thread.start();
        });
        popupMenu.add(conToSoc);
        MenuItem toConsole = new MenuItem();
        toConsole.setLabel("Console Back");
        toConsole.addActionListener(e -> {
            System.setOut(err);
            Socket socket = MyServer.getSocket();
            try {
                socket.close();
            } catch (IOException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        popupMenu.add(toConsole);
    }
    private static boolean srvGitIs() {
        try{
            return InetAddress.getByName("srv-git.eatmeat.ru").isReachable(1000);
        }
        catch(IOException e){
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}